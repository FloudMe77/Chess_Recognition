package pl.dariusz_marecik.chess_rec

import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Modifier.PRIVATE
import kotlin.collections.minus
import kotlin.math.abs

class PositionManager(startPosition: Map<Pair<Int, Int>, PieceInfo>) {
    private var _positionToDraw = MutableStateFlow<Map<Pair<Int, Int>, PieceInfo>>(emptyMap())
    val positionToDraw: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = _positionToDraw.asStateFlow()

    private var _isMoveFound = MutableStateFlow(false)
    val isMoveFound: StateFlow<Boolean> = _isMoveFound.asStateFlow()

    private val castleMap: Map<ColorTeam, CastleInfo>
    init {
        _positionToDraw.value = startPosition
        castleMap = mapOf(
            ColorTeam.WHITE to CastleInfo(),
            ColorTeam.BLACK to CastleInfo()
        )
    }

    private var previousPosition: Map<Pair<Int, Int>,PieceInfo> = startPosition
    var onMove: ColorTeam = ColorTeam.WHITE
        private set

    private var enPassant: Pair<Int, Int>? = null
    private var potentialNewPosition: Map<Pair<Int, Int>,PieceInfo> = startPosition
    var potentialNewMove: Move? = null
        private set

    fun considerNewPosition(detectedPosition: Map<Pair<Int, Int>, PieceInfo>) {
        Log.d("PositionManager", "Otrzymałem newPieces: $detectedPosition")
        val fromCordsSet = previousPosition.keys - detectedPosition.keys
        val toCordsSet = detectedPosition.keys - previousPosition.keys
        Log.d("PositionManager", "fromPositions: $fromCordsSet, toPositions: $toCordsSet")
        Log.d("PositionManager", "onMove: $onMove")

        if(fromCordsSet.isEmpty() && toCordsSet.isEmpty()) {
            // undo changes
            potentialNewMove = null
            _isMoveFound.value = false
            potentialNewPosition = previousPosition
            _positionToDraw.value = previousPosition
        }

        val potentialMove = findLegalMove(fromCordsSet, toCordsSet, detectedPosition) ?: return

        val (from, to, label) = potentialMove
        //
        val newPosition: MutableMap<Pair<Int, Int>, PieceInfo> =
            previousPosition.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()

        fun applyMove(piece: PieceInfo, update: () -> Unit) {
            update()
            if (!isKingAttacked(newPosition)) {
                _isMoveFound.value = true
                potentialNewPosition = newPosition
                potentialNewMove = Move(from, to, piece.name, label)
                _positionToDraw.value = newPosition
            }
        }

        when (label) {
            Action.MOVE -> {
                newPosition.remove(from)?.let { piece ->
                    applyMove(piece) { piece.cords = to; newPosition[to] = piece }
                }
            }
            Action.PROMOTION -> {
                newPosition.remove(from)?.let { piece ->
                    applyMove(piece) {
                        val promoted = PieceInfo(piece.id + 4, piece.position, piece.bbox)
                        promoted.cords = to
                        newPosition[to] = promoted
                    }
                }
            }
            Action.TAKE, Action.EN_PASSANT -> {
                newPosition.remove(from)?.let { piece ->
                    applyMove(piece) {
                        val capturePos = if (label == Action.EN_PASSANT) Pair(to.first, from.second) else to
                        newPosition.keys.removeAll(listOfNotNull(from, capturePos).toSet())
                        piece.cords = to
                        newPosition[to] = piece
                    }
                }
            }
            Action.CASTLE_LEFT, Action.CASTLE_RIGHT -> {
                newPosition[from]?.let { king ->
                    val row = if (onMove == ColorTeam.WHITE) 0 else 7
                    val rookFrom = if (label == Action.CASTLE_LEFT) Pair(0, row) else Pair(7, row)
                    val rookTo = if (label == Action.CASTLE_LEFT) Pair(3, row) else Pair(5, row)

                    newPosition[rookFrom]?.let { rook ->
                        applyMove(king) {
                            newPosition.remove(from)
                            newPosition.remove(rookFrom)
                            king.cords = to
                            rook.cords = rookTo
                            newPosition[king.cords] = king
                            newPosition[rook.cords] = rook
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun findLegalMove(
        fromCordsSet: Set<Pair<Int, Int>>,
        toCordsSet: Set<Pair<Int, Int>>,
        detectedPosition: Map<Pair<Int, Int>, PieceInfo>
    ): Triple<Pair<Int, Int>, Pair<Int, Int>, Action>? {

        fun pawnPromotion(piece: PieceInfo, to: Pair<Int, Int>) =
            (piece.name == PieceKind.WHITE_PAWN && to.second == 7) ||
                    (piece.name == PieceKind.BLACK_PAWN && to.second == 0)

        // Castling
        if (fromCordsSet.size == 2 && toCordsSet.size == 2) {
            val (wantedRook, wantedKing, row) = when (onMove) {
                ColorTeam.WHITE -> Triple(PieceKind.WHITE_ROOK, PieceKind.WHITE_KING, 0)
                ColorTeam.BLACK -> Triple(PieceKind.BLACK_ROOK, PieceKind.BLACK_KING, 7)
            }
            val kingPos = fromCordsSet.firstOrNull { previousPosition[it]?.name == wantedKing }
            val rookPos = fromCordsSet.firstOrNull { previousPosition[it]?.name == wantedRook }

            if (kingPos == Pair(4, row) && rookPos != null) {
                val rook = previousPosition[rookPos]
                val leftCastle = castleMap[onMove]?.left == true &&
                        rookPos == Pair(0, row) &&
                        toCordsSet == setOf(Pair(2,row), Pair(3,row))
                val rightCastle = castleMap[onMove]?.right == true &&
                        rookPos == Pair(7, row) &&
                        toCordsSet == setOf(Pair(5,row), Pair(6,row))
                if (rook != null) {
                    when {
                        leftCastle && rook.movement.validateMove(Pair(0,row), Pair(3,row), previousPosition, onMove) ->
                            return Triple(kingPos, Pair(2,row), Action.CASTLE_LEFT)
                        rightCastle && rook.movement.validateMove(Pair(7,row), Pair(5,row), previousPosition, onMove) ->
                            return Triple(kingPos, Pair(6,row), Action.CASTLE_RIGHT)
                    }
                }
            }
        }

        // Regular move
        if (fromCordsSet.size == 1 && toCordsSet.size == 1) {
            val from = fromCordsSet.single()
            val to = toCordsSet.single()
            val piece = previousPosition[from]
            if (piece != null && !previousPosition.containsKey(to) && piece.color == onMove &&
                piece.movement.validateMove(from, to, previousPosition, piece.color)) {
                return if (pawnPromotion(piece, to)) Triple(from, to, Action.PROMOTION)
                else Triple(from, to, Action.MOVE)
            }
        }

        // Capture move
        if (fromCordsSet.size == 1 && toCordsSet.isEmpty()) {
            val from = fromCordsSet.single()
            previousPosition[from]?.let { piece ->
                if(piece.color == onMove){
                    val possibleTakes = piece.movement.possibleTake(from, previousPosition, piece.color)
                    possibleTakes.firstOrNull { pos ->
                        previousPosition[pos]?.color != detectedPosition[pos]?.color
                    }?.let { capturePos ->
                        return if (pawnPromotion(piece, capturePos)) Triple(from, capturePos, Action.PROMOTION)
                        else Triple(from, capturePos, Action.TAKE)
                    }
                }

            }
        }

        // En passant
        if (fromCordsSet.size == 2 && toCordsSet.size == 1) {
            val listFrom = fromCordsSet.toList()
            val to = toCordsSet.single()
            val whitePawn = listFrom.find { previousPosition[it]?.name == PieceKind.WHITE_PAWN }
            val blackPawn = listFrom.find { previousPosition[it]?.name == PieceKind.BLACK_PAWN }

            if (whitePawn != null && blackPawn != null) {
                val diff = blackPawn - whitePawn
                if (onMove == ColorTeam.BLACK && whitePawn == enPassant && abs(diff.first) == 1 && diff.second == 0 &&
                    to == whitePawn - Pair(0,1)) return Triple(blackPawn, to, Action.EN_PASSANT)
                if (onMove == ColorTeam.WHITE && blackPawn == enPassant && abs(diff.first) == 1 && diff.second == 0 &&
                    to == blackPawn + Pair(0,1)) return Triple(whitePawn, to, Action.EN_PASSANT)
            }
        }

        return null
    }


    @VisibleForTesting(otherwise = PRIVATE)
    internal fun isKingAttacked(newPieces: Map<Pair<Int, Int>, PieceInfo>, color: ColorTeam = onMove): Boolean {
        val king = newPieces.values.find {
            (color == ColorTeam.WHITE && it.name == PieceKind.WHITE_KING) ||
                    (color == ColorTeam.BLACK && it.name == PieceKind.BLACK_KING)
        } ?: return false

        return newPieces.values.any {
            it.color != color && king.cords in it.movement.possibleTake(it.cords, newPieces, it.color)
        }
    }

    fun acceptNewState(): Move? {
        Log.d("PositionManager", "zmieniam" )
        potentialNewMove?.let { move ->
            val (from, to) = move
            val pieceKind = move.pieceKind

            when (pieceKind) {
                PieceKind.WHITE_ROOK -> {
                    when (from) {
                        Pair(0, 0) -> castleMap[ColorTeam.WHITE]?.left = false
                        Pair(7, 0) -> castleMap[ColorTeam.WHITE]?.right = false
                    }
                }
                PieceKind.BLACK_ROOK -> {
                    when (from) {
                        Pair(0, 7) -> castleMap[ColorTeam.BLACK]?.left = false
                        Pair(7, 7) -> castleMap[ColorTeam.BLACK]?.right = false
                    }
                }
                PieceKind.WHITE_KING -> {
                    castleMap[ColorTeam.WHITE]?.left = false
                    castleMap[ColorTeam.WHITE]?.right = false
                }
                PieceKind.BLACK_KING -> {
                    castleMap[ColorTeam.BLACK]?.left = false
                    castleMap[ColorTeam.BLACK]?.right = false
                }
                else -> {}
            }

            enPassant = when {
                pieceKind == PieceKind.BLACK_PAWN && from.second == 6 && to.second == 4 -> to
                pieceKind == PieceKind.WHITE_PAWN && from.second == 1 && to.second == 3 -> to
                else -> null
            }

            if (enPassant != null) {
                Log.d("PositionManager2", "en_passant możliwe" + enPassant)
            }
        }

        previousPosition = potentialNewPosition
        val tmp = potentialNewMove
        potentialNewMove = null
        _isMoveFound.value = false
        onMove = if(onMove == ColorTeam.WHITE) ColorTeam.BLACK else ColorTeam.WHITE
        return tmp
    }
}