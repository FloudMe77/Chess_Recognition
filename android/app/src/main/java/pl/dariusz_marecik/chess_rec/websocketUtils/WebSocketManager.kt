package pl.dariusz_marecik.chess_rec.websocketUtils

import android.graphics.Bitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import okio.ByteString
import pl.dariusz_marecik.chess_rec.utils.PieceInfo
import java.io.ByteArrayOutputStream

class WebSocketManager(private val url: String) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val webSocketClient = WebSocketClient()

    // Start the WebSocket client
    fun start() {
        scope.launch {
            webSocketClient.startWithRetry(url)
        }
    }

    // Scale the bitmap to a maximum dimension while keeping aspect ratio
    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int = 640): Bitmap {
        val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    // Send a bitmap image to the WebSocket server
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

    // Stop the WebSocket and cancel all coroutines
    fun stop() {
        scope.cancel()
        webSocketClient.disconnect()
    }

    // Expose current pieces state from the WebSocket client
    fun getPosition(): StateFlow<Map<Pair<Int, Int>, PieceInfo>> {
        return webSocketClient.positionMap
    }

    // Expose current connection status from the WebSocket client
    fun getConnectionStatus(): StateFlow<Boolean> {
        return webSocketClient.isConnected
    }
}
