package com.wojder.breedy

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaActionSound
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.wojder.breedy.tools.getUriFromPhotoPath
import java.io.File

private const val REQUEST_PERMISSION = 1
private const val REQUEST_TAKE_PICTURE = 2

class MainActivity : AppCompatActivity() {

    private var photoFilePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
    }

    private fun checkPermissions() {
        if (arePermsAlreadyGranted()) {
            takePhoto()
        } else {
            requestPermissions()
        }
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
        } else -> super.onOptionsItemSelected(item)
    }
}
