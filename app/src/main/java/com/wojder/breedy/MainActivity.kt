package com.wojder.breedy

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.wojder.breedy.tools.ImageTools
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.wojder.breedy.imageClassifier.*
import com.wojder.breedy.tools.getUriFromPhotoPath
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import com.wojder.breedy.Result

private const val REQUEST_PERMISSION = 1
private const val REQUEST_TAKE_PICTURE = 2

class MainActivity : AppCompatActivity() {

    private var photoFilePath = ""
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
        photoFilePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).absolutePath + "/${System.currentTimeMillis()}.jpg"

        val photoUri = getUriFromPhotoPath(this, photoFilePath)

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
        val file = File(photoFilePath)
        if (requestCode == REQUEST_TAKE_PICTURE && file.exists()) {
            //mamy zdjęcie
            Toast.makeText(this, "MAMY zdjęcie", Toast.LENGTH_LONG).show()

            classifyTakenPhoto(file)
        }
    }

    private fun classifyTakenPhoto(file: File) {
        val photoBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val croppedBitmap = ImageTools().getCroppedBitmap(photoBitmap)
        classifyAndShowResult(croppedBitmap)
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
        layoutContainer.setBackgroundColor(getColorFromResult(result.result))
    }

    @Suppress("DEPRECATION")
    private fun getColorFromResult(result: String): Int {
        return if (result == getString(R.string.boxer)) {
            resources.getColor(R.color.boxer)
        } else {
            resources.getColor(R.color.human)
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
        else -> super.onOptionsItemSelected(item)
    }
}
