package com.jandan.reader

object CodeDisguiser {

    data class Theme(
        val bg: String, val fg: String, val kw: String, val str: String,
        val cmt: String, val ann: String, val num: String, val fn: String,
        val border: String, val tucaoBg: String
    )

    val DARK = Theme(
        bg = "#2b2b2b", fg = "#a9b7c6", kw = "#cc7832", str = "#6a8759",
        cmt = "#808080", ann = "#bbb529", num = "#6897bb", fn = "#ffc66d",
        border = "#3c3f41", tucaoBg = "#313335"
    )
    val LIGHT = Theme(
        bg = "#ffffff", fg = "#000000", kw = "#000080", str = "#008000",
        cmt = "#808080", ann = "#808000", num = "#0000ff", fn = "#7a7a43",
        border = "#d0d0d0", tucaoBg = "#f5f5f5"
    )

    private fun esc(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private fun cleanHtml(raw: String): String {
        return raw
            .replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
            .replace("&quot;", "\"").replace("&#039;", "'").replace("&nbsp;", " ")
            .trim()
    }

    private fun sp(n: Int) = "&nbsp;".repeat(n)
    private fun kw(t: Theme, s: String) = "<font color=\"${t.kw}\"><b>$s</b></font>"
    private fun str(t: Theme, s: String) = "<font color=\"${t.str}\">\"${esc(s)}\"</font>"
    private fun cmt(t: Theme, s: String) = "<font color=\"${t.cmt}\">$s</font>"
    private fun ann(t: Theme, s: String) = "<font color=\"${t.ann}\">$s</font>"
    private fun fn(t: Theme, s: String) = "<font color=\"${t.fn}\">$s</font>"
    private fun num(t: Theme, s: String) = "<font color=\"${t.num}\">$s</font>"
    private fun ln(content: String) = "$content<br>"

    private fun header(t: Theme): String {
        return """<html><head></head>
            <body bgcolor="${t.bg}" text="${t.fg}" style="margin:0;padding:0;">
            <font face="Consolas, 'JetBrains Mono', 'Courier New', monospace" size="3">
            <div style="padding:12px 16px;">"""
    }

    private fun footer(): String = "</div></font></body></html>"

    private fun renderTucao(t: Theme, tucaoList: List<Tucao>, indent: Int): String {
        if (tucaoList.isEmpty()) return ""
        val sb = StringBuilder()
        val pad = sp(indent)
        sb.append(ln(""))
        sb.append(ln("$pad${cmt(t, "// ───── replies ─────")}"))
        tucaoList.forEach { tc ->
            val cleanContent = cleanHtml(tc.content)
            if (cleanContent.isBlank()) return@forEach
            val loc = tc.ipLocation ?: ""
            val locStr = if (loc.isNotBlank()) " @ $loc" else ""
            val lines = cleanContent.split("\n").filter { it.isNotBlank() }
            lines.forEachIndexed { i, line ->
                val prefix = if (i == 0) "${cmt(t, "// &gt;&gt; ${esc(tc.author)}$locStr:")}" else "${cmt(t, "//")}"
                sb.append(ln("$pad$prefix ${cmt(t, esc(line.trim()))}"))
            }
        }
        sb.append(ln("$pad${cmt(t, "// ─────────────────")}"))
        return sb.toString()
    }

    fun disguiseTreeholeHtml(
        comments: List<TreeholeComment>,
        tucaoMap: Map<String, List<Tucao>>,
        page: Int,
        isDark: Boolean
    ): String {
        val t = if (isDark) DARK else LIGHT
        val sb = StringBuilder()
        sb.append(header(t))

        sb.append(ln("${kw(t, "package")} com.project.core.service;"))
        sb.append(ln(""))
        sb.append(ln("${kw(t, "import")} java.util.concurrent.CompletableFuture;"))
        sb.append(ln("${kw(t, "import")} org.slf4j.Logger;"))
        sb.append(ln("${kw(t, "import")} org.slf4j.LoggerFactory;"))
        sb.append(ln(""))
        sb.append(ln(cmt(t, "/**")))
        sb.append(ln(cmt(t, " * MessageProcessingService - Page $page")))
        sb.append(ln(cmt(t, " * @author system")))
        sb.append(ln(cmt(t, " */")))
        sb.append(ln("${kw(t, "public class")} MessageProcessingService {"))
        sb.append(ln(""))
        sb.append(ln("${sp(4)}${kw(t, "private static final")} Logger log = LoggerFactory.getLogger(MessageProcessingService.${kw(t, "class")});"))
        sb.append(ln(""))

        comments.forEach { comment ->
            val clean = cleanHtml(comment.content)
            if (clean.isBlank()) return@forEach

            val loc = comment.ipLocation ?: "Unknown"
            val methodName = "processMsg_${comment.id}"

            sb.append(ln("${sp(4)}${cmt(t, "/**")}"))
            sb.append(ln("${sp(4)}${cmt(t, " * [${esc(comment.author)}] @ $loc")}"))
            sb.append(ln("${sp(4)}${cmt(t, " * ${comment.dateGmt}")}"))
            sb.append(ln("${sp(4)}${cmt(t, " * ${ann(t, "oo:${comment.votePositive}")} / ${ann(t, "xx:${comment.voteNegative}")}  replies:${num(t, "${comment.subCommentCount}")}")}"))
            sb.append(ln("${sp(4)}${cmt(t, " */")}"))
            sb.append(ln("${sp(4)}${kw(t, "public")} CompletableFuture&lt;Void&gt; ${fn(t, methodName)}() {"))

            clean.split("\n").filter { it.isNotBlank() }.forEach { line ->
                sb.append(ln("${sp(8)}log.info(${str(t, line.trim())});"))
            }

            val tucaoList = tucaoMap[comment.id.toString()] ?: emptyList()
            sb.append(renderTucao(t, tucaoList, 8))

            sb.append(ln("${sp(8)}${kw(t, "return")} CompletableFuture.completedFuture(${kw(t, "null")});"))
            sb.append(ln("${sp(4)}}"))
            sb.append(ln(""))
        }

        sb.append(ln("}"))
        sb.append(footer())
        return sb.toString()
    }

    fun disguisePicsHtml(
        comments: List<PicComment>,
        tucaoMap: Map<String, List<Tucao>>,
        page: Int,
        isDark: Boolean
    ): String {
        val t = if (isDark) DARK else LIGHT
        val sb = StringBuilder()
        sb.append(header(t))

        sb.append(ln("${kw(t, "package")} com.project.resource.config;"))
        sb.append(ln(""))
        sb.append(ln("${kw(t, "import")} org.springframework.context.annotation.Bean;"))
        sb.append(ln("${kw(t, "import")} org.springframework.context.annotation.Configuration;"))
        sb.append(ln("${kw(t, "import")} org.springframework.core.io.Resource;"))
        sb.append(ln("${kw(t, "import")} org.springframework.core.io.UrlResource;"))
        sb.append(ln(""))
        sb.append(ln(cmt(t, "/**")))
        sb.append(ln(cmt(t, " * ResourceRegistryConfig - Batch $page")))
        sb.append(ln(cmt(t, " * Auto-generated resource configuration")))
        sb.append(ln(cmt(t, " */")))
        sb.append(ln("${ann(t, "@Configuration")}"))
        sb.append(ln("${kw(t, "public class")} ResourceRegistryConfig {"))
        sb.append(ln(""))

        comments.forEach { comment ->
            val cleanText = cleanHtml(comment.textContent)
            val beanName = "res_${comment.id}"
            val pics = comment.pics ?: emptyList()

            sb.append(ln("${sp(4)}${cmt(t, "/**")}"))
            sb.append(ln("${sp(4)}${cmt(t, " * @author ${esc(comment.author)}")}"))
            sb.append(ln("${sp(4)}${cmt(t, " * @since ${comment.date}")}"))
            sb.append(ln("${sp(4)}${cmt(t, " * ${ann(t, "oo:${comment.votePositive}")} / ${ann(t, "xx:${comment.voteNegative}")}  replies:${num(t, comment.subCommentCount)}")}"))
            if (cleanText.isNotBlank()) {
                cleanText.split("\n").filter { it.isNotBlank() }.forEach { line ->
                    sb.append(ln("${sp(4)}${cmt(t, " * &gt; ${esc(line.trim())}")}"))
                }
            }
            sb.append(ln("${sp(4)}${cmt(t, " */")}"))
            sb.append(ln("${sp(4)}${ann(t, "@Bean")}(${str(t, beanName)})"))
            sb.append(ln("${sp(4)}${kw(t, "public")} Resource ${fn(t, beanName)}() {"))

            if (pics.isNotEmpty()) {
                sb.append(ln("${sp(8)}${cmt(t, "/* resource preview */")}"))
                pics.forEach { picUrl ->
                    val url = if (picUrl.startsWith("//")) "https:$picUrl" else picUrl
                    sb.append("<div style=\"margin:6px 0 6px 48px;\">")
                    sb.append("<img src=\"$url\" width=\"420\" border=\"1\" ")
                    sb.append("style=\"border-color:${t.border};\" ")
                    sb.append("onerror=\"this.style.display='none'\" />")
                    sb.append("</div>")
                }
                sb.append(ln("${sp(8)}${kw(t, "return new")} UrlResource(${str(t, pics.first())});"))
            } else {
                sb.append(ln("${sp(8)}${kw(t, "return null")}; ${cmt(t, "// no resource")}"))
            }

            val tucaoList = tucaoMap[comment.id] ?: emptyList()
            sb.append(renderTucao(t, tucaoList, 8))

            sb.append(ln("${sp(4)}}"))
            sb.append(ln(""))
        }

        sb.append(ln("}"))
        sb.append(footer())
        return sb.toString()
    }

    fun errorHtml(message: String, isDark: Boolean): String {
        val t = if (isDark) DARK else LIGHT
        val sb = StringBuilder()
        sb.append(header(t))
        sb.append(ln("${kw(t, "package")} com.project.core.error;"))
        sb.append(ln(""))
        sb.append(ln(cmt(t, "/**")))
        sb.append(ln(cmt(t, " * Connection Error")))
        sb.append(ln(cmt(t, " * ${esc(message)}")))
        sb.append(ln(cmt(t, " * Please check network and retry.")))
        sb.append(ln(cmt(t, " */")))
        sb.append(ln("${kw(t, "public class")} ConnectionErrorHandler {"))
        sb.append(ln("${sp(4)}${kw(t, "public void")} ${fn(t, "handle")}() {"))
        sb.append(ln("${sp(8)}${kw(t, "throw new")} RuntimeException(${str(t, message)});"))
        sb.append(ln("${sp(4)}}"))
        sb.append(ln("}"))
        sb.append(footer())
        return sb.toString()
    }
}
