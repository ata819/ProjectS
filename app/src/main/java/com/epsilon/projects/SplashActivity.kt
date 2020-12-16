package com.epsilon.projects

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowInsets
import android.view.WindowManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val typeFace: Typeface = Typeface.createFromAsset(assets,"SFQuartzite.ttf")
        //tv_app_name.typeface = typeFace
        Handler().postDelayed({
            startActivity(Intent(this,IntroActivty::class.java))
            finish()
        }, 2500)
    }
}