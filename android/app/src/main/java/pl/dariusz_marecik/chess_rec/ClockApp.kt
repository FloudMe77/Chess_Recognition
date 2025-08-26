package pl.dariusz_marecik.chess_rec

import android.app.Activity
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

private fun onPlayClick(gameStarted: MutableState<Boolean>, viewModel: PiecesViewModel, positionManager: MutableState<PositionManager?>, startPosition: MutableState<Map<Pair<Int, Int>, PieceInfo>?>) {
    if(!gameStarted.value){
        startPosition.value = viewModel.pieces.value
        positionManager.value = PositionManager(viewModel.pieces.value)
        gameStarted.value = true
    }
    else{
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
private fun onRestartClick(gameStarted: MutableState<Boolean>, positionManager: MutableState<PositionManager?>, startPosition: MutableState<Map<Pair<Int, Int>, PieceInfo>?>, viewModel: PiecesViewModel){
    gameStarted.value = false
    positionManager.value = null
    startPosition.value = null
    viewModel.restartListMove()
}

@Composable
fun ClockApp(isCameraView: MutableState<Boolean>,  viewModel: PiecesViewModel) {
    val positionManager = remember { mutableStateOf<PositionManager?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var gameUrl by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as Activity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    val startPosition = remember { mutableStateOf<Map<Pair<Int, Int>, PieceInfo>?>(null) }
    val gameStarted = remember { mutableStateOf(false) }

    val piecesFlow = remember(positionManager.value, gameStarted.value) {
        if (gameStarted.value && positionManager.value != null) {
            positionManager.value!!.positionToDraw
        } else {
            viewModel.pieces
        }
    }

    val pieces by piecesFlow.collectAsState()
    val piecesFollow by viewModel.pieces.collectAsState()
    val isConnected by viewModel.getConnectionStatus().collectAsState()

    val activeButtonColor = Color(0xFF7ABC56)
    val deActiveButtonColor = Color(0xFF696969)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.LightGray))
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
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
            val blackPlayerFontColor = if(blackPlayerButtonColor == activeButtonColor) Color.Black else Color.White
            val whitePlayerFontColor = if(whitePlayerButtonColor == activeButtonColor) Color.Black else Color.White
            Button(
                onClick = { if(!gameStarted.value ||  positionManager.value?.onMove == ColorTeam.BLACK) onPlayClick(gameStarted, viewModel, positionManager, startPosition)},
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blackPlayerButtonColor)

            ) {
                Text("Black Player", modifier = Modifier.graphicsLayer { rotationZ = 180f }, fontSize = 20.sp, color = blackPlayerFontColor)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Column(modifier = Modifier,
                ){
                    // next

                    Box(
                        modifier = Modifier
                            .size(70.dp) // średnica
                            .padding(25.dp)
                            .background( if(isConnected) Color.Green else Color.Red, shape = CircleShape)
                    )

                    //save
                    IconButton(
                        onClick = {
                            if(gameStarted.value) {
                                val pgn = PgnExporter.export(viewModel.moveList.value, startPosition.value!!)
                                LichessConverter.importPgnToLichess(pgn) { url ->
                                    if (url != null) {
                                        gameUrl = url
                                        showDialog = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(70.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Save")
                    }
                }

                BoardContent(
                    pieces,
                    modifier = Modifier.weight(1f),
                    viewModel.getLatestMove(),
                    positionManager.value?.potentialNewMove
                )

                Column {
                    // reset
                    IconButton(onClick = { onRestartClick(gameStarted, positionManager, startPosition, viewModel) }, modifier = Modifier.size(70.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Restart")
                    }
                    //
                    IconButton(onClick = { isCameraView.value = !isCameraView.value }, modifier = Modifier.size(70.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "Change mode")
                    }

                }
            }
            Button(
                onClick = {if(!gameStarted.value ||  positionManager.value?.onMove == ColorTeam.WHITE) onPlayClick(gameStarted, viewModel, positionManager, startPosition)},
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = whitePlayerButtonColor,
                )

            ) {
                Text("White Player", fontSize = 20.sp, color = whitePlayerFontColor)
            }
    }

    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Link do partii") },
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
    LaunchedEffect(positionManager.value, piecesFollow) {
        positionManager.value?.considerNewPosition(piecesFollow)
    }


}