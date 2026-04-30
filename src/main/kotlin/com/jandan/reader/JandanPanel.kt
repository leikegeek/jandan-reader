package com.jandan.reader

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.text.html.HTMLEditorKit

class JandanPanel(
    private val project: Project,
    private val contentType: ContentType
) : JPanel(BorderLayout()) {

    private var currentPage = 1
    private var totalPages = 1
    private val pageLabel = JLabel("Page: 1 / ?")
    private val editorPane: JEditorPane
    private val statusLabel = JLabel("")

    init {
        editorPane = JEditorPane().apply {
            isEditable = false
            contentType = "text/html"
            val kit = HTMLEditorKit()
            editorKit = kit
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        }

        val scrollPane = JScrollPane(editorPane).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }

        val toolbar = createToolbar()
        add(toolbar, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
        add(statusLabel, BorderLayout.SOUTH)

        loadContent()
    }

    private fun createToolbar(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))

        val prevBtn = JButton("<< Prev").apply {
            addActionListener {
                if (currentPage > 1) {
                    currentPage--
                    loadContent()
                }
            }
        }

        val nextBtn = JButton("Next >>").apply {
            addActionListener {
                if (currentPage < totalPages) {
                    currentPage++
                    loadContent()
                }
            }
        }

        val refreshBtn = JButton("Refresh").apply {
            addActionListener { loadContent() }
        }

        val jumpField = JTextField(4)
        val jumpBtn = JButton("Go").apply {
            addActionListener {
                val target = jumpField.text.toIntOrNull()
                if (target != null && target in 1..totalPages) {
                    currentPage = target
                    loadContent()
                }
            }
        }

        panel.add(prevBtn)
        panel.add(pageLabel)
        panel.add(nextBtn)
        panel.add(JSeparator(SwingConstants.VERTICAL))
        panel.add(JLabel("Jump:"))
        panel.add(jumpField)
        panel.add(jumpBtn)
        panel.add(JSeparator(SwingConstants.VERTICAL))
        panel.add(refreshBtn)

        return panel
    }

    private fun loadContent() {
        pageLabel.text = "Loading..."
        statusLabel.text = " Fetching data..."

        Thread {
            val isDark = UIUtil.isUnderDarcula()
            val html = when (contentType) {
                ContentType.TREEHOLE -> loadTreehole(isDark)
                ContentType.PIC -> loadPics(isDark)
            }

            ApplicationManager.getApplication().invokeLater {
                editorPane.text = html
                editorPane.caretPosition = 0
                pageLabel.text = "Page: $currentPage / $totalPages"
                statusLabel.text = ""
            }
        }.start()
    }

    private fun loadTreehole(isDark: Boolean): String {
        return try {
            if (totalPages <= 1) {
                totalPages = JandanApi.detectTreeholeTotalPages()
                currentPage = totalPages
            }
            val response = JandanApi.fetchTreeholeComments(currentPage)
            if (response != null && response.code == 0) {
                totalPages = response.data.totalPages

                statusLabel.text = " Loading replies..."
                val idsWithTucao = response.data.list
                    .filter { it.subCommentCount > 0 }
                    .map { it.id.toString() }
                val tucaoMap = JandanApi.batchFetchTucao(idsWithTucao)

                CodeDisguiser.disguiseTreeholeHtml(response.data.list, tucaoMap, currentPage, isDark)
            } else {
                CodeDisguiser.errorHtml("Failed to fetch treehole data", isDark)
            }
        } catch (e: Exception) {
            CodeDisguiser.errorHtml("Exception: ${e.message}", UIUtil.isUnderDarcula())
        }
    }

    private fun loadPics(isDark: Boolean): String {
        return try {
            val response = JandanApi.fetchPicComments(currentPage)
            if (response != null && response.status == "ok") {
                totalPages = response.pageCount

                statusLabel.text = " Loading replies..."
                val idsWithTucao = response.comments
                    .filter { (it.subCommentCount.toIntOrNull() ?: 0) > 0 }
                    .map { it.id }
                val tucaoMap = JandanApi.batchFetchTucao(idsWithTucao)

                CodeDisguiser.disguisePicsHtml(response.comments, tucaoMap, currentPage, isDark)
            } else {
                CodeDisguiser.errorHtml("Failed to fetch pic data", isDark)
            }
        } catch (e: Exception) {
            CodeDisguiser.errorHtml("Exception: ${e.message}", UIUtil.isUnderDarcula())
        }
    }
}
