package com.tomer.screenshotsharer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import com.example.pc.eleiosemfake.Parameters
import com.example.pc.eleiosemfake.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnTouchListener, View.OnClickListener {

    var sharedPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadUserInfo()
        sharedPref = getSharedPreferences(Parameters.SHARED_PREF, Context.MODE_PRIVATE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
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
        chkBody.setOnClickListener(this)
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
                Toast.makeText(this, getString(R.string.select_assist_app), Toast.LENGTH_SHORT).show()
            }
            R.id.storage -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            R.id.preview -> {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
                Toast.makeText(this, getString(R.string.permit_drawing_over_apps), Toast.LENGTH_SHORT).show()
            }

            else -> v.performClick()
        }
        return false
    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.btnSave -> saveUserInfo()
           R.id.chkBody -> handleCheckboxEvent(v)
       }
    }

    private fun loadUserInfo() {
        etName.setText(sharedPref?.getString(Parameters.USER_NAME, ""))
        etDocumentNumber.setText(sharedPref?.getString(Parameters.DOCUMENT_INFO, ""))
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun saveUserInfo() {
        with(sharedPref?.edit()) {
            this?.putString(Parameters.USER_NAME, etName.text.toString())
            this?.putString(Parameters.DOCUMENT_INFO, etDocumentNumber.text.toString())
        }?.commit()
        Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
    }

    private fun handleCheckboxEvent(v: View?) {
        var isChecked = false
        if ((v as CheckBox).isChecked) {
            isChecked = true
        }
        with(sharedPref?.edit()) {
            this?.putBoolean(Parameters.ADD_BODY, isChecked)
        }?.commit()
    }

}
