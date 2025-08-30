package pl.dariusz_marecik.chess_rec.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import pl.dariusz_marecik.chess_rec.viewmodel.PositionViewModel

// Analyzes camera frames and sends rotated bitmap images to the ViewModel
class ChessImageAnalyzer(
    private val positionViewModel: PositionViewModel
) : ImageAnalysis.Analyzer {

    private var lastAnalysisTime = 0L
    private val analysisInterval = 200L

    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastAnalysisTime >= analysisInterval) {
            try {
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

                positionViewModel.sendImage(rotatedBitmap)
                lastAnalysisTime = currentTime

            } catch (e: Exception) {
                Log.e("ChessImageAnalyzer", "Error analyzing image", e)
            }
        }

        image.close()
    }
}