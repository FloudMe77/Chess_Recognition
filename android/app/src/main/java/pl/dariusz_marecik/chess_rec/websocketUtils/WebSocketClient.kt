package pl.dariusz_marecik.chess_rec.websocketUtils

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
import pl.dariusz_marecik.chess_rec.utils.PieceInfo
import java.util.concurrent.TimeUnit


class WebSocketClient : WebSocketListener() {

    // OkHttp client with ping interval for connection keep-alive
    private val client = OkHttpClient.Builder()
        .pingInterval(2, TimeUnit.SECONDS)
        .build()

    private lateinit var webSocket: WebSocket
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isReconnecting = false

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var _positionMap = MutableStateFlow<Map<Pair<Int, Int>, PieceInfo>>(emptyMap())
    val positionMap: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = _positionMap.asStateFlow()

    private val gson = Gson()

    // Attempt to connect to WebSocket with retry on failure
    fun startWithRetry(url: String) {
        Log.d("WebSocket", "Trying to connect")
        try {
            connect(url)
        } catch (_: SocketTimeoutException) {
        }
    }

    // Establish a WebSocket connection
    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, this)
    }

    // Send binary data over WebSocket if connected
    fun sendBytes(byteString: ByteString) {
        if (_isConnected.value) {
            webSocket.send(byteString)
            Log.d("WebSocket", "Image send success")
        }
    }

    // Close the WebSocket and cancel coroutines
    fun disconnect() {
        _isConnected.value = false
        scope.cancel() // zatrzymuje coroutines
        webSocket.close(1000, "Normal close")
    }

    // Called when WebSocket connection is successfully opened
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Connected to server")
        _isConnected.value = true
    }

    // Called when a text message is received; parse JSON to update pieces
    override fun onMessage(webSocket: WebSocket, text: String) {
        if (!_isConnected.value) return
        try {
            val listType = object : TypeToken<List<PieceInfo>>() {}.type
            val piecesList: List<PieceInfo> = gson.fromJson(text, listType)

            val newPiecesMap = piecesList.associateBy { it.cords }
            _positionMap.value = newPiecesMap

            piecesList.forEach { Log.d("WebSocket", "JSON: ${it.name} ${it.cords}") }

        } catch (e: Exception) {
            Log.d("WebSocket", "Error parsing JSON: ${e.message}")
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        //
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        _isConnected.value = false
        Log.e("WebSocket", "Connection failed: ${t.message}", t)
        attemptReconnect(webSocket.request().url.toString())
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "Disconnected: $code / $reason")
        _isConnected.value = false
        attemptReconnect(webSocket.request().url.toString())
    }

    // Attempt to reconnect after a delay if not already reconnecting
    private fun attemptReconnect(url: String) {
        if (isReconnecting) return
        isReconnecting = true

        scope.launch {
            delay(1000)
            try {
                connect(url)
            } catch (e: Exception) {
                Log.e("WebSocket", "Retry failed: ${e.message}")
            } finally {
                isReconnecting = false
            }
        }
    }
}
