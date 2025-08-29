package pl.dariusz_marecik.chess_rec.ui

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import pl.dariusz_marecik.chess_rec.ChessImageAnalyzer
import pl.dariusz_marecik.chess_rec.utils.PieceInfo
import pl.dariusz_marecik.chess_rec.PositionViewModel


@Composable
fun CameraApp(applicationContext: Context, isCameraView: MutableState<Boolean>, viewModel: PositionViewModel) {
    val context = LocalContext.current
    val activity = context as Activity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val controller = remember {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(
                CameraController.IMAGE_ANALYSIS or
                        CameraController.VIDEO_CAPTURE
            )

            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(applicationContext),
                ChessImageAnalyzer(viewModel)
            )
        }
    }

    val piecesFlow = viewModel.pieces
    val isConnected by viewModel.getConnectionStatus().collectAsState()
    val pieces by piecesFlow.collectAsState()

    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        horizontalDrawer(isConnected, pieces, viewModel, isCameraView, controller)
    } else {
        verticalDrawer(isConnected, pieces, viewModel, isCameraView, controller)
    }
}

@Composable
private fun verticalDrawer(
    isConnected: Boolean,
    pieces: Map<Pair<Int, Int>, PieceInfo>,
    viewModel: PositionViewModel,
    isCameraView: MutableState<Boolean>,
    controller: LifecycleCameraController
) {
    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f).background(Color.LightGray)) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .padding(25.dp)
                    .background(if (isConnected) Color.Green else Color.Red, shape = CircleShape)
            )
            BoardContent(
                pieces,
                modifier = Modifier.weight(1f),
                viewModel.getLatestMove(),
                null
            )
            IconButton(onClick = { isCameraView.value = !isCameraView.value }, modifier = Modifier.size(70.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Change mode")
            }
        }

        Box(modifier = Modifier.weight(4f)) {
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize().padding(10.dp),
            )
        }
    }
}

@Composable
private fun horizontalDrawer(
    isConnected: Boolean,
    pieces: Map<Pair<Int, Int>, PieceInfo>,
    viewModel: PositionViewModel,
    isCameraView: MutableState<Boolean>,
    controller: LifecycleCameraController
) {
    Row(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).background(Color.LightGray)) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .padding(25.dp)
                    .background(if (isConnected) Color.Green else Color.Red, shape = CircleShape)
            )
            BoardContent(
                pieces,
                modifier = Modifier.weight(1f),
                viewModel.getLatestMove(),
                null
            )
            IconButton(onClick = { isCameraView.value = !isCameraView.value }, modifier = Modifier.size(70.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Change mode")
            }
        }

        Box(modifier = Modifier.weight(4f)) {
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize().padding(10.dp),
            )
        }
    }
}