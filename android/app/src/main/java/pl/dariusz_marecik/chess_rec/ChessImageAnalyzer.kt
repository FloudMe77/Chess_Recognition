package pl.dariusz_marecik.chess_rec

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ChessImageAnalyzer(
    private val piecesViewModel: PiecesViewModel
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

                piecesViewModel.sendImage(rotatedBitmap)
                lastAnalysisTime = currentTime

            } catch (e: Exception) {
                Log.e("ChessImageAnalyzer", "Error analyzing image", e)
            }
        }

        image.close()
    }
}