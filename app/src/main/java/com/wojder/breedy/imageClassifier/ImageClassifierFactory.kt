package com.wojder.breedy.imageClassifier

import android.content.res.AssetManager
import com.wojder.breedy.Classifier
import com.wojder.breedy.ImageClassifier
import com.wojder.breedy.tools.getLabels
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

object ImageClassifierFactory {
    fun createClassifier(
            assetManager: AssetManager,
            graphFilePath: String,
            labelsFilePath: String,
            imageSize: Int,
            inputName: String,
            outputName: String
    ): Classifier {

        val labels = getLabels(assetManager, labelsFilePath)

        return ImageClassifier(
                inputName,
                outputName,
                imageSize.toLong(),
                labels,
                IntArray(imageSize * imageSize),
                FloatArray(imageSize * imageSize * COLOR_CHANNELS),
                FloatArray(labels.size),
                TensorFlowInferenceInterface(assetManager, graphFilePath)
                )
    }
}
