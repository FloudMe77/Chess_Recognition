package pl.dariusz_marecik.chessLens.utils

import pl.dariusz_marecik.chessLens.enums.Action
import pl.dariusz_marecik.chessLens.enums.PieceKind

// Represents a chess move including start and end positions, piece type, and action
data class Move(
    val from: Pair<Int, Int>,   // Starting coordinates of the move
    val to: Pair<Int, Int>,     // Ending coordinates of the move
    val pieceKind: PieceKind,   // Type of the moving piece
    val action: Action          // Action type (move, take, promotion, castle, en passant)
)
