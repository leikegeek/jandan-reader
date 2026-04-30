package com.jandan.reader

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class JandanToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()

        val treeholePanel = JandanPanel(project, ContentType.TREEHOLE)
        val treeholeContent = contentFactory.createContent(treeholePanel, "ServiceLog", false)
        toolWindow.contentManager.addContent(treeholeContent)

        val picPanel = JandanPanel(project, ContentType.PIC)
        val picContent = contentFactory.createContent(picPanel, "ResourceCfg", false)
        toolWindow.contentManager.addContent(picContent)
    }
}

enum class ContentType {
    TREEHOLE, PIC
}
