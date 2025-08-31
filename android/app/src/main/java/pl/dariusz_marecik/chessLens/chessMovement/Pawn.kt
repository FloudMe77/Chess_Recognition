package pl.dariusz_marecik.chessLens.chessPieces

import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import pl.dariusz_marecik.chessLens.utils.plus
import pl.dariusz_marecik.chessLens.utils.times
import kotlin.math.abs

class Pawn:Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam
    ) = abstractValidateMove(from, to, positionMap) { diff ->
        if(colorTeam == ColorTeam.WHITE ){
            // two squares move
            if(from.second==1) abs(diff.first) == 0 && (diff.second == 2 || diff.second == 1)
            // one squares move
            else abs(diff.first) == 0 && diff.second == 1
        }
        else {
            // two squares move
            if(from.second==6) abs(diff.first) == 0 && (diff.second == -2 || diff.second == -1)
            // one squares move
            else abs(diff.first) == 0 && diff.second == -1
        }}

    override fun possibleTake(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = positionMap[from]?.color ?: return possiblePositions
        for (directionX in listOf(-1, +1)) {
            val directionY = if (colorTeam == ColorTeam.WHITE) 1 else -1
            val newPosition = from + (Pair(-1,1) * Pair(directionX, directionY))
            if(isOnMap(newPosition)){
                positionMap[newPosition]?.let {
                    if(it.color != colorOfPiece){
                        possiblePositions.add(newPosition)
                    }
                }
            }
        }
        return possiblePositions
    }

    override fun possibleMove(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = positionMap[from]?.color ?: return possiblePositions
        val directionY = if (colorOfPiece == ColorTeam.WHITE) 1 else -1
        var newCords = from + Pair(0, directionY)

        // one squares move
        if(isOnMap(newCords) && !positionMap.containsKey(newCords)){
            possiblePositions.add(newCords)
        }

        // two squares move
        if((colorOfPiece == ColorTeam.WHITE && from.second == 1)||(colorOfPiece == ColorTeam.BLACK && from.second == 6)){
            newCords = from + Pair(0, directionY * 2)
            if(isOnMap(newCords) && !positionMap.containsKey(newCords)){
                possiblePositions.add(newCords)
            }
        }
        return possiblePositions
    }
}