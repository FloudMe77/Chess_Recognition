package pl.dariusz_marecik.chessLens.game

import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.enums.PieceKind
import pl.dariusz_marecik.chessLens.utils.CastleInfo
import pl.dariusz_marecik.chessLens.utils.Move

// Manages castling rights for both players and updates them based on king and rook movements.
class CastlingManager {
    private val castleMap: Map<ColorTeam, CastleInfo> = mapOf(
        ColorTeam.WHITE to CastleInfo(),
        ColorTeam.BLACK to CastleInfo()
    )

    fun canCastleLeft(color: ColorTeam): Boolean = castleMap[color]?.left == true
    fun canCastleRight(color: ColorTeam): Boolean = castleMap[color]?.right == true

    fun updateCastlingRights(move: Move) {
        val (from, _, pieceKind) = move

        when (pieceKind) {
            PieceKind.WHITE_ROOK -> {
                when (from) {
                    Pair(0, 0) -> castleMap[ColorTeam.WHITE]?.left = false
                    Pair(7, 0) -> castleMap[ColorTeam.WHITE]?.right = false
                }
            }
            PieceKind.BLACK_ROOK -> {
                when (from) {
                    Pair(0, 7) -> castleMap[ColorTeam.BLACK]?.left = false
                    Pair(7, 7) -> castleMap[ColorTeam.BLACK]?.right = false
                }
            }
            PieceKind.WHITE_KING -> {
                castleMap[ColorTeam.WHITE]?.left = false
                castleMap[ColorTeam.WHITE]?.right = false
            }
            PieceKind.BLACK_KING -> {
                castleMap[ColorTeam.BLACK]?.left = false
                castleMap[ColorTeam.BLACK]?.right = false
            }
            else -> {}
        }
    }
}