package pl.dariusz_marecik.chess_rec.serialization

import pl.dariusz_marecik.chess_rec.enums.Action
import pl.dariusz_marecik.chess_rec.utils.Move
import pl.dariusz_marecik.chess_rec.utils.PieceInfo

object PgnExporter {

    // Exports a list of moves and a starting position to PGN format
    fun export(moves: List<Move>, startPositionMap: Map<Pair<Int, Int>, PieceInfo>): String {
        val sb = StringBuilder()

        // Minimal PGN metadata
        sb.appendLine("[Event \"Custom Game\"]")
        sb.appendLine("[Site \"Local\"]")
        sb.appendLine("[Date \"2025.08.21\"]")
        sb.appendLine("[Round \"1\"]")
        sb.appendLine("[White \"Player1\"]")
        sb.appendLine("[Black \"Player2\"]")
        sb.appendLine("[Result \"*\"]")
        sb.appendLine("[Variant \"From Position\"]")
        sb.appendLine("[FEN \"${FenConverter.mapToFen(startPositionMap)}\"]")

        sb.appendLine()

        // Convert moves to LAN (Long Algebraic Notation) and add move numbers
        moves.forEachIndexed { index, move ->
            if (move.pieceKind.name.startsWith("WHITE")) {
                sb.append("${index / 2 + 1}. ")
            } else {
                sb.append("${index / 2 + 1}... ")
            }
            sb.append(moveToLan(move))
            sb.append(" ")
        }

        return sb.toString()
    }

    // Converts a single move to LAN notation
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

    // Converts board coordinates to algebraic square (e.g., a1, e4)
    private fun toSquare(pos: Pair<Int, Int>): String {
        val file = 'a' + pos.first
        val rank = pos.second + 1
        return "$file$rank"
    }
}
