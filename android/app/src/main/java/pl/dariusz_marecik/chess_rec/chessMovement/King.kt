package pl.dariusz_marecik.chess_rec.chessPieces

import pl.dariusz_marecik.chess_rec.ColorTeam
import pl.dariusz_marecik.chess_rec.PieceInfo
import pl.dariusz_marecik.chess_rec.plus
import kotlin.math.abs

class King: Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ) = abstractValidateMove(from, to, piecesPosition) { diff -> abs(diff.first) < 2 && abs(diff.second) < 2 }

    override fun possibleTake(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = piecesPosition[from]?.color ?: return possiblePositions
        for (i in -1..1){
            for (j in -1..1){
                if(i==j && i==0) continue
                val versor = Pair(i, j)
                val newPosition = from + versor
                if(isOnMap(newPosition)){
                    val pieceAtNewPos = piecesPosition[newPosition]
                    if (pieceAtNewPos != null && pieceAtNewPos.color != colorOfPiece) {
                        possiblePositions.add(newPosition)
                    }
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
        for (i in -1..1){
            for (j in -1..1){
                if(i==j && i==0) continue
                val versor = Pair(i, j)
                val newPosition = from + versor
                if(isOnMap(newPosition) && !piecesPosition.containsKey(newPosition)){
                    possiblePositions.add(newPosition)
                }
            }
        }
        return possiblePositions
    }
}