import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import pl.dariusz_marecik.chess_rec.PieceInfo

class WebSocketManager(private val url: String) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val webSocketClient = WebSocketClient()

    fun start() {
        scope.launch {
            webSocketClient.startWithRetry(url)
        }
    }

    fun sendImage(bitmap: Bitmap) {
        scope.launch {
            webSocketClient.sendImage(bitmap)
        }
    }

    fun stop() {
        scope.cancel()
        webSocketClient.disconnect()
    }

    fun getPieces(): StateFlow<Map<Pair<Int, Int>, PieceInfo>> {
        return webSocketClient.piecesMap
    }
}
