package com.example.chessLens.game

import com.example.chessLens.enums.Action
import com.example.chessLens.utils.Move
import com.example.chessLens.utils.PieceInfo

// Applies chess moves to board positions including special moves like castling, en passant, and promotion.
class PositionModifier {
    fun applyMove(
        position: MutableMap<Pair<Int, Int>, PieceInfo>,
        move: Move
    ) {
        val (from, to, _, label) = move

        when (label) {
            Action.MOVE -> {
                position.remove(from)?.let { piece ->
                    movePositionMod(position, piece, to)
                }
            }
            Action.PROMOTION -> {
                position.remove(from)?.let { piece ->
                    promotionPositionMod(position, piece, to)
                }
            }
            Action.TAKE -> {
                position.remove(from)?.let { piece ->
                    takePositionMod(position, piece, to)
                }
            }
            Action.EN_PASSANT -> {
                position.remove(from)?.let { piece ->
                    enPassantPositionMod(position, piece, from, to)
                }
            }
            Action.CASTLE_LEFT, Action.CASTLE_RIGHT -> {
                applyCastling(position, move, label)
            }
        }
    }

    private fun applyCastling(
        position: MutableMap<Pair<Int, Int>, PieceInfo>,
        move: Move,
        label: Action
    ) {
        val (from, to) = move
        position[from]?.let { king ->
            val row = to.second
            val rookFrom = if (label == Action.CASTLE_LEFT) Pair(0, row) else Pair(7, row)
            val rookTo = if (label == Action.CASTLE_LEFT) Pair(3, row) else Pair(5, row)

            position[rookFrom]?.let { rook ->
                castlePositionMod(position, king, from, to, rook, rookFrom, rookTo)
            }
        }
    }

    private fun castlePositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        king: PieceInfo,
        fromKing: Pair<Int, Int>,
        toKing: Pair<Int, Int>,
        rook: PieceInfo,
        rookFrom: Pair<Int, Int>,
        rookTo: Pair<Int, Int>
    ) {
        newPosition.remove(fromKing)
        newPosition.remove(rookFrom)
        king.cords = toKing
        rook.cords = rookTo
        newPosition[king.cords] = king
        newPosition[rook.cords] = rook
    }

    internal fun enPassantPositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        from: Pair<Int, Int>,
        to: Pair<Int, Int>
    ) {
        val capturePos = Pair(to.first, from.second)
        newPosition.keys.removeAll(listOfNotNull(from, capturePos).toSet())
        piece.cords = to
        newPosition[to] = piece
    }

    internal fun takePositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        to: Pair<Int, Int>
    ) {
        newPosition.keys.removeAll(listOfNotNull(piece.cords, to).toSet())
        piece.cords = to
        newPosition[to] = piece
    }

    private fun promotionPositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        to: Pair<Int, Int>
    ) {
        val promoted = PieceInfo(piece.id + 4, piece.position, piece.bbox)
        promoted.cords = to
        newPosition[to] = promoted
    }

    internal fun movePositionMod(
        newPosition: MutableMap<Pair<Int, Int>, PieceInfo>,
        piece: PieceInfo,
        to: Pair<Int, Int>
    ) {
        piece.cords = to
        newPosition[to] = piece
    }
}