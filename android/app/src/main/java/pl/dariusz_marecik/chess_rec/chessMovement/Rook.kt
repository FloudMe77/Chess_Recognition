package pl.dariusz_marecik.chess_rec.chessPieces

import pl.dariusz_marecik.chess_rec.enums.ColorTeam
import pl.dariusz_marecik.chess_rec.utils.PieceInfo

class Rook: Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam
    ): Boolean = abstractValidateMove(from, to, positionMap) {
        diff ->
        // vertical move
        diff.first == 0
            // horizontal move
            || diff.second == 0
    }

    override fun possibleTake(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        // move (-)
        var possiblePositions = abstractPossibleTake(from, Pair(1, 0), positionMap)
        // move (|)
        possiblePositions += abstractPossibleTake(from, Pair(0, 1), positionMap)
        return possiblePositions
    }

    override fun possibleMove(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>
    ): List<Pair<Int, Int>> {
        // move (-)
        var possiblePositions = abstractPossibleMove(from, Pair(1, 0), positionMap)
        // move (|)
        possiblePositions += abstractPossibleMove(from, Pair(0, 1), positionMap)
        return possiblePositions
    }
}