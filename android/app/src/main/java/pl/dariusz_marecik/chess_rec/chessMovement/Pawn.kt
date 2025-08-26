package pl.dariusz_marecik.chess_rec.chessPieces

import pl.dariusz_marecik.chess_rec.*
import kotlin.math.abs

class Pawn:Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam
    ) = abstractValidateMove(from, to, piecesPosition) { diff ->
        if(colorTeam == ColorTeam.WHITE ){
            if(from.second==1) abs(diff.first) == 0 && (diff.second == 2 || diff.second == 1)
            else abs(diff.first) == 0 && diff.second == 1
        }
        else {
            if(from.second==6) abs(diff.first) == 0 && (diff.second == -2 || diff.second == -1)
            else abs(diff.first) == 0 && diff.second == -1
        }}

    override fun possibleTake(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = piecesPosition[from]?.color ?: return possiblePositions
        for (directionX in listOf(-1, +1)) {
            val directionY = if (colorTeam == ColorTeam.WHITE) 1 else -1
            val newPosition = from + (Pair(-1,1) * Pair(directionX, directionY))
            if(isOnMap(newPosition)){
                val pieceAtNewPos = piecesPosition[newPosition]
                if (pieceAtNewPos != null && pieceAtNewPos.color != colorOfPiece) {
                    possiblePositions.add(newPosition)
                }
            }
        }
        return possiblePositions
    }

    override fun possibleMove(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = piecesPosition[from]?.color ?: return possiblePositions
        for (directionX in listOf(-1, +1)) {
            val directionY = if (colorOfPiece == ColorTeam.WHITE) 1 else -1
            val newPosition = from + (Pair(-1,1) * Pair(directionX, directionY))
            if(isOnMap(newPosition)){
                val pieceAtNewPos = piecesPosition[newPosition]
                if (pieceAtNewPos != null && pieceAtNewPos.color != colorOfPiece) {
                    possiblePositions.add(newPosition)
                }
            }
        }
        return possiblePositions
    }
}