package pl.dariusz_marecik.chess_rec

import java.util.*

object Config {
    val props: Properties by lazy {
        Properties().apply {
            val inputStream = Config::class.java.classLoader
                ?.getResourceAsStream("config.properties")
            load(inputStream)
        }
    }

    val WEBSOCKET_IP: String by lazy { props.getProperty("WEBSOCKET_IP") }
}