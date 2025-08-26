package pl.dariusz_marecik.chess_rec.chessPieces

import pl.dariusz_marecik.chess_rec.ColorTeam
import pl.dariusz_marecik.chess_rec.PieceInfo
import pl.dariusz_marecik.chess_rec.plus
import pl.dariusz_marecik.chess_rec.minus
import pl.dariusz_marecik.chess_rec.times
import kotlin.math.abs


class Knight:Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): Boolean{
        val diff = to - from
        return (abs(diff.first) == 2 && abs(diff.second) == 1) || (abs(diff.first) == 1 && abs(diff.second) == 2) }

    override fun possibleTake(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = piecesPosition[from]?.color ?: return possiblePositions
        for(direction in listOf(Pair(1, 2), Pair(2, 1))) {
            for (sgnX in listOf(-1, +1)){
                for(sgnY in listOf(-1, +1)){
                    val newPosition = from + (direction * Pair(sgnX, sgnY))
                    if(isOnMap(newPosition)){
                        val pieceAtNewPos = piecesPosition[newPosition]
                        if (pieceAtNewPos != null && pieceAtNewPos.color != colorOfPiece) {
                            possiblePositions.add(newPosition)
                        }
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
        val possibleCords = mutableListOf<Pair<Int, Int>>()
        for(direction in listOf(Pair(1, 2), Pair(2, 1))) {
            for (sgnX in listOf(-1, +1)){
                for(sgnY in listOf(-1, +1)){
                    val newCords = from + (direction * Pair(sgnX, sgnY))
                    if(isOnMap(newCords) && !piecesPosition.containsKey(newCords)){
                        possibleCords.add(newCords)
                    }
                }
            }
        }
        return possibleCords
    }
}