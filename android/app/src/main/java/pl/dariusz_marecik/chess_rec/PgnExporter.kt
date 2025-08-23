package pl.dariusz_marecik.chess_rec

import java.io.File

object PgnExporter {

    fun export(moves: List<Move>, startPosition: Map<Pair<Int, Int>, PieceInfo>): String {
        val sb = StringBuilder()

        // Minimalne metadane PGN
        sb.appendLine("[Event \"Custom Game\"]")
        sb.appendLine("[Site \"Local\"]")
        sb.appendLine("[Date \"2025.08.21\"]")
        sb.appendLine("[Round \"1\"]")
        sb.appendLine("[White \"Player1\"]")
        sb.appendLine("[Black \"Player2\"]")
        sb.appendLine("[Result \"*\"]")
        sb.appendLine("[Variant \"From Position\"]")
        sb.appendLine("[FEN \"${FenConverter.mapToFen(startPosition)}\"]")

        sb.appendLine()

        moves.forEachIndexed { index, move ->
            // Numer ruchu tylko dla białych
            if (move.pieceKind.name.startsWith("WHITE")) {
                sb.append("${index / 2 + 1}. ")
            }
            else{
                sb.append("${index / 2 + 1}... ")
            }
            sb.append(moveToLan(move))
            sb.append(" ")
        }

        // Zapis do pliku
        return sb.toString()
    }

    private fun moveToLan(move: Move): String {
        val from = toSquare(move.from)
        val to = toSquare(move.to)

        return when (move.action) {
            Action.CASTLE_LEFT -> "O-O-O"
            Action.CASTLE_RIGHT -> "O-O"
            Action.EN_PASSANT -> "${from}${to}"
            Action.TAKE -> "${move.pieceKind.getSymbolPgn()}${from}x${to}"
            Action.MOVE -> "${move.pieceKind.getSymbolPgn()}${from}${to}"
            Action.PROMOTION -> "${from}${to}Q"
        }
    }

    private fun toSquare(pos: Pair<Int, Int>): String {
        val file = 'a' + pos.first
        val rank = pos.second + 1
        return "$file$rank"
    }
}
