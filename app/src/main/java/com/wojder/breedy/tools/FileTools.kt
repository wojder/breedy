package com.wojder.breedy.tools

import android.content.res.AssetManager
import com.wojder.breedy.imageClassifier.ASSETS_PATH
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path

fun getLabels(assetManager: AssetManager, labelFilePath: String): List<String> {
    val actualFileName = getLabelsFileName(labelFilePath)
    return getLabelsFromFile(assetManager, actualFileName)
}

fun getLabelsFromFile(assetManager: AssetManager, actualFileName: String): ArrayList<String> {
    val labels = ArrayList<String>()
    BufferedReader(InputStreamReader(assetManager.open(actualFileName))).use {
        var line: String? = it.readLine()
        while (line != null) {
            labels.add(line)
            line = it.readLine()
        }
        it.close()
    }
    return labels
}

private fun getLabelsFileName(labelFilePath: String): String {
    return labelFilePath.split(ASSETS_PATH.toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()[1]
}
