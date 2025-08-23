package pl.dariusz_marecik.chess_rec

import pl.dariusz_marecik.chess_rec.chessPieces.*

class PieceInfo(
    val id: Int,
    var position: String,
    val bbox: List<Double>
) {
    var cords: Pair<Int, Int>
        get() = Pair(position[0].code - 'a'.code, position[1].code - '1'.code)
        set(value) {
            position = "${'a' + value.first}${'1' + value.second}"
        }

    val name: PieceKind get() = PieceKind.entries.first { it.id == id }
    val color: ColorTeam get() = if (id < 6) ColorTeam.WHITE else ColorTeam.BLACK
    val movement: Piece
        get() = when (id % 6) {
            0 -> Pawn()
            1 -> Rook()
            2 -> Knight()
            3 -> Bishop()
            4 -> Queen()
            5 -> King()
            else -> error("Invalid id")
        }

    fun deepCopy(): PieceInfo =
        PieceInfo(id, position, ArrayList(bbox))


    override fun toString(): String {
        return "($name, $position)"
    }
}
