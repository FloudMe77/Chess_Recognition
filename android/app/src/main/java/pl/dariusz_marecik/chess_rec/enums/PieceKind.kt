package pl.dariusz_marecik.chess_rec.enums

// Represents all chess piece types, with unique IDs and PGN/FEN symbols
enum class PieceKind(val id: Int) {
    WHITE_PAWN(0),
    WHITE_ROOK(1),
    WHITE_KNIGHT(2),
    WHITE_BISHOP(3),
    WHITE_QUEEN(4),
    WHITE_KING(5),
    BLACK_PAWN(6),
    BLACK_ROOK(7),
    BLACK_KNIGHT(8),
    BLACK_BISHOP(9),
    BLACK_QUEEN(10),
    BLACK_KING(11);

    // Returns the PGN symbol for this piece (empty for pawns)
    fun getSymbolPgn(): String {
        return when (this) {
            WHITE_PAWN, BLACK_PAWN -> ""
            WHITE_ROOK, BLACK_ROOK -> "R"
            WHITE_KNIGHT, BLACK_KNIGHT -> "N"
            WHITE_BISHOP, BLACK_BISHOP -> "B"
            WHITE_QUEEN, BLACK_QUEEN -> "Q"
            WHITE_KING, BLACK_KING -> "K"
        }
    }

    // Returns the FEN symbol for this piece (lowercase for black)
    fun getSymbolFen(): String {
        return when (this) {
            WHITE_PAWN, BLACK_PAWN -> "p"
            WHITE_ROOK, BLACK_ROOK -> "r"
            WHITE_KNIGHT, BLACK_KNIGHT -> "n"
            WHITE_BISHOP, BLACK_BISHOP -> "b"
            WHITE_QUEEN, BLACK_QUEEN -> "q"
            WHITE_KING, BLACK_KING -> "k"
        }
    }
}
