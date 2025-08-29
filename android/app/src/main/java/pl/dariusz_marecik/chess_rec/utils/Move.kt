package pl.dariusz_marecik.chess_rec.utils

import pl.dariusz_marecik.chess_rec.enums.Action
import pl.dariusz_marecik.chess_rec.enums.PieceKind

data class Move(
    val from: Pair<Int, Int>,
    val to: Pair<Int, Int>,
    val pieceKind: PieceKind,
    val action: Action
)
