package pl.dariusz_marecik.chessLens.chessPieces

import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import kotlin.math.abs

class Bishop : Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam
    ): Boolean = abstractValidateMove(from, to, positionMap) {
        // diagonal move check
        diff -> abs(diff.first) == abs(diff.second)
    }

    override fun possibleTake(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        // diagonal (/)
        val possiblePositions = abstractPossibleTake(from, Pair(1, 1), positionMap)
        // diagonal (\)
        return possiblePositions + abstractPossibleTake(from, Pair(1, -1), positionMap)
    }
    override fun possibleMove(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
    ): List<Pair<Int, Int>> {
        // diagonal (/)
        val possiblePositions = abstractPossibleMove(from, Pair(1, 1), positionMap)
        // diagonal (\)
        return possiblePositions + abstractPossibleMove(from, Pair(1, -1), positionMap)
    }
}