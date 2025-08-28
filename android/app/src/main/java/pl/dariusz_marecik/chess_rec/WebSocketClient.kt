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


class WebSocketClient : WebSocketListener() {

    private val client = OkHttpClient.Builder()
        .build()

    private lateinit var webSocket: WebSocket
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    

    private var _piecesMap = MutableStateFlow<Map<Pair<Int, Int>, PieceInfo>>(emptyMap())
    val piecesMap: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = _piecesMap.asStateFlow()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    fun startWithRetry(url: String){
        scope.launch {
            while (!_isConnected.value) {
                Log.d("WebSocket", "Trying to connect")
                try {
                    connect(url)
                }
                catch (_: SocketTimeoutException){

                }
                delay(10000)
            }
        }
    }

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, this)

    }

    fun sendBytes(byteString: ByteString) {
        if (_isConnected.value){
            webSocket.send(byteString)
            Log.d("WebSocket", "Image send success")
        }
    }

    fun disconnect() {
        _isConnected.value = false
        scope.cancel()
        webSocket.close(1000, "Normal close")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Connected to server")
        _isConnected.value = true
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (_isConnected.value) {
            try {
                if(text.startsWith("Ping")){

                }
                else{
                    val listType = object : TypeToken<List<PieceInfo>>() {}.type
                    val piecesList: List<PieceInfo> = gson.fromJson(text, listType)
                    val newPiecesMap = mutableMapOf<Pair<Int, Int>, PieceInfo>()
                    for (piece in piecesList) {
                        newPiecesMap.put(piece.cords, piece)
                        Log.d("WebSocket", "JSON: ${piece.name} ${piece.cords}")
                    }
                    _piecesMap.value = newPiecesMap
                }
            } catch (e: Exception) {
                Log.d("WebSocket", "Error: ${e.message}")
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {

    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        _isConnected.value = false
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
        _isConnected.value = false
        scope.launch {
            delay(1000) // np. 2 sekundy przerwy
            try {
                connect(webSocket.request().url.toString())
            } catch (e: Exception) {
                Log.e("WebSocket", "Retry failed: ${e.message}")
            }
        }
    }
}
