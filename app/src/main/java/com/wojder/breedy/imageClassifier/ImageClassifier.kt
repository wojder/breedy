package com.wojder.breedy

import android.graphics.Bitmap
import com.wojder.breedy.imageClassifier.COLOR_CHANNELS
import com.wojder.breedy.Result
import java.lang.Float
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.*

private const val ENABLE_LOG_STATS = false

class ImageClassifier(
    private val inputName: String,
    private val outputName: String,
    private val imageSize: Long,
    private val labels: List<String>,
    private val imageBitmapPixels: IntArray,
    private val imageNormalizePixels: FloatArray,
    private val results: FloatArray,
    private val tensorFlowInference: TensorFlowInferenceInterface
) : Classifier {
        override fun recognizeImage(bitmap: Bitmap): Result {
                preprocessImageToNormalizedFloats(bitmap)
                classifyImageToOutputs()
                val outputQueue = getResults()
                return outputQueue.poll()
        }

        private fun getResults(): PriorityQueue<Result> {
                val outputQueue = createOutputQueue()
                results.indices.mapTo(outputQueue){ Result(labels[it], results[it]) }
                return outputQueue
        }

        private fun createOutputQueue(): PriorityQueue<Result> {
                return PriorityQueue(
                        labels.size,
                        Comparator { (_, rConfidence), (_, lConfidence) ->
                                Float.compare(lConfidence, rConfidence) })
        }

        private fun classifyImageToOutputs() {
                tensorFlowInference.feed(inputName, imageNormalizePixels, 1L, imageSize, imageSize, COLOR_CHANNELS.toLong())
                tensorFlowInference.run(arrayOf(outputName), ENABLE_LOG_STATS)
                tensorFlowInference.fetch(outputName, results)
        }

        private fun preprocessImageToNormalizedFloats(bitmap: Bitmap){
                val imageMean = 128
                val imageStd = 128.0f
                bitmap.getPixels(imageBitmapPixels, 0, bitmap.width, 0,0, bitmap.width, bitmap.height)

                for (i in imageBitmapPixels.indices) {
                        val `val` = imageBitmapPixels[i]
                        imageNormalizePixels[i * 3 + 0] = ((`val` shr 16 and 0xFF) - imageMean / imageStd)
                        imageNormalizePixels[i * 3 + 1] = ((`val` shr 8 and 0xFF) - imageMean / imageStd)
                        imageNormalizePixels[i * 3 + 2] = ((`val` and 0xFF) - imageMean / imageStd)
                }
        }

}
