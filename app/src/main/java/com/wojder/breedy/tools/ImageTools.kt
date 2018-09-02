package com.wojder.breedy.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.wojder.breedy.imageClassifier.IMAGE_SIZE

class ImageTools {
    fun getCroppedBitmap(photoBitmap: Bitmap): Bitmap {
        val croppedBitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)
        val transfromationMatrix = getTakenPhotoTransformattedMatrix(photoBitmap)
        val canvas = Canvas(croppedBitmap)

        canvas.drawBitmap(photoBitmap, transfromationMatrix, null)
        return croppedBitmap
    }

    private fun getTakenPhotoTransformattedMatrix(photoBitmap: Bitmap): Matrix {
        val frameToCropTransformatedMatrix = getTransformattedMatrix(
                photoBitmap.width, photoBitmap.height,
                IMAGE_SIZE, IMAGE_SIZE, 0, true)
        val cropToFrameTransformatedMatrix = Matrix()
        frameToCropTransformatedMatrix.invert(cropToFrameTransformatedMatrix)
        return frameToCropTransformatedMatrix
    }

    private fun getTransformattedMatrix(srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int, applyRotation: Int, maintainAscpectRatio: Boolean): Matrix {
        val matrix = Matrix()

        matrix.postTranslate(-srcWidth/2.0f, -srcHeight/2.0f)

        matrix.postRotate(applyRotation.toFloat())

        val transpose = (Math.abs(applyRotation) + 90) % 180 == 0

        val inWidth = if (transpose) srcHeight else srcWidth
        val inHeight = if (transpose) srcWidth else srcHeight

        if (inWidth != dstWidth || inHeight != dstHeight) {
            val scaleFactorX = dstWidth / inWidth.toFloat()
            val scaleFactorY = dstHeight / inHeight.toFloat()

            if (maintainAscpectRatio) {
                val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }

        matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)

        return matrix
    }
}
