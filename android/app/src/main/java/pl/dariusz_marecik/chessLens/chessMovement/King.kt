package pl.dariusz_marecik.chessLens.chessPieces

import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import pl.dariusz_marecik.chessLens.utils.plus
import kotlin.math.abs

class King: Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ) = abstractValidateMove(from, to, positionMap) {
        // move in distance equals 1
        diff -> abs(diff.first) < 2 && abs(diff.second) < 2
    }

    override fun possibleTake(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        val colorOfPiece = positionMap[from]?.color ?: return mutableListOf()
        // for every place where distance equals 1
        val directions = (-1..1).flatMap { i -> (-1..1).map { j -> Pair(i, j) } }
            .filter { it != Pair(0, 0) }

        val possiblePositions = directions
            .map { from + it }
            .filter { isOnMap(it) }
            .filter {
                // color different then taken piece
                positionMap[it]?.color != colorOfPiece && positionMap[it] != null
            }

        return possiblePositions
    }
    override fun possibleMove(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
    ): List<Pair<Int, Int>> {
        // for every place where distance equals 1
        val directions = (-1..1).flatMap { i -> (-1..1).map { j -> Pair(i, j) } }
            .filter { it != Pair(0, 0) }

        val possiblePositions = directions
            .map { from + it }
            // place is free
            .filter { isOnMap(it) && !positionMap.containsKey(it) }

        return possiblePositions
    }
}