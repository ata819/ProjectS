package com.epsilon.projects.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.epsilon.projects.R
import com.epsilon.projects.firebase.FirestoreClass
import com.epsilon.projects.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
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

        setupActionBar()
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this, "You have " +
                "successfully registered", Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_up_activity.setNavigationOnClickListener{onBackPressed()}

        btn_sign_up.setOnClickListener{
            registerUser()
        }
    }

    // A function used to register a new user into the Firebase (Auth, Cloud, Storage, etc.)
    private fun registerUser(){
        // editText provides the info and is trimmed for spaces
        val name: String = et_name.text.toString().trim{it <= ' '}
        val email: String = et_email.text.toString().trim { it <= ' ' }
        val password: String = et_password.text.toString().trim{it <= ' '}

        // If the info provided is valid
        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))

            // Firebase function that uses the info to create the user account
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid, name, registeredEmail)
                            // Calls the registerUser function to register/add the user info into the Firebase Database
                            FirestoreClass().registerUser(this, user)
                        } else {
                            // If the task fails and is unable to register the user
                            Toast.makeText(this,
                                    "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }

        }
    }

    private fun validateForm(name: String, email: String, password:String) : Boolean{
        return when{
            TextUtils.isEmpty(name) ->{
                showErrorSnackBar("Please enter a name")
                false
            }

            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter an email address")
                false
            }

            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }

        }

    }

}