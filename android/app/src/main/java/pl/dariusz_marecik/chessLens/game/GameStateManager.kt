package pl.dariusz_marecik.chessLens.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.dariusz_marecik.chessLens.enums.ColorTeam

// Manages game state including current player, move detection, and game end conditions (mate/stalemate).
class GameStateManager(startingPlayerColor: ColorTeam = ColorTeam.WHITE) {
    private var _isMoveFound = MutableStateFlow(false)
    val isMoveFound: StateFlow<Boolean> = _isMoveFound.asStateFlow()

    private var _isMate = MutableStateFlow(false)
    val isMate: StateFlow<Boolean> = _isMate.asStateFlow()

    private var _isStaleMate = MutableStateFlow(false)
    val isStaleMate: StateFlow<Boolean> = _isStaleMate.asStateFlow()

    var onMove: ColorTeam = startingPlayerColor
        private set

    fun setMoveFound(found: Boolean) {
        _isMoveFound.value = found
    }

    fun setMate(mate: Boolean) {
        _isMate.value = mate
    }

    fun setStaleMate(staleMate: Boolean) {
        _isStaleMate.value = staleMate
    }

    fun switchPlayer() {
        onMove = if (onMove == ColorTeam.WHITE) ColorTeam.BLACK else ColorTeam.WHITE
    }
}
