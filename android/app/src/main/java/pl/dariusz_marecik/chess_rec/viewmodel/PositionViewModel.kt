package pl.dariusz_marecik.chess_rec.viewmodel

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

    val pieces: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = manager.getPosition()

    init {
        Log.d("PiecesViewModel", "init")
        manager.start()
    }

    // Send a captured bitmap image to the WebSocket server
    fun sendImage(bitmap: Bitmap) {
        manager.sendImage(bitmap)
    }

    // Save a new move to the move list
    fun saveMove(move: Move) {
        _moveList.value += move
        Log.d("PiecesViewModel", moveList.toString())
    }

    // Clear all saved moves
    fun restartListMove() {
        _moveList.value = emptyList()
    }

    // Get the last move made, if any
    fun getLatestMove(): Move? {
        return _moveList.value.lastOrNull()
    }

    // Get the current connection status of the WebSocket
    fun getConnectionStatus(): StateFlow<Boolean> {
        return manager.getConnectionStatus()
    }
}
