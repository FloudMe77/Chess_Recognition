import android.graphics.Bitmap
import okhttp3.*
import okio.ByteString
import com.google.gson.Gson
import android.util.Log
import com.google.gson.reflect.TypeToken
import io.ktor.client.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.dariusz_marecik.chess_rec.PieceInfo
import java.io.ByteArrayOutputStream


class WebSocketClient : WebSocketListener() {

    private val client = OkHttpClient.Builder()
        .build()

    private lateinit var webSocket: WebSocket
    private var isRunning = false

    private var _piecesMap = MutableStateFlow<Map<Pair<Int, Int>, PieceInfo>>(emptyMap())
    val piecesMap: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = _piecesMap.asStateFlow()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startWithRetry(url: String){
        scope.launch {
            while (!isRunning) {
                Log.d("WebSocket", "Trying to connect")
                try {
                    connect(url)
                }
                catch (e: SocketTimeoutException){

                }
                delay(1000)
            }
        }
    }

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, this)

    }
    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int = 640): Bitmap {
        val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    fun sendImage(bitmap: Bitmap) {
        if (isRunning){
            val scaledBitmap = scaleBitmap(bitmap, 640)
            val stream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val byteString = ByteString.of(*byteArray)
            webSocket.send(byteString)
            Log.d("WebSocket", "Image send success")
        }
    }

    fun disconnect() {
        isRunning = false
        scope.cancel()
        webSocket.close(1000, "Normal close")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Connected to server")
        isRunning = true
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (isRunning) {
            try {
                val listType = object : TypeToken<List<PieceInfo>>() {}.type
                val piecesList: List<PieceInfo> = gson.fromJson(text, listType)
                val newPiecesMap = mutableMapOf<Pair<Int, Int>, PieceInfo>()
                for (piece in piecesList) {
                    newPiecesMap.put(piece.cords, piece)
                    Log.d("WebSocket", "JSON: ${piece.name} ${piece.cords}")
                }
                _piecesMap.value = newPiecesMap
            } catch (e: Exception) {
                Log.d("WebSocket", "Error: ${e.message}")
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {

    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isRunning = false
        Log.e("WebSocket", "Connection failed: ${t.message}", t)
        scope.launch {
            delay(1000) // np. 2 sekundy przerwy
            try {
                connect(webSocket.request().url.toString())
            } catch (e: Exception) {
                Log.e("WebSocket", "Retry failed: ${e.message}")
            }
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "Disconnected: $code / $reason")
    }
}
