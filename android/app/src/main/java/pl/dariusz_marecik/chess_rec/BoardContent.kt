package pl.dariusz_marecik.chess_rec

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
//import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.ColorUtils

@Composable
fun BoardContent(
    pieces: Map<Pair<Int, Int>,PieceInfo>,
    modifier: Modifier = Modifier,
    previousMove: Move?,
    potentialMove: Move?,
) {
    val lightSquareColor = Color(0xFFEEEED2)
    val darkSquareColor = Color(0xFF769656)
    val highlightColorPreviousMove = Color(0x80FFD700)
    val highlightColorPotentialMove = Color(0x8033B5E5)
    Log.d("BoardContent", pieces.toString())
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {


        Column(
            modifier = modifier
                .aspectRatio(1f)
        ) {
            repeat(8) { rowIndex ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(8) { colIndex ->
                        val isLightSquare = (rowIndex + colIndex) % 2 == 0

                        val baseColor = if (isLightSquare) lightSquareColor else darkSquareColor

                        val boxPosition = Pair(colIndex, 7-rowIndex)
                        val squareColor = when {
                            potentialMove != null && (boxPosition == potentialMove.to || boxPosition == potentialMove.from) ->
                                Color(
                                    ColorUtils.blendARGB(baseColor.toArgb(), highlightColorPotentialMove.toArgb(), 0.5f)
                                )
                            previousMove != null && (boxPosition == previousMove.to || boxPosition == previousMove.from) ->
                                Color(
                                    ColorUtils.blendARGB(baseColor.toArgb(), highlightColorPreviousMove.toArgb(), 0.5f)
                                )
                            else -> baseColor
                        }

                        val piece = pieces[boxPosition]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(squareColor),
                            contentAlignment = Alignment.Center
                        ) {
                            piece?.let {
                                val rotationModifier = if(piece.color == ColorTeam.BLACK) Modifier.graphicsLayer { rotationZ = 180f } else Modifier
                                val imageResource = when (it.name) {
                                    PieceKind.WHITE_PAWN -> R.drawable.white_pawn
                                    PieceKind.WHITE_ROOK -> R.drawable.white_rook
                                    PieceKind.WHITE_KNIGHT -> R.drawable.white_knight
                                    PieceKind.WHITE_BISHOP -> R.drawable.white_bishop
                                    PieceKind.WHITE_QUEEN -> R.drawable.white_queen
                                    PieceKind.WHITE_KING -> R.drawable.white_king
                                    PieceKind.BLACK_PAWN -> R.drawable.black_pawn
                                    PieceKind.BLACK_ROOK -> R.drawable.black_rook
                                    PieceKind.BLACK_KNIGHT -> R.drawable.black_knight
                                    PieceKind.BLACK_BISHOP -> R.drawable.black_bishop
                                    PieceKind.BLACK_QUEEN -> R.drawable.black_queen
                                    PieceKind.BLACK_KING -> R.drawable.black_king
                                }
                                Image(
                                    painter = painterResource(imageResource),
                                    contentDescription = "${it.position} (${it.id})",
                                    modifier = rotationModifier
                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}
