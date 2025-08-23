package pl.dariusz_marecik.chess_rec

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.media3.common.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.util.Log
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import okhttp3.*
import android.view.WindowManager
import java.io.IOException

@androidx.annotation.OptIn(ExperimentalGetImage::class)
class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if(!hasRequiredPermissions()){
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)

        setContent {
            CameraApp(applicationContext)
        }


    }


    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        piecesViewModel: PiecesViewModel
    ){
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object  : OnImageCapturedCallback(){
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't capture image", exception)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }

                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )

                    piecesViewModel.sendImage(rotatedBitmap)

                    image.close()
                }
            }
        )
    }

    private fun onPlayClick(gameStarted: MutableState<Boolean>, viewModel: PiecesViewModel, positionManager: MutableState<PositionManager?>,controller: LifecycleCameraController, startPosition: MutableState<Map<Pair<Int, Int>, PieceInfo>?>) {
        if(!gameStarted.value){
            startPosition.value = viewModel.pieces.value
            positionManager.value = PositionManager(viewModel.pieces.value)
            gameStarted.value = true
        }
        else{
            positionManager.value?.let { manager ->
                if (manager.isPositionReady.value) {
                    val move = manager.acceptNewState()
                    if (move != null) {
                        viewModel.saveMove(move)
                    }
                    try {
                        takePhoto(
                            controller = controller,
                            viewModel
                        )
                    } catch (e: Exception) {
                        Log.d("zjebało sie", e.message.toString());
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CameraApp(applicationContext: Context) {
        val positionManager = remember { mutableStateOf<PositionManager?>(null) }
        val scope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }
        var gameUrl by remember { mutableStateOf("") }
        val context = LocalContext.current
        val startPosition = remember { mutableStateOf<Map<Pair<Int, Int>, PieceInfo>?>(null) }
        val gameStarted = remember { mutableStateOf(false) }
        val scaffoldState = rememberBottomSheetScaffoldState()
        val controller = remember {
            LifecycleCameraController(applicationContext).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or
                            CameraController.VIDEO_CAPTURE
                )
            }
        }
        val viewModel = viewModel<PiecesViewModel>()
        val piecesFlow = remember(positionManager.value, gameStarted.value) {
            if (gameStarted.value && positionManager.value != null) {
                positionManager.value!!.piecesMap
            } else {
                viewModel.pieces
            }
        }

        val pieces by piecesFlow.collectAsState()
        val piecesFollow by viewModel.pieces.collectAsState()


        Column(Modifier.fillMaxSize()) {
            BoardContent(
                pieces,
                modifier = Modifier.weight(1f),
                viewModel.getLatestMove(),
                positionManager.value?.potentialNewMove
            )
            Box(modifier = Modifier.weight(4f))
            {

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomSheetContent(
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                ) { padding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        CameraPreview(
                            controller = controller,
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = {
                                controller.cameraSelector =
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                            },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Switch camera")
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = {onRestartClick(gameStarted, positionManager, startPosition, viewModel)},
                                modifier = Modifier
                                    .size(60.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Restart",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            // Duży przycisk Play
                            Button(
                                onClick = {
                                    onPlayClick(gameStarted, viewModel, positionManager, controller, startPosition)
                                },
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val pgn = PgnExporter.export(viewModel.moveList.value, startPosition.value!!)
                                    LichessConverter.importPgnToLichess(pgn) { url ->
                                        if (url != null) {
                                            gameUrl = url
                                            showDialog = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(60.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
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
            LaunchedEffect(controller) {
                while (isActive) {
                    try {
                        takePhoto(
                            controller = controller,
                            viewModel
                        )
                    } catch (e: Exception) {
                        Log.d("CameraProblem", e.message.toString());
                    }
                    delay(1000)
                }
            }

        }

    }


