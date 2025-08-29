package pl.dariusz_marecik.chess_rec

import pl.dariusz_marecik.chess_rec.websocketUtils.WebSocketManager
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.dariusz_marecik.chess_rec.utils.Move
import pl.dariusz_marecik.chess_rec.utils.PieceInfo


class PositionViewModel : ViewModel() {
    private val manager = WebSocketManager("ws://192.168.0.21:8765/ws")
    private var _moveList = MutableStateFlow<List<Move>>(listOf())
    val moveList: StateFlow<List<Move>> = _moveList.asStateFlow()

    val pieces: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = manager.getPieces()

    init {
        Log.d("PiecesViewModel", "init")
        manager.start()
    }

    fun sendImage(bitmap: Bitmap) {
        manager.sendImage(bitmap)
    }

    fun saveMove(move: Move) {
        _moveList.value += move
        Log.d("PiecesViewModel", moveList.toString())
    }

    fun restartListMove() {
        _moveList.value -= _moveList.value
    }

    fun getLatestMove(): Move? {
        return _moveList.value.lastOrNull()
    }

    fun getConnectionStatus(): StateFlow<Boolean> {
        return manager.getConnectionStatus()
    }
}
