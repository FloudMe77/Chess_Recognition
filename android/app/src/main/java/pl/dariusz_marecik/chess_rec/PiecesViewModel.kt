package pl.dariusz_marecik.chess_rec

import WebSocketClient
import WebSocketManager
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import pl.dariusz_marecik.chess_rec.BuildConfig



class PiecesViewModel: ViewModel() {
    private val manager = WebSocketManager(BuildConfig.WEBSOCKET_IP)
    private var _moveList = MutableStateFlow<List<Move>>(listOf())
    val moveList: StateFlow<List<Move>> = _moveList.asStateFlow()

    val pieces: StateFlow<Map<Pair<Int, Int>,PieceInfo>> = manager.getPieces()

    init {
        Log.d("PiecesViewModel", "init")
        manager.start()
    }
    fun sendImage(bitmap: Bitmap) {
        manager.sendImage(bitmap)
    }
    fun saveMove(move:Move){
        _moveList.value += move
        Log.d("PiecesViewModel", moveList.toString())
    }
    fun restartListMove(){
        _moveList.value -= _moveList.value
    }
    fun getLatestMove(): Move? {
        return _moveList.value.lastOrNull()
    }
}
