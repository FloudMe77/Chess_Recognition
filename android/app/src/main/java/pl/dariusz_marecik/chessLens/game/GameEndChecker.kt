package pl.dariusz_marecik.chessLens.game

import android.util.Log
import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.enums.GameEndResult
import pl.dariusz_marecik.chessLens.enums.PieceKind
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import pl.dariusz_marecik.chessLens.utils.plus

// Detects checkmate and stalemate conditions by checking king attacks and available legal moves.
class GameEndChecker(
    private val enPassantManager: EnPassantManager,
    private val positionModifier: PositionModifier
) {
    fun isKingAttacked(positionMap: Map<Pair<Int, Int>, PieceInfo>, color: ColorTeam): Boolean {
        val king = positionMap.values.find {
            (color == ColorTeam.WHITE && it.name == PieceKind.WHITE_KING) ||
                    (color == ColorTeam.BLACK && it.name == PieceKind.BLACK_KING)
        } ?: return false

        return positionMap.values.any {
            it.color != color && king.cords in it.movement.possibleTake(it.cords, positionMap, it.color)
        }
    }

    fun haveAnyLegalMove(positionToCheckMap: Map<Pair<Int, Int>, PieceInfo>, onMove: ColorTeam): Boolean {
        for (piece in positionToCheckMap.values) {
            if (piece.color == onMove) {
                // Check regular moves
                if (hasLegalRegularMoves(piece, positionToCheckMap, onMove)) return true
                // Check captures
                if (hasLegalCaptures(piece, positionToCheckMap, onMove)) return true
                // Check en passant
                if (hasLegalEnPassant(piece, positionToCheckMap, onMove)) return true
            }
        }
        return false
    }

    private fun hasLegalRegularMoves(
        piece: PieceInfo,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        onMove: ColorTeam
    ): Boolean {
        for (possibleToCord in piece.movement.possibleMove(piece.cords, positionMap)) {
            val newPosition = positionMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
            newPosition.remove(piece.cords)?.let {
                positionModifier.movePositionMod(newPosition, it, possibleToCord)
            }
            if (!isKingAttacked(newPosition, onMove)) return true
        }
        return false
    }

    private fun hasLegalCaptures(
        piece: PieceInfo,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        onMove: ColorTeam
    ): Boolean {
        for (possibleToCord in piece.movement.possibleTake(piece.cords, positionMap, onMove)) {
            val newPosition = positionMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
            newPosition.remove(piece.cords)?.let {
                positionModifier.takePositionMod(newPosition, it, possibleToCord)
            }
            if (!isKingAttacked(newPosition, onMove)) return true
        }
        return false
    }

    private fun hasLegalEnPassant(
        piece: PieceInfo,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        onMove: ColorTeam
    ): Boolean {
        enPassantManager.enPassantTarget?.let { ep ->
            val yDirection = if (onMove == ColorTeam.WHITE) 1 else -1

            listOf(-1, 1).forEach { xDirection ->
                val pawnTakerCords = ep + Pair(xDirection, 0)
                val pawn = positionMap[pawnTakerCords] ?: return@forEach

                val correctPawn = (pawn.name == PieceKind.BLACK_PAWN && onMove == ColorTeam.BLACK) ||
                        (pawn.name == PieceKind.WHITE_PAWN && onMove == ColorTeam.WHITE)
                if (!correctPawn) return@forEach

                val newPosition = positionMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
                newPosition.remove(pawn.cords)?.let { removedPawn ->
                    positionModifier.enPassantPositionMod(newPosition, removedPawn, removedPawn.cords, ep + Pair(0, yDirection))
                }

                if (!isKingAttacked(newPosition, onMove)) return true
            }
        }
        return false
    }

    fun checkGameEnd(positionMap: Map<Pair<Int, Int>, PieceInfo>, onMove: ColorTeam): GameEndResult {
        if (!haveAnyLegalMove(positionMap, onMove)) {
            return if (isKingAttacked(positionMap, onMove)) {
                Log.d("endGame", "MATE")
                GameEndResult.MATE
            } else {
                Log.d("endGame", "STALEMATE")
                GameEndResult.STALEMATE
            }
        }
        return GameEndResult.CONTINUE
    }
}
