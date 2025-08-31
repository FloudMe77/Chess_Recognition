package com.example.chessLens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chessLens.ui.CameraApp
import com.example.chessLens.ui.ClockApp
import com.example.chessLens.viewmodel.PositionViewModel

@androidx.annotation.OptIn(ExperimentalGetImage::class)
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (!hasRequiredPermissions()) {
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
        val viewModel = viewModel<PositionViewModel>()
        // Switch between CameraApp and ClockApp based on state
        if (isCameraView.value) {
            CameraApp(applicationContext, isCameraView, viewModel)
        } else {
            ClockApp(isCameraView, viewModel)
        }
    }
}


