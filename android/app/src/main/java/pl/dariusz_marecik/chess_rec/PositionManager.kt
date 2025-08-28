package pl.dariusz_marecik.chess_rec

import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Modifier.PRIVATE
import kotlin.collections.minus
import kotlin.math.abs

class PositionManager(startPosition: Map<Pair<Int, Int>, PieceInfo>, startingPlayerColor: ColorTeam = ColorTeam.WHITE) {
    private var _positionToDraw = MutableStateFlow<Map<Pair<Int, Int>, PieceInfo>>(emptyMap())
    val positionToDraw: StateFlow<Map<Pair<Int, Int>, PieceInfo>> = _positionToDraw.asStateFlow()

    private var _isMoveFound = MutableStateFlow(false)
    val isMoveFound: StateFlow<Boolean> = _isMoveFound.asStateFlow()

    private var _isMate = MutableStateFlow(false)
    val isMate: StateFlow<Boolean> = _isMate.asStateFlow()

    private var _isStaleMate = MutableStateFlow(false)
    val isStaleMate: StateFlow<Boolean> = _isStaleMate.asStateFlow()

    private val castleMap: Map<ColorTeam, CastleInfo>
    init {
        _positionToDraw.value = startPosition
        castleMap = mapOf(
            ColorTeam.WHITE to CastleInfo(),
            ColorTeam.BLACK to CastleInfo()
        )
    }

    private var previousPosition: Map<Pair<Int, Int>,PieceInfo> = startPosition
    var onMove: ColorTeam = startingPlayerColor
        private set

    private var enPassant: Pair<Int, Int>? = null
    private var potentialNewPosition: Map<Pair<Int, Int>,PieceInfo> = startPosition
    var potentialNewMove: Move? = null
        private set

    fun considerNewPosition(detectedPosition: Map<Pair<Int, Int>, PieceInfo>) {
//        Log.d("PositionManager", "Otrzymałem newPieces: $detectedPosition")
        val fromCordsSet = previousPosition.keys - detectedPosition.keys
        val toCordsSet = detectedPosition.keys - previousPosition.keys
//        Log.d("PositionManager", "fromPositions: $fromCordsSet, toPositions: $toCordsSet")
//        Log.d("PositionManager", "onMove: $onMove")

        if(fromCordsSet.isEmpty() && toCordsSet.isEmpty()) {
            // undo changes
            potentialNewMove = null
            _isMoveFound.value = false
            potentialNewPosition = previousPosition
            _positionToDraw.value = previousPosition
        }

        val potentialMove = findLegalMove(detectedPosition, fromCordsSet, toCordsSet) ?: return

        val (from, to, _, label) = potentialMove
        if(potentialMove == potentialNewMove) return
        //
        val newPosition: MutableMap<Pair<Int, Int>, PieceInfo> =
            previousPosition.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()

        fun applyMove(update: () -> Unit) {
            update()
            if (!isKingAttacked(newPosition)) {
                _isMoveFound.value = true
                potentialNewPosition = newPosition
                potentialNewMove = potentialMove
                _positionToDraw.value = newPosition
            }
        }

        when (label) {
            Action.MOVE -> {
                newPosition.remove(from)?.let { piece ->
                    applyMove { movePositionMod(newPosition, piece, to) }
                }
            }
            Action.PROMOTION -> {
                newPosition.remove(from)?.let { piece ->
                    applyMove {
                        promotionPositionMod(newPosition, piece, to)
                    }
                }
            }
            Action.TAKE -> {
                newPosition.remove(from)?.let { piece ->
                    applyMove {
                        takePositionMod(newPosition, piece, to)
                    }
                }
            }
            Action.EN_PASSANT -> {
                newPosition.remove(from)?.let { piece ->
                    applyMove {
                        enPassantPositionMod(newPosition, piece, from, to)
                    }
                }
            }
            Action.CASTLE_LEFT, Action.CASTLE_RIGHT -> {
                newPosition[from]?.let { king ->
                    val row = if (onMove == ColorTeam.WHITE) 0 else 7
                    val rookFrom = if (label == Action.CASTLE_LEFT) Pair(0, row) else Pair(7, row)
                    val rookTo = if (label == Action.CASTLE_LEFT) Pair(3, row) else Pair(5, row)

                    newPosition[rookFrom]?.let { rook ->
                        applyMove {
                            castlePositionMod(newPosition, king, from, to, rook, rookFrom, rookTo)
                        }
                    }
                }
            }
        }
    }

    private fun castlePositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        king: PieceInfo,
        fromKing: Pair<Int, Int>,
        toKing: Pair<Int, Int>,
        rook: PieceInfo,
        rookFrom: Pair<Int, Int>,
        rookTo: Pair<Int, Int>
    ) {
        newPosition.remove(fromKing)
        newPosition.remove(rookFrom)
        king.cords = toKing
        rook.cords = rookTo
        newPosition[king.cords] = king
        newPosition[rook.cords] = rook
    }

    private fun enPassantPositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        from: Pair<Int, Int>,
        to: Pair<Int, Int>
    ) {
        val capturePos = Pair(to.first, from.second)
        newPosition.keys.removeAll(listOfNotNull(from, capturePos).toSet())
        piece.cords = to
        newPosition[to] = piece
    }

    private fun takePositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        to: Pair<Int, Int>
    ) {
        newPosition.keys.removeAll(listOfNotNull(piece.cords, to).toSet())
        piece.cords = to
        newPosition[to] = piece
    }

    private fun promotionPositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        to: Pair<Int, Int>
    ) {
        val promoted = PieceInfo(piece.id + 4, piece.position, piece.bbox)
        promoted.cords = to
        newPosition[to] = promoted
    }

    private fun movePositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        to: Pair<Int, Int>
    ) {
        piece.cords = to; newPosition[to] = piece
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun findLegalMove(
        detectedPosition: Map<Pair<Int, Int>, PieceInfo>,
        fromCordsSet: Set<Pair<Int, Int>>,
        toCordsSet: Set<Pair<Int, Int>>
    ): Move? {

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
                            return Move(kingPos, Pair(2,row), wantedKing, Action.CASTLE_LEFT)
                        rightCastle && rook.movement.validateMove(Pair(7,row), Pair(5,row), previousPosition, onMove) ->
                            return Move(kingPos, Pair(6,row), wantedKing, Action.CASTLE_RIGHT)
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
                return if (pawnPromotion(piece, to)) Move(from, to, piece.name, Action.PROMOTION)
                else Move(from, to, piece.name ,Action.MOVE)
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
                        return if (pawnPromotion(piece, capturePos)) Move(from, capturePos, piece.name, Action.PROMOTION)
                        else Move(from, capturePos, piece.name, Action.TAKE)
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
                    to == whitePawn - Pair(0,1)) return Move(blackPawn, to, PieceKind.BLACK_PAWN, Action.EN_PASSANT)
                if (onMove == ColorTeam.WHITE && blackPawn == enPassant && abs(diff.first) == 1 && diff.second == 0 &&
                    to == blackPawn + Pair(0,1)) return Move(whitePawn,to, PieceKind.WHITE_PAWN, Action.EN_PASSANT)
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

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun haveAnyLegalMove(positionToCheck: Map<Pair<Int, Int>,PieceInfo>): Boolean{
        println(positionToCheck.values.toString())
        for(piece in positionToCheck.values){
            if(piece.color == onMove){
                //move
                for(possibleToCord in piece.movement.possibleMove(piece.cords, positionToCheck)){
                    val newPosition: MutableMap<Pair<Int, Int>, PieceInfo> =
                        positionToCheck.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()

                    newPosition.remove(piece.cords)?.let {
                        movePositionMod(newPosition, it, possibleToCord)
                    }

                    if(!isKingAttacked(newPosition, onMove)){
                        return true
                    }
                }
                //take
                for(possibleToCord in piece.movement.possibleTake(piece.cords, positionToCheck, onMove)){
                    val newPosition: MutableMap<Pair<Int, Int>, PieceInfo> =
                        positionToCheck.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
                    newPosition.remove(piece.cords)?.let {
                        takePositionMod(newPosition, it, possibleToCord)
                    }
                    if(!isKingAttacked(newPosition, onMove)){
                        return true
                    }
                }
                // enPassant
                enPassant?.let { ep ->
                    val yDirection = if (onMove == ColorTeam.WHITE) 1 else -1

                    listOf(-1, 1).forEach { xDirection ->
                        val pawnTakerCords = ep + Pair(xDirection, 0)
                        val pawn = positionToCheck[pawnTakerCords] ?: return@forEach

                        val correctPawn = (pawn.name == PieceKind.BLACK_PAWN && onMove == ColorTeam.BLACK) ||
                                (pawn.name == PieceKind.WHITE_PAWN && onMove == ColorTeam.WHITE)
                        if (!correctPawn) return@forEach

                        val newPosition = positionToCheck.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
                        newPosition.remove(pawn.cords)?.let { removedPawn ->
                            enPassantPositionMod(newPosition, removedPawn, removedPawn.cords, ep + Pair(0, yDirection))
                        }

                        if (!isKingAttacked(newPosition, onMove)) {
                            return true
                        }
                    }
                }
            }

        }
        return false
    }


    fun acceptNewState(): Move? {
//        Log.d("PositionManager", "zmieniam" )
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

//            if (enPassant != null) {
//                Log.d("PositionManager2", "en_passant możliwe" + enPassant)
//            }
        }

        previousPosition = potentialNewPosition
        val tmp = potentialNewMove
        potentialNewMove = null
        _isMoveFound.value = false
        onMove = if(onMove == ColorTeam.WHITE) ColorTeam.BLACK else ColorTeam.WHITE
        // sprawdzanie mata
        Log.d("endGame", "Sprawdzam " + onMove)
        if(!haveAnyLegalMove(previousPosition)){
            if(isKingAttacked(previousPosition)){
                Log.d("endGame", "MATE")
                _isMate.value = true
            }
            else{
                Log.d("endGame", "STALEMATE")
                _isStaleMate.value = true
            }
        }
        return tmp
    }
}