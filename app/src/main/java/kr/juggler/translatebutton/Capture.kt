package kr.juggler.translatebutton;

import android.content.Context
import android.content.Intent

object Capture {
    fun handleScreenCaptureIntentResult(
        context: Context,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        return true
    }
    fun onInitialize(context: Context) {

    }
}
