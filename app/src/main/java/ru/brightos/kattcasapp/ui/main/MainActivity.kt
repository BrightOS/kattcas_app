package ru.brightos.kattcasapp.ui.main

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking
import ru.brightos.kattcasapp.App
import ru.brightos.kattcasapp.R
import ru.brightos.kattcasapp.ui.main.console.ConsoleViewModel
import java.io.File

class MainActivity : AppCompatActivity() {

    var openScreenID: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupWithNavController(
            bottom_navigation, findNavController(main_container)
        )
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ПАДЛА", "ДАЙ ПРАВА")
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    11234
                );
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    11234
                );
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 11234) {
            val folder = File(
                Environment.getExternalStorageDirectory().toString() + "/Scripts"
            )

            if (!folder.exists())
                folder.mkdir()

            Log.e("УРА", "ПАПКА СОЗДАЛАСЬ ЕБАТЬ")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean =
        Navigation.findNavController(this, R.id.main_container).navigateUp()

    override fun startActivity(intent: Intent?) {
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}