package pl.dariusz_marecik.chess_rec

data class Move(
    val from: Pair<Int, Int>,
    val to: Pair<Int, Int>,
    val pieceKind: PieceKind,
    val action: Action
)
