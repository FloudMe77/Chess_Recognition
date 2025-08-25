package pl.dariusz_marecik.chess_rec

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private fun takePhoto(
    controller: LifecycleCameraController,
    piecesViewModel: PiecesViewModel,
    applicationContext: Context
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraApp(applicationContext: Context, isCameraView: MutableState<Boolean>,  viewModel: PiecesViewModel) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    val controller = remember {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }
    val piecesFlow = viewModel.pieces
    val isConnected by viewModel.getConnectionStatus().collectAsState()

    val pieces by piecesFlow.collectAsState()


    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f).background(Color.LightGray)){
            Box(
                modifier = Modifier
                    .size(70.dp) // średnica
                    .padding(25.dp)
                    .background( if(isConnected) Color.Green else Color.Red, shape = CircleShape)
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



                }
            }
            LaunchedEffect(controller) {
                while (isActive) {
                    try {
                        takePhoto(
                            controller = controller,
                            viewModel,
                            context
                        )
                    } catch (e: Exception) {
                        Log.d("CameraProblem", e.message.toString());
                    }
                    delay(500)
                }
            }
        }
    }
}
