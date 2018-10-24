package com.wojder.breedy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.wojder.breedy.tools.ImageTools
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.wojder.breedy.imageClassifier.*
import com.wojder.breedy.tools.getUriFromFilePath
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

private const val REQUEST_PERMISSION = 1
private const val REQUEST_TAKE_PICTURE = 2
private const val READ_REQUEST_CODE: Int = 42
private const val FILE_FROM_MEMORY: Int = 101

class MainActivity : AppCompatActivity() {

    private var filePath = ""
    private lateinit var classifier: Classifier
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
    }

    private fun checkPermissions() {
        if (arePermsAlreadyGranted()) {
            init()
        } else {
            requestPermissions()
        }
    }

    private fun init() {
        createImageClassifier()
        takePhoto()

    }

    private fun createImageClassifier() {
        classifier = ImageClassifierFactory.createClassifier(
                assets,
                GRAPH_FILE_PATH,
                LABELS_FILE_PATH,
                IMAGE_SIZE,
                GRAPH_INPUT_NAME,
                GRAPH_OUTPUT_NAME
        )
    }

    private fun takePhoto() {
        filePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).absolutePath + "/${System.currentTimeMillis()}.jpg"

        val photoUri = getUriFromFilePath(this, filePath)

        val photoIntent = Intent(ACTION_IMAGE_CAPTURE)
        photoIntent.putExtra(EXTRA_OUTPUT, photoUri)
        photoIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        if (photoIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(photoIntent, REQUEST_TAKE_PICTURE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION && arePermsGranted(grantResults)) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_TAKE_PICTURE) {
            val file = File(filePath)
            //mamy zdjęcie
            Toast.makeText(this, "MAMY zdjęcie", Toast.LENGTH_LONG).show()

            classifyTakenPhoto(file)
        } else if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "MAMY plik z pamięci", Toast.LENGTH_LONG).show()
            if (data != null) {
                val uri = data.data
                Log.d("URI", uri.toString())
                val mimeType = getMimeTypeFromUri(uri)
                uri.path
                val finalPath :String = uri.path + "/${System.currentTimeMillis()}" + getExtensionFromUri(mimeType)
                //val fileUri = getUriFromFilePath(this, uri.path)
                Toast.makeText(this, "Plik$finalPath", Toast.LENGTH_LONG).show()
                uri.queryParameterNames
                uri.authority
                val finalFileName = getFileFromPath(finalPath)
                val file = File(finalFileName)
                uri.scheme
            }
        }
    }

    private fun getFileFromPath(finalPath: String) : String{
        return finalPath.substringAfterLast("/")
    }

    private fun getFileNameFromFile(file: File): String {
        return file.name
    }

    private fun getExtensionFromUri(mimeType: String?): String {

        return if (mimeType.equals("application/pdf")) {
            ".pdf"
        } else {
            ".jpg"
        }
    }

    private fun getMimeTypeFromUri(uri: Uri): String? {
        val mimeType: String? = uri.let { returnUri ->
            contentResolver.getType(returnUri)
        }
        return mimeType
    }

    private fun classifyTakenPhoto(file: File) {
        val photoBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val croppedBitmap = ImageTools().getCroppedBitmap(photoBitmap)
        classifyAndShowResult(croppedBitmap)
        imagePhoto.setImageBitmap(photoBitmap)
    }

    private fun classifyAndShowResult(croppedBitmap: Bitmap) {
        runInBackground(
                Runnable {
                    val result = classifier.recognizeImage(croppedBitmap)
                    showResult(result)
                }
        )
    }

    @Synchronized
    private fun runInBackground(runnable: Runnable) {
        handler.post(runnable)
    }

    private fun showResult(result: Result) {
        textResult.text = result.result.toUpperCase()
        textConfidence.text = result.confidence.toString()
        layoutContainer.setBackgroundColor(getColorFromResult(result.result))
    }

    @Suppress("DEPRECATION")
    private fun getColorFromResult(result: String): Int {
        return when (result) {
            getString(R.string.human) -> resources.getColor(R.color.human)
            getString(R.string.boxer) -> resources.getColor(R.color.boxer)
            else -> resources.getColor(R.color.shephard)
        }

    }

    private fun arePermsGranted(grantResults: IntArray) =
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED


    private fun arePermsAlreadyGranted() =
            ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.take_photo -> {
            // do stuff
            takePhoto()
            true
        }
        R.id.take_file -> {
            takeFileFromMemory()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun takeFileFromMemory() {
        performFileSearch()
        takeUri()
    }

    private fun takeUri() {

    }

    private fun performFileSearch() {
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(fileIntent, READ_REQUEST_CODE)

    }
}
