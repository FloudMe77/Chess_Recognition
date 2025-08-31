package com.example.chessLens.chessPieces

import com.example.chessLens.enums.ColorTeam
import com.example.chessLens.utils.PieceInfo
import com.example.chessLens.utils.plus
import com.example.chessLens.utils.minus
import com.example.chessLens.utils.times
import kotlin.math.abs


class Knight:Piece {
    override fun validateMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): Boolean{
        val diff = to - from
        // knight validation move
        return (abs(diff.first) == 2 && abs(diff.second) == 1) || (abs(diff.first) == 1 && abs(diff.second) == 2)
    }

    override fun possibleTake(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
        colorTeam: ColorTeam,
    ): List<Pair<Int, Int>> {
        val colorOfPiece = positionMap[from]?.color ?: return mutableListOf()
        val directions = listOf(Pair(1, 2), Pair(2, 1))
        val signs = listOf(-1, 1)

        val possiblePositions = directions
            // all cords, where can potential move
            .flatMap { d -> signs.flatMap { sx -> signs.map { sy -> d * Pair(sx, sy) } } }
            .map { from + it }
            // can take
            .filter { isOnMap(it) && positionMap[it]?.color != colorOfPiece && positionMap[it] != null }

        return possiblePositions
    }

    override fun possibleMove(
        from: Pair<Int, Int>,
        positionMap: Map<Pair<Int, Int>, PieceInfo>,
    ): List<Pair<Int, Int>> {
        val possibleCords = listOf(Pair(1, 2), Pair(2, 1))
            .flatMap { d -> listOf(-1, 1).flatMap { sx -> listOf(-1, 1).map { sy -> from + (d * Pair(sx, sy)) } } }
            // can move
            .filter { isOnMap(it) && !positionMap.containsKey(it) }

        return possibleCords
    }
}