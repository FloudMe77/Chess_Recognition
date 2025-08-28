import android.graphics.Bitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import okio.ByteString
import pl.dariusz_marecik.chess_rec.PieceInfo
import java.io.ByteArrayOutputStream

class WebSocketManager(private val url: String) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val webSocketClient = WebSocketClient()

    fun start() {
        scope.launch {
            webSocketClient.startWithRetry(url)
        }
    }
    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int = 640): Bitmap {
        val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    fun sendImage(bitmap: Bitmap) {
        scope.launch {
            val scaledBitmap = scaleBitmap(bitmap, 640)
            val stream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val byteString = ByteString.of(*byteArray)
            webSocketClient.sendBytes(byteString)
        }
    }

    fun stop() {
        scope.cancel()
        webSocketClient.disconnect()
    }

    fun getPieces(): StateFlow<Map<Pair<Int, Int>, PieceInfo>> {
        return webSocketClient.piecesMap
    }
    fun getConnectionStatus(): StateFlow<Boolean> {
        return webSocketClient.isConnected
    }
}
