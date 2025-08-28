package pl.dariusz_marecik.chess_rec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.WindowManager
import androidx.compose.runtime.saveable.rememberSaveable

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

    @Composable
    fun MainApp(applicationContext: Context) {
        val isCameraView = rememberSaveable { mutableStateOf(false) }
        val viewModel = viewModel<PiecesViewModel>()
        if(isCameraView.value){
            CameraApp(applicationContext, isCameraView, viewModel)
        }
        else{
            ClockApp(isCameraView, viewModel)
        }


        }
    }


