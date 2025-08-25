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
            MainApp(applicationContext)
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

    @Composable
    fun MainApp(applicationContext: Context) {
        val isCameraView = remember { mutableStateOf(false) }
        val viewModel = viewModel<PiecesViewModel>()
        if(isCameraView.value){
            CameraApp(applicationContext, isCameraView, viewModel)
        }
        else{
            ClockApp(isCameraView, viewModel)
        }


        }

    }


