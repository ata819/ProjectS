package com.epsilon.projects.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_intro_activty.*
import android.content.Intent
import com.epsilon.projects.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*

// A class after the splash screen and handles the SIGN IN and SIGN UP page
class IntroActivty : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_activty)

        // To hide the status bar depending on the API
        // Handles API => 30
        @Suppress("DEPRECATION")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        else {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // Launches the SIGN IN screen
        btn_sign_in_intro.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // Launches the SIGN UP screen
        btn_sign_up_intro.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

}