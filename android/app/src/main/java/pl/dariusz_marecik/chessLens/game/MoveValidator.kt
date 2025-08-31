package pl.dariusz_marecik.chessLens.game

import pl.dariusz_marecik.chessLens.enums.Action
import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.enums.PieceKind
import pl.dariusz_marecik.chessLens.utils.Move
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import pl.dariusz_marecik.chessLens.utils.minus
import pl.dariusz_marecik.chessLens.utils.plus
import kotlin.math.abs

// Validates and identifies legal chess moves including castling, en passant, captures, and regular moves.
class MoveValidator(
    private val castlingManager: CastlingManager,
    private val enPassantManager: EnPassantManager
) {
    fun findLegalMove(
        previousPosition: Map<Pair<Int, Int>, PieceInfo>,
        detectedPosition: Map<Pair<Int, Int>, PieceInfo>,
        fromCordsSet: Set<Pair<Int, Int>>,
        toCordsSet: Set<Pair<Int, Int>>,
        onMove: ColorTeam
    ): Move? {
        return when {
            // Castling
            fromCordsSet.size == 2 && toCordsSet.size == 2 -> findCastlingMove(
                previousPosition, fromCordsSet, toCordsSet, onMove
            )
            // Regular move
            fromCordsSet.size == 1 && toCordsSet.size == 1 -> findRegularMove(
                previousPosition, fromCordsSet, toCordsSet, onMove
            )
            // Capture move
            fromCordsSet.size == 1 && toCordsSet.isEmpty() -> findCaptureMove(
                previousPosition, detectedPosition, fromCordsSet, onMove
            )
            // En passant
            fromCordsSet.size == 2 && toCordsSet.size == 1 -> findEnPassantMove(
                previousPosition, fromCordsSet, toCordsSet, onMove
            )
            else -> null
        }
    }

    private fun pawnPromotion(piece: PieceInfo, to: Pair<Int, Int>): Boolean =
        (piece.name == PieceKind.WHITE_PAWN && to.second == 7) ||
                (piece.name == PieceKind.BLACK_PAWN && to.second == 0)

    private fun findCastlingMove(
        previousPosition: Map<Pair<Int, Int>, PieceInfo>,
        fromCordsSet: Set<Pair<Int, Int>>,
        toCordsSet: Set<Pair<Int, Int>>,
        onMove: ColorTeam
    ): Move? {
        val (wantedRook, wantedKing, row) = when (onMove) {
            ColorTeam.WHITE -> Triple(PieceKind.WHITE_ROOK, PieceKind.WHITE_KING, 0)
            ColorTeam.BLACK -> Triple(PieceKind.BLACK_ROOK, PieceKind.BLACK_KING, 7)
        }

        val kingPos = fromCordsSet.firstOrNull { previousPosition[it]?.name == wantedKing }
        val rookPos = fromCordsSet.firstOrNull { previousPosition[it]?.name == wantedRook }

        if (kingPos == Pair(4, row) && rookPos != null) {
            val rook = previousPosition[rookPos]
            val leftCastle = castlingManager.canCastleLeft(onMove) &&
                    rookPos == Pair(0, row) &&
                    toCordsSet == setOf(Pair(2, row), Pair(3, row))
            val rightCastle = castlingManager.canCastleRight(onMove) &&
                    rookPos == Pair(7, row) &&
                    toCordsSet == setOf(Pair(5, row), Pair(6, row))

            if (rook != null) {
                when {
                    leftCastle && rook.movement.validateMove(Pair(0, row), Pair(3, row), previousPosition, onMove) ->
                        return Move(kingPos, Pair(2, row), wantedKing, Action.CASTLE_LEFT)
                    rightCastle && rook.movement.validateMove(Pair(7, row), Pair(5, row), previousPosition, onMove) ->
                        return Move(kingPos, Pair(6, row), wantedKing, Action.CASTLE_RIGHT)
                }
            }
        }
        return null
    }

    private fun findRegularMove(
        previousPosition: Map<Pair<Int, Int>, PieceInfo>,
        fromCordsSet: Set<Pair<Int, Int>>,
        toCordsSet: Set<Pair<Int, Int>>,
        onMove: ColorTeam
    ): Move? {
        val from = fromCordsSet.single()
        val to = toCordsSet.single()
        val piece = previousPosition[from]

        if (piece != null && !previousPosition.containsKey(to) && piece.color == onMove &&
            piece.movement.validateMove(from, to, previousPosition, piece.color)
        ) {
            return if (pawnPromotion(piece, to)) Move(from, to, piece.name, Action.PROMOTION)
            else Move(from, to, piece.name, Action.MOVE)
        }
        return null
    }

    private fun findCaptureMove(
        previousPosition: Map<Pair<Int, Int>, PieceInfo>,
        detectedPosition: Map<Pair<Int, Int>, PieceInfo>,
        fromCordsSet: Set<Pair<Int, Int>>,
        onMove: ColorTeam
    ): Move? {
        val from = fromCordsSet.single()
        previousPosition[from]?.let { piece ->
            if (piece.color == onMove) {
                val possibleTakes = piece.movement.possibleTake(from, previousPosition, piece.color)
                possibleTakes.firstOrNull { pos ->
                    previousPosition[pos]?.color != detectedPosition[pos]?.color
                }?.let { capturePos ->
                    return if (pawnPromotion(piece, capturePos)) {
                        Move(from, capturePos, piece.name, Action.PROMOTION)
                    } else {
                        Move(from, capturePos, piece.name, Action.TAKE)
                    }
                }
            }
        }
        return null
    }

    private fun findEnPassantMove(
        previousPosition: Map<Pair<Int, Int>, PieceInfo>,
        fromCordsSet: Set<Pair<Int, Int>>,
        toCordsSet: Set<Pair<Int, Int>>,
        onMove: ColorTeam
    ): Move? {
        val listFrom = fromCordsSet.toList()
        val to = toCordsSet.single()
        val whitePawn = listFrom.find { previousPosition[it]?.name == PieceKind.WHITE_PAWN }
        val blackPawn = listFrom.find { previousPosition[it]?.name == PieceKind.BLACK_PAWN }

        if (whitePawn != null && blackPawn != null) {
            val diff = blackPawn - whitePawn
            val enPassant = enPassantManager.enPassantTarget

            if (onMove == ColorTeam.BLACK && whitePawn == enPassant && abs(diff.first) == 1 && diff.second == 0 &&
                to == whitePawn - Pair(0, 1)
            ) return Move(blackPawn, to, PieceKind.BLACK_PAWN, Action.EN_PASSANT)

            if (onMove == ColorTeam.WHITE && blackPawn == enPassant && abs(diff.first) == 1 && diff.second == 0 &&
                to == blackPawn + Pair(0, 1)
            ) return Move(whitePawn, to, PieceKind.WHITE_PAWN, Action.EN_PASSANT)
        }
        return null
    }
}
