package com.tomer.screenshotsharer

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.pc.eleiosemfake.R
import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.Bitmap
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import com.example.pc.eleiosemfake.Parameters
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity(), View.OnTouchListener, View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadUserInfo()

//        startService(Intent(this, AssistLoggerService::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        /*menu?.findItem(R.id.github)?.setOnMenuItemClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rosenpin/Screenshot-Sharer"))
                startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
            }
            true
        }*/
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        checkAssistant()
        checkStorageAccess()
        checkPreview()
        assistant.isClickable = false
        storage.isClickable = false
        preview.isClickable = false
        btnSave.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        checkStorageAccess()
    }

    private fun checkAssistant() {
        val currentAssistant = Settings.Secure.getString(contentResolver, "voice_interaction_service")
        assistant.isChecked = currentAssistant != null && (currentAssistant == packageName + "/." + AssistLoggerService::class.java.simpleName || currentAssistant.contains(packageName))
        assistant.setOnTouchListener(this)
    }

    private fun checkStorageAccess() {
        if (canWriteExternalPermission())
            storage.isChecked = true
        else {
            storage.isChecked = false
            storage.setOnTouchListener(this)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun checkPreview() {
        preview.isChecked = Settings.canDrawOverlays(this)
        preview.setOnTouchListener(this)
    }

    private fun canWriteExternalPermission(): Boolean {
        val permission = "android.permission.WRITE_EXTERNAL_STORAGE"
        val res = checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (v.id) {
            R.id.assistant -> {
                startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
                Toast.makeText(this, "Select " + getString(R.string.app_name) + " as your assist app", Toast.LENGTH_SHORT).show()
            }
            R.id.storage -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            R.id.preview -> {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
                Toast.makeText(this, "Permit drawing over other apps for previews", Toast.LENGTH_SHORT).show()
            }

            else -> v.performClick()
        }
        return false
    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.btnSave -> saveUserInfo()
       }
    }

    private fun loadUserInfo() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)

        etName.setText(sharedPref.getString(Parameters.USER_NAME, ""))
        etDocumentNumber.setText(sharedPref.getString(Parameters.DOCUMENT_INFO, ""))

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

    }

    private fun saveUserInfo() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(Parameters.USER_NAME, etName.text.toString())
            putString(Parameters.DOCUMENT_INFO, etDocumentNumber.text.toString())
            commit()
        }
    }

}
