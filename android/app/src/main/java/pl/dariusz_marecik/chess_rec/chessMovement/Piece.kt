package pl.dariusz_marecik.chess_rec.chessPieces

import pl.dariusz_marecik.chess_rec.*
import kotlin.math.abs
import kotlin.math.max

interface Piece {

    fun validateMove(from: Pair<Int, Int>, to: Pair<Int, Int>, piecesPosition:Map<Pair<Int, Int>,PieceInfo>, colorTeam: ColorTeam): Boolean

    fun possibleTake(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>>
    fun possibleMove(
        from: Pair<Int, Int>,
        piecesPosition: Map<Pair<Int, Int>, PieceInfo>,
    ): List<Pair<Int, Int>>

    // czy to był legalny ruch bez zbicia?
    fun abstractValidateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        previousPieces: Map<Pair<Int, Int>,PieceInfo>,
        isValidDirection: (Pair<Int, Int>) -> Boolean,
    ): Boolean {
        val diff = to - from
        if(diff == 0 to 0) return false
        if (!isValidDirection(diff)) return false
        val len = max(abs(diff.first), abs(diff.second))
        val step = diff / len
        var pos = from + step

        while (pos != to) {

            if (previousPieces.containsKey(pos)) return false
            pos += step
        }

        return true
    }
    // jakie pola mógła zbić dana figura
    fun abstractPossibleTake(
        from: Pair<Int, Int>,
        versor: Pair<Int, Int>,
        previousPieces: Map<Pair<Int, Int>, PieceInfo>
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = previousPieces[from]?.color ?: return possiblePositions

        for (direction in listOf(-1, +1)) {
            var newPosition = from + (versor * direction)
            while (isOnMap(newPosition) && !previousPieces.containsKey(newPosition)) {
                newPosition += (versor * direction)
            }
            if (isOnMap(newPosition)) {
                val pieceAtNewPos = previousPieces[newPosition]
                if (pieceAtNewPos != null && pieceAtNewPos.color != colorOfPiece) {
                    possiblePositions.add(newPosition)
                }
            }
        }
        return possiblePositions
    }

    fun abstractPossibleMove(
        from: Pair<Int, Int>,
        versor: Pair<Int, Int>,
        previousPieces: Map<Pair<Int, Int>, PieceInfo>
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()

        for (direction in listOf(-1, +1)) {
            var newCords = from + (versor * direction)
            while (isOnMap(newCords) && !previousPieces.containsKey(newCords)) {
                possiblePositions.add(newCords)
                newCords += (versor * direction)
            }
        }
        return possiblePositions
    }



    fun isOnMap(pos: Pair<Int, Int>): Boolean{
        return pos.first < 8 && pos.second < 8 && pos.first > -1 && pos.second > -1
    }
}