
package pl.dariusz_marecik.chess_rec

object FenConverter {

    fun mapToFen(piecesMap: Map<Pair<Int, Int>, PieceInfo>): String {
        val rows = mutableListOf<String>()

        for (rank in 7 downTo 0) {
            var rowFen = ""
            var emptyCount = 0
            for (file in 0..7) {
                val pieceInfo = piecesMap[Pair(file, rank)]
                if (pieceInfo != null) {
                    if (emptyCount > 0) {
                        rowFen += emptyCount.toString()
                        emptyCount = 0
                    }
                    rowFen += pieceToFenChar(pieceInfo)
                } else {
                    emptyCount++
                }
            }
            if (emptyCount > 0) rowFen += emptyCount

            rows.add(rowFen)
        }
        return rows.joinToString("/") + " w KQkq - 0 1"
    }

    private fun pieceToFenChar(pieceInfo: PieceInfo): String {
        val symbol = pieceInfo.name.getSymbolFen()
        return if (pieceInfo.color == ColorTeam.WHITE) symbol.uppercase() else symbol.lowercase()
    }
}

