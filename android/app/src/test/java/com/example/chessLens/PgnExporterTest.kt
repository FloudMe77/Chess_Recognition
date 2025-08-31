package com.example.chessLens

import okhttp3.*
import org.junit.Test
import java.io.IOException
import org.jsoup.Jsoup

class PgnExporterTest(){
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
    val pgnTest = """[Event "Custom Game"]
[Site "Local"]
[Date "2025.08.21"]
[Round "1"]
[White "Player1"]
[Black "Player2"]
[Result "*"]
[Variant "From Position"]
[FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBRR w KQkq - 0 1"]
                                                                                                    
1. e2e4 1... d7d5 2. e4xd5 2... c7c5 3. d5c6 3... Ng8f6 4. c6xb7 4... Nf6g4 5. b7a8Q 
"""
    @Test
    fun test() {
        val latch = java.util.concurrent.CountDownLatch(1)

        importPgnToLichess(pgnTest) { url ->
            if (url != null) {
                println("Link do partii: $url")
            } else {
                println("Nie udało się zaimportować partii.")
            }
            latch.countDown()  // odblokowuje test
        }

        latch.await()  // czeka aż callback się wykona
    }
}