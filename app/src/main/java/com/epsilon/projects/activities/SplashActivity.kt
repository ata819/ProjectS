package com.epsilon.projects.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.epsilon.projects.R
import com.epsilon.projects.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val typeFace: Typeface = Typeface.createFromAsset(assets,"SFQuartzite.ttf")
        //tv_app_name.typeface = typeFace
        Handler().postDelayed({

            var currentUserID = FirestoreClass().getCurrentUserID()

            if(currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }
            else{
                startActivity(Intent(this, IntroActivty::class.java))
            }
            finish()
        }, 2500)
    }
}