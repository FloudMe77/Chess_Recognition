package pl.dariusz_marecik.chessLens.game

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.enums.GameEndResult
import pl.dariusz_marecik.chessLens.utils.Move
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import java.lang.reflect.Modifier.PRIVATE

// Orchestrates chess game logic by coordinating move validation, position updates, and game state management.
class PositionManager(
    startPosition: Map<Pair<Int, Int>, PieceInfo>,
    startingPlayerColor: ColorTeam = ColorTeam.WHITE
) {
    private var _positionToDraw = MutableStateFlow<Map<Pair<Int, Int>, PieceInfo>>(emptyMap())
    val positionToDraw: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = _positionToDraw.asStateFlow()

    // Delegate state management to GameStateManager
    private val gameStateManager = GameStateManager(startingPlayerColor)
    val isMoveFound: StateFlow<Boolean> = gameStateManager.isMoveFound
    val isMate: StateFlow<Boolean> = gameStateManager.isMate
    val isStaleMate: StateFlow<Boolean> = gameStateManager.isStaleMate
    val onMove: ColorTeam get() = gameStateManager.onMove

    // Component managers
    private val castlingManager = CastlingManager()
    private val enPassantManager = EnPassantManager()
    private val positionModifier = PositionModifier()
    private val moveValidator = MoveValidator(castlingManager, enPassantManager)
    private val gameEndChecker = GameEndChecker(enPassantManager, positionModifier)

    private var previousPosition: Map<Pair<Int, Int>, PieceInfo> = startPosition
    private var potentialNewPosition: Map<Pair<Int, Int>, PieceInfo> = startPosition
    var potentialNewMove: Move? = null
        private set

    init {
        _positionToDraw.value = startPosition
    }

    fun considerNewPosition(detectedPosition: Map<Pair<Int, Int>, PieceInfo>) {
        val fromCordsSet = previousPosition.keys - detectedPosition.keys
        val toCordsSet = detectedPosition.keys - previousPosition.keys

        if (fromCordsSet.isEmpty() && toCordsSet.isEmpty()) {
            // undo changes
            potentialNewMove = null
            gameStateManager.setMoveFound(false)
            potentialNewPosition = previousPosition
            _positionToDraw.value = previousPosition
            return
        }

        val potentialMove = findLegalMove(detectedPosition, fromCordsSet, toCordsSet) ?: return

        if (potentialMove == potentialNewMove) return

        val newPosition: MutableMap<Pair<Int, Int>, PieceInfo> =
            previousPosition.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()

        positionModifier.applyMove(newPosition, potentialMove)

        if (!gameEndChecker.isKingAttacked(newPosition, onMove)) {
            gameStateManager.setMoveFound(true)
            potentialNewPosition = newPosition
            potentialNewMove = potentialMove
            _positionToDraw.value = newPosition
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun findLegalMove(
        detectedPosition: Map<Pair<Int, Int>, PieceInfo>,
        fromCordsSet: Set<Pair<Int, Int>>,
        toCordsSet: Set<Pair<Int, Int>>
    ): Move? {
        return moveValidator.findLegalMove(previousPosition, detectedPosition, fromCordsSet, toCordsSet, onMove)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun isKingAttacked(newPieces: Map<Pair<Int, Int>, PieceInfo>, color: ColorTeam = onMove): Boolean {
        return gameEndChecker.isKingAttacked(newPieces, color)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun haveAnyLegalMove(positionToCheck: Map<Pair<Int, Int>, PieceInfo>): Boolean {
        return gameEndChecker.haveAnyLegalMove(positionToCheck, onMove)
    }

    fun acceptNewState(): Move? {
        potentialNewMove?.let { move ->
            castlingManager.updateCastlingRights(move)
            enPassantManager.updateEnPassant(move)
        }

        previousPosition = potentialNewPosition
        val tmp = potentialNewMove
        potentialNewMove = null
        gameStateManager.setMoveFound(false)
        gameStateManager.switchPlayer()

        // Check game end
        val gameEndResult = gameEndChecker.checkGameEnd(previousPosition, onMove)
        when (gameEndResult) {
            GameEndResult.MATE -> gameStateManager.setMate(true)
            GameEndResult.STALEMATE -> gameStateManager.setStaleMate(true)
            GameEndResult.CONTINUE -> { /* Game continues */ }
        }

        return tmp
    }
}