package com.example.chessLens.game

import com.example.chessLens.enums.PieceKind
import com.example.chessLens.utils.Move

// Tracks en passant target squares created by pawn double moves.
class EnPassantManager {
    var enPassantTarget: Pair<Int, Int>? = null
        private set

    fun updateEnPassant(move: Move) {
        val (from, to, pieceKind) = move
        enPassantTarget = when {
            pieceKind == PieceKind.BLACK_PAWN && from.second == 6 && to.second == 4 -> to
            pieceKind == PieceKind.WHITE_PAWN && from.second == 1 && to.second == 3 -> to
            else -> null
        }
    }
}