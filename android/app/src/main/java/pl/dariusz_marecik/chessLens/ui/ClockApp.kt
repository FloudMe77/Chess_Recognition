package pl.dariusz_marecik.chessLens.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.dariusz_marecik.chessLens.enums.ColorTeam
import pl.dariusz_marecik.chessLens.game.PositionManager
import pl.dariusz_marecik.chessLens.serialization.LichessConverter
import pl.dariusz_marecik.chessLens.serialization.PgnExporter
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import pl.dariusz_marecik.chessLens.viewmodel.PositionViewModel

private fun onPlayClick(
    gameStarted: MutableState<Boolean>,
    viewModel: PositionViewModel,
    positionManager: MutableState<PositionManager?>,
    startPosition: MutableState<Map<Pair<Int, Int>, PieceInfo>?>
) {
    // Handles the play button logic for starting or making a move in the game
    if (!gameStarted.value) {
        startPosition.value = viewModel.pieces.value
        positionManager.value = PositionManager(viewModel.pieces.value)
        gameStarted.value = true
    } else {
        positionManager.value?.let { manager ->
            if (manager.isMoveFound.value) {
                val move = manager.acceptNewState()
                if (move != null) {
                    viewModel.saveMove(move)
                }
            }
        }
    }

}

private fun onRestartClick(
    gameStarted: MutableState<Boolean>,
    positionManager: MutableState<PositionManager?>,
    startPosition: MutableState<Map<Pair<Int, Int>, PieceInfo>?>,
    viewModel: PositionViewModel
) {
    // Resets the game state and clears all moves
    gameStarted.value = false
    positionManager.value = null
    startPosition.value = null
    viewModel.restartListMove()
}

@Composable
fun ClockApp(isCameraView: MutableState<Boolean>, viewModel: PositionViewModel) {
    // Main UI for the chess clock and board interface
    val positionManager = remember { mutableStateOf<PositionManager?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val gameUrl = remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as Activity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    val startPosition = remember { mutableStateOf<Map<Pair<Int, Int>, PieceInfo>?>(null) }
    val gameStarted = remember { mutableStateOf(false) }
    val messageForDialog = remember { mutableStateOf("") }
    val isMate = positionManager.value?.isMate?.collectAsState(initial = false)?.value ?: false
    val isStaleMate = positionManager.value?.isStaleMate?.collectAsState(initial = false)?.value ?: false

    // Determine which piece positions to display based on game state
    val positionMapFlow = remember(positionManager.value, gameStarted.value) {
        if (gameStarted.value && positionManager.value != null) {
            positionManager.value!!.positionToDraw
        } else {
            viewModel.pieces
        }
    }

    val positionMap by positionMapFlow.collectAsState()
    val cameraPositionMap by viewModel.pieces.collectAsState()
    val isConnected by viewModel.getConnectionStatus().collectAsState()

    val activeButtonColor = Color(0xFF7ABC56)
    val deActiveButtonColor = Color(0xFF696969)

    // Main layout container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Configure player button colors based on game state
            val blackPlayerButtonColor = when {
                !gameStarted.value -> activeButtonColor
                positionManager.value?.onMove == ColorTeam.BLACK -> activeButtonColor
                else -> deActiveButtonColor
            }
            val whitePlayerButtonColor = when {
                !gameStarted.value -> activeButtonColor
                positionManager.value?.onMove == ColorTeam.WHITE -> activeButtonColor
                else -> deActiveButtonColor
            }
            val blackPlayerFontColor = if (blackPlayerButtonColor == activeButtonColor) Color.Black else Color.White
            val whitePlayerFontColor = if (whitePlayerButtonColor == activeButtonColor) Color.Black else Color.White

            // Black player button
            Button(
                onClick = {
                    if (!gameStarted.value || positionManager.value?.onMove == ColorTeam.BLACK) onPlayClick(
                        gameStarted,
                        viewModel,
                        positionManager,
                        startPosition
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blackPlayerButtonColor)

            ) {
                Text(
                    if(gameStarted.value) "Black Player" else "Accept position",
                    modifier = Modifier.graphicsLayer { rotationZ = 180f },
                    fontSize = 20.sp,
                    color = blackPlayerFontColor
                )
            }

            // Main row with board and side buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier,
                ) {
                    // Connection indicator
                    Box(
                        modifier = Modifier
                            .size(70.dp) // średnica
                            .padding(25.dp)
                            .background(if (isConnected) Color.Green else Color.Red, shape = CircleShape)
                    )

                    // Save game button
                    IconButton(
                        onClick = {
                            calcGameUrl(gameStarted, viewModel, startPosition, gameUrl, showDialog)
                            messageForDialog.value = "Link to the chess game:"

                        },
                        modifier = Modifier.size(70.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Save")
                    }
                }

                // Board display
                BoardContent(
                    positionMap,
                    modifier = Modifier.weight(1f),
                    viewModel.getLatestMove(),
                    positionManager.value?.potentialNewMove
                )

                Column {
                    // Restart button
                    IconButton(
                        onClick = { onRestartClick(gameStarted, positionManager, startPosition, viewModel) },
                        modifier = Modifier.size(70.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Restart")
                    }
                    // Switch camera/view mode button
                    IconButton(
                        onClick = { isCameraView.value = !isCameraView.value },
                        modifier = Modifier.size(70.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Change mode")
                    }

                }
            }
            // White player button
            Button(
                onClick = {
                    if (!gameStarted.value || positionManager.value?.onMove == ColorTeam.WHITE) onPlayClick(
                        gameStarted,
                        viewModel,
                        positionManager,
                        startPosition
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = whitePlayerButtonColor,
                )

            ) {
                Text(if(gameStarted.value) "White Player" else "Accept position", fontSize = 20.sp, color = whitePlayerFontColor)
            }
        }

    }

    // Monitor board changes and update potential move detection
    LaunchedEffect(positionManager.value, cameraPositionMap) {
        positionManager.value?.considerNewPosition(cameraPositionMap)
    }

    // Show mate notification when detected
    LaunchedEffect(isMate) {
        if (isMate) {
            messageForDialog.value = "Mate!! \nLink to the chess game: "
            calcGameUrl(gameStarted, viewModel, startPosition, gameUrl, showDialog)

        }
    }

    // Show stalemate notification when detected
    LaunchedEffect(isStaleMate) {
        if (isStaleMate) {
            messageForDialog.value = "Stalemate!! \nLink to the chess game:"
            calcGameUrl(gameStarted, viewModel, startPosition, gameUrl, showDialog)

        }
    }

    // Display the notification dialog if required
    if (showDialog.value) {
        showNotification(gameUrl.value, context, messageForDialog.value, showDialog)
    }


}

private fun calcGameUrl(
    gameStarted: MutableState<Boolean>,
    viewModel: PositionViewModel,
    startPosition: MutableState<Map<Pair<Int, Int>, PieceInfo>?>,
    gameUrl: MutableState<String>,
    showDialog: MutableState<Boolean>
) {
    // Generate PGN and get Lichess game URL
    if (gameStarted.value) {
        val pgn = PgnExporter.export(viewModel.moveList.value, startPosition.value!!)
        LichessConverter.importPgnToLichess(pgn) { url ->
            if (url != null) {
                gameUrl.value = url
                showDialog.value = true
            }
        }
    }
}

@Composable
private fun showNotification(
    gameUrl: String,
    context: Context,
    communicate: String,
    showDialog: MutableState<Boolean>
) {
    // Display an alert dialog with clickable game URL
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            },
            title = { Text(communicate) },
            text = {
                ClickableText(
                    text = AnnotatedString(gameUrl),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gameUrl))
                        context.startActivity(intent)
                    }
                )
            }
        )
    }
}