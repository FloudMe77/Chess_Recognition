package pl.dariusz_marecik.chessLens.ui

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
import pl.dariusz_marecik.chessLens.utils.ChessImageAnalyzer
import pl.dariusz_marecik.chessLens.utils.PieceInfo
import pl.dariusz_marecik.chessLens.viewmodel.PositionViewModel


@Composable
fun CameraApp(applicationContext: Context, isCameraView: MutableState<Boolean>, viewModel: PositionViewModel) {
    // Main composable for the camera view, switching layout based on orientation
    val context = LocalContext.current
    val activity = context as Activity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val controller = remember {
        LifecycleCameraController(applicationContext).apply {
            // Configure camera controller with analysis and video capture
            setEnabledUseCases(
                CameraController.IMAGE_ANALYSIS or
                        CameraController.VIDEO_CAPTURE
            )

            // Set analyzer to process frames for chess recognition
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(applicationContext),
                ChessImageAnalyzer(viewModel)
            )
        }
    }

    val piecesFlow = viewModel.pieces
    val isConnected by viewModel.getConnectionStatus().collectAsState()
    val pieces by piecesFlow.collectAsState()

    // Switch layout based on device orientation
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
    // Layout for portrait orientation with board on top and camera below
    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f).background(Color.LightGray)) {
            // Status indicator for connection
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .padding(25.dp)
                    .background(if (isConnected) Color.Green else Color.Red, shape = CircleShape)
            )
            // Chess board content
            BoardContent(
                pieces,
                modifier = Modifier.weight(1f),
                viewModel.getLatestMove(),
                null
            )
            // Button to switch camera/board view
            IconButton(onClick = { isCameraView.value = !isCameraView.value }, modifier = Modifier.size(70.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Change mode")
            }
        }

        // Camera preview section
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
    // Layout for landscape orientation with board on left and camera on right
    Row(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).background(Color.LightGray)) {
            // Status indicator for connection
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .padding(25.dp)
                    .background(if (isConnected) Color.Green else Color.Red, shape = CircleShape)
            )
            // Chess board content
            BoardContent(
                pieces,
                modifier = Modifier.weight(1f),
                viewModel.getLatestMove(),
                null
            )
            // Button to switch camera/board view
            IconButton(onClick = { isCameraView.value = !isCameraView.value }, modifier = Modifier.size(70.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Change mode")
            }
        }

        // Camera preview section
        Box(modifier = Modifier.weight(4f)) {
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize().padding(10.dp),
            )
        }
    }
}
