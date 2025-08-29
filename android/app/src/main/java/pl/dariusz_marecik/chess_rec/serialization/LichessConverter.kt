package pl.dariusz_marecik.chess_rec.serialization

import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException

object LichessConverter {

    fun extractLichessLink(html: String): String? {
        val doc = Jsoup.parse(html)
        val metaTag = doc.selectFirst("meta[property=og:url]")
        return metaTag?.attr("content")
    }

    fun importPgnToLichess(pgn: String, onResult: (String?) -> Unit) {
        val client = OkHttpClient()

        val body = FormBody.Builder()
            .add("pgn", pgn)
            .build()

        val request = Request.Builder()
            .url("https://lichess.org/api/import")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        onResult(null)
                    } else {
                        val json = it.body?.string()
                        var url: String? = null
                        if (json != null) {
                            url = extractLichessLink(json)
                        }
                        onResult(url)
                    }
                }
            }
        })
    }
}