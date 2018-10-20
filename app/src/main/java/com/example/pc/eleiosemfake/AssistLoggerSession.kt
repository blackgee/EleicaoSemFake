package com.tomer.screenshotsharer

import android.animation.Animator
import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.example.pc.eleiosemfake.Parameters
import com.example.pc.eleiosemfake.R


class AssistLoggerSession(context: Context) : VoiceInteractionSession(context) {


	override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
		super.onHandleAssist(data, structure, content)
		Log.d("Data", data!!.toString())
	}
	
	override fun onHandleScreenshot(screenshot: Bitmap?) {
		super.onHandleScreenshot(screenshot)
		Log.d(AssistLoggerSession::class.java.simpleName, "Received screenshot")
		if (Settings.canDrawOverlays(context)) {
			showPreviewAndFinish(screenshot)
		} else {
			sendEmail(screenshot)
			finish()
		}
	}
	
	private fun showPreviewAndFinish(screenshot: Bitmap?) {
		val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
		val rootView = layoutInflater.inflate(R.layout.image_view, null)
		val imageView = rootView.findViewById(R.id.image_view) as ImageView
		imageView.alpha = 0.0f
		imageView.setImageBitmap(screenshot)
		val params = WindowManager.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				0,
				PixelFormat.TRANSLUCENT)
		windowManager.addView(rootView, params)
		Handler().post {
			imageView.animate().alpha(1.0f).setDuration(700).setListener(object : Animator.AnimatorListener {
				override fun onAnimationStart(animation: Animator) {
				
				}
				
				override fun onAnimationEnd(animation: Animator) {
					sendEmail(screenshot)
					windowManager.removeView(rootView)
					finish()
				}
				
				override fun onAnimationCancel(animation: Animator) {
				
				}
				
				override fun onAnimationRepeat(animation: Animator) {
				
				}
			})
		}
	}

	private fun sendEmail(bitmap: Bitmap?) {
		if (!canWriteExternalPermission()) {
			val mainActivity = Intent(context, MainActivity::class.java)
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(mainActivity)
			return
		}

		val sharedPref = context.getSharedPreferences(Parameters.SHARED_PREF, Context.MODE_PRIVATE)
		val pathToScreenshot = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap,
				"screenshot", null)
		val bmpUri = Uri.parse(pathToScreenshot)
		val mailIntent = Intent(android.content.Intent.ACTION_SEND)
		mailIntent.type = "message/rfc822"
		mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		mailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("denuncia.eleicao.ce@dpf.gov.br", "aic@tse.jus.br", "presidencia@tse.jus.br"))
		val name = sharedPref?.getString(Parameters.USER_NAME, "")
		val id = sharedPref?.getString(Parameters.DOCUMENT_INFO, "")
		mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Denúncia Fake News: $name - $id")
		mailIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
		var emailBody = context.resources.getString(R.string.email_body)
		if (sharedPref.getBoolean(Parameters.ADD_BODY, false)) {
			emailBody += context.resources.getString(R.string.email_additional_body)
		}
		mailIntent.putExtra(Intent.EXTRA_TEXT, emailBody)
		mailIntent.type = "image/jpeg"
		context.startActivity(mailIntent)
	}
	
	private fun canWriteExternalPermission(): Boolean {
		val permission = "android.permission.WRITE_EXTERNAL_STORAGE"
		val res = context.checkCallingOrSelfPermission(permission)
		return res == PackageManager.PERMISSION_GRANTED
	}


}
