package com.jandan.reader

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class PicComment(
    @SerializedName("comment_ID") val id: String,
    @SerializedName("comment_author") val author: String,
    @SerializedName("comment_date") val date: String,
    @SerializedName("comment_content") val content: String,
    @SerializedName("text_content") val textContent: String,
    @SerializedName("vote_positive") val votePositive: String,
    @SerializedName("vote_negative") val voteNegative: String,
    @SerializedName("sub_comment_count") val subCommentCount: String,
    val pics: List<String>?
)

data class PicResponse(
    val status: String,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_comments") val totalComments: Int,
    @SerializedName("page_count") val pageCount: Int,
    val count: Int,
    val comments: List<PicComment>
)

data class TreeholeComment(
    val id: Long,
    val author: String,
    val content: String,
    @SerializedName("date_gmt") val dateGmt: String,
    @SerializedName("vote_positive") val votePositive: Int,
    @SerializedName("vote_negative") val voteNegative: Int,
    @SerializedName("sub_comment_count") val subCommentCount: Int,
    @SerializedName("ip_location") val ipLocation: String?
)

data class TreeholeData(
    val list: List<TreeholeComment>,
    @SerializedName("total_pages") val totalPages: Int,
    val page: Int
)

data class TreeholeResponse(
    val code: Int,
    val data: TreeholeData
)

data class Tucao(
    @SerializedName("comment_ID") val id: String?,
    @SerializedName("comment_author") val author: String,
    @SerializedName("comment_content") val content: String,
    @SerializedName("vote_positive") val votePositive: Any?,
    @SerializedName("vote_negative") val voteNegative: Any?,
    @SerializedName("ip_location") val ipLocation: String?
)

data class TucaoResponse(
    val code: Int,
    val tucao: List<Tucao>?
)

object JandanApi {
    private val gson = Gson()
    private const val PIC_API = "https://i.jandan.net/?oxwlxojflwblxbsapi=jandan.get_pic_comments&page="
    private const val TREEHOLE_POST_ID = "102312"
    private const val TREEHOLE_API = "https://jandan.net/api/comment/post/"
    private const val TUCAO_API = "https://jandan.net/api/tucao/list/"

    fun fetchPicComments(page: Int): PicResponse? {
        return try {
            val json = httpGet("$PIC_API$page")
            gson.fromJson(json, PicResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun fetchTreeholeComments(page: Int): TreeholeResponse? {
        return try {
            val url = "${TREEHOLE_API}${TREEHOLE_POST_ID}?order=desc&page=$page"
            val json = httpGet(url)
            gson.fromJson(json, TreeholeResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun detectTreeholeTotalPages(): Int {
        return try {
            val url = "${TREEHOLE_API}${TREEHOLE_POST_ID}?order=desc&page=0"
            val json = httpGet(url)
            val resp = gson.fromJson(json, TreeholeResponse::class.java)
            resp.data.totalPages
        } catch (e: Exception) {
            1
        }
    }

    fun fetchTucao(commentId: String): List<Tucao> {
        return try {
            val json = httpGet("$TUCAO_API$commentId")
            val resp = gson.fromJson(json, TucaoResponse::class.java)
            if (resp.code == 0) resp.tucao ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun batchFetchTucao(commentIds: List<String>): Map<String, List<Tucao>> {
        val result = mutableMapOf<String, List<Tucao>>()
        for (id in commentIds) {
            val tucaoList = fetchTucao(id)
            if (tucaoList.isNotEmpty()) {
                result[id] = tucaoList
            }
        }
        return result
    }

    private fun httpGet(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        conn.setRequestProperty("Referer", "https://jandan.net/")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val response = reader.readText()
        reader.close()
        conn.disconnect()
        return response
    }
}
