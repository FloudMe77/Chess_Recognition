package com.example.chessLens.chessPieces

import com.example.chessLens.enums.ColorTeam
import com.example.chessLens.utils.*
import kotlin.math.abs
import kotlin.math.max

interface Piece {
    // check if move is valid on actual chessboard
    fun validateMove(from: Pair<Int, Int>, to: Pair<Int, Int>, positionMap:Map<Pair<Int, Int>, PieceInfo>, colorTeam: ColorTeam): Boolean

    // return list of possible capture by figure on specific cords
    fun possibleTake(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>>

    // return list of squares, where figure can move
    fun possibleMove(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
    ): List<Pair<Int, Int>>

    // abstract general method
    fun abstractValidateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        isValidDirection: (Pair<Int, Int>) -> Boolean,
    ): Boolean {
        val direction = to - from
        if(direction == 0 to 0) return false /* can't be on the same square */
        if (!isValidDirection(direction)) return false
        val len = max(abs(direction.first), abs(direction.second))
        val step = direction / len
        var pos = from + step

        while (pos != to) {

            if (positionMap.containsKey(pos)) return false /* something on the road */
            pos += step
        }

        return true
    }
    // abstract general method
    fun abstractPossibleTake(
        from: Pair<Int, Int>,
        versor: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        val colorOfPiece = positionMap[from]?.color ?: return possiblePositions

        // in both opposite directions
        for (direction in listOf(-1, +1)) {
            var newPosition = from + (versor * direction)
            // while we dont meet figure
            while (isOnMap(newPosition) && !positionMap.containsKey(newPosition)) {
                newPosition += (versor * direction)
            }
            if (isOnMap(newPosition)) {
                positionMap[newPosition]?.let {
                    // if opponent piece
                    if(it.color != colorOfPiece){
                        possiblePositions.add(newPosition)
                    }
                }
            }
        }
        return possiblePositions
    }
    // abstract general method
    fun abstractPossibleMove(
        from: Pair<Int, Int>,
        versor: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>
    ): List<Pair<Int, Int>> {
        val possiblePositions = mutableListOf<Pair<Int, Int>>()
        // in both opposite directions
        for (direction in listOf(-1, +1)) {
            var newCords = from + (versor * direction)
            // while we dont meet figure, add as can to move
            while (isOnMap(newCords) && !positionMap.containsKey(newCords)) {
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