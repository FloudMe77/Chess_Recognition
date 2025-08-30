package pl.dariusz_marecik.chess_rec.utils

import pl.dariusz_marecik.chess_rec.chessPieces.*
import pl.dariusz_marecik.chess_rec.enums.ColorTeam
import pl.dariusz_marecik.chess_rec.enums.PieceKind

// Holds information about a chess piece, including its type, color, position, and movement logic
class PieceInfo(
    val id: Int,               // Unique ID representing piece type
    var position: String,      // Algebraic notation of position, e.g., "e4"
    val bbox: List<Double>     // Bounding box from image recognition
) {
    // Converts position string to board coordinates (0..7, 0..7)
    var cords: Pair<Int, Int>
        get() = Pair(position[0].code - 'a'.code, position[1].code - '1'.code)
        set(value) {
            position = "${'a' + value.first}${'1' + value.second}"
        }

    // Gets the piece kind from the ID
    val name: PieceKind get() = PieceKind.entries.first { it.id == id }

    // Determines the piece color based on ID
    val color: ColorTeam get() = if (id < 6) ColorTeam.WHITE else ColorTeam.BLACK

    // Returns movement logic object corresponding to piece type
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

    // Creates a deep copy of this PieceInfo
    fun deepCopy(): PieceInfo =
        PieceInfo(id, position, ArrayList(bbox))

    override fun toString(): String {
        return "($name, $position)"
    }
}
