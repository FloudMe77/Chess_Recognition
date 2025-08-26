package pl.dariusz_marecik.chess_rec.chessPieces

import pl.dariusz_marecik.chess_rec.ColorTeam
import pl.dariusz_marecik.chess_rec.PieceInfo
import kotlin.math.abs

class Queen:Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam
    ): Boolean = abstractValidateMove(from, to, piecesPosition) { diff -> abs(diff.first) == abs(diff.second)
            || diff.first == 0 || diff.second == 0}

    override fun possibleTake(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        val possiblePositions = abstractPossibleTake(from, Pair(1, 1), piecesPosition).toMutableList()
        possiblePositions += abstractPossibleTake(from, Pair(1, -1), piecesPosition)
        possiblePositions += abstractPossibleTake(from, Pair(1, 0), piecesPosition)
        possiblePositions += abstractPossibleTake(from, Pair(0, 1), piecesPosition)
        return possiblePositions
    }

    override fun possibleMove(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>
    ): List<Pair<Int, Int>> {
        TODO("Not yet implemented")
    }
}