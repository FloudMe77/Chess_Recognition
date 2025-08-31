package pl.dariusz_marecik.chessLens.chessPieces

import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import kotlin.math.abs

class Queen:Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam
    ): Boolean = abstractValidateMove(from, to, positionMap) {
        diff ->
        // diagonal move
        abs(diff.first) == abs(diff.second)
            // vertical move
            || diff.first == 0
            // horizontal move
            || diff.second == 0}

    override fun possibleTake(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        // move (/)
        val possiblePositions = abstractPossibleTake(from, Pair(1, 1), positionMap).toMutableList()
        // move (\)
        possiblePositions += abstractPossibleTake(from, Pair(1, -1), positionMap)
        // move (-)
        possiblePositions += abstractPossibleTake(from, Pair(1, 0), positionMap)
        // move (|)
        possiblePositions += abstractPossibleTake(from, Pair(0, 1), positionMap)
        return possiblePositions
    }

    override fun possibleMove(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>
    ): List<Pair<Int, Int>> {
        // move (/)
        val possiblePositions = abstractPossibleMove(from, Pair(1, 1), positionMap).toMutableList()
        // move (\)
        possiblePositions += abstractPossibleMove(from, Pair(1, -1), positionMap)
        // move (-)
        possiblePositions += abstractPossibleMove(from, Pair(1, 0), positionMap)
        // move (|)
        possiblePositions += abstractPossibleMove(from, Pair(0, 1), positionMap)
        return possiblePositions
    }
}