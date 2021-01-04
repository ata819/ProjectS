package com.epsilon.projects.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.epsilon.projects.R
import com.epsilon.projects.firebase.FirestoreClass
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        // A unique ID for starting the activity for reult
        const val MY_PROFILE_REQUEST_CODE : Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Calls the parent constructor
        super.onCreate(savedInstanceState)

        // Used to unify xml code with this class functions
        setContentView(R.layout.activity_main)

        // To hide the status bar depending on the API
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

        // Activates the navigation view to this class
        nav_view.setNavigationItemSelectedListener(this)

        // Calls Firestore to load the user data
        FirestoreClass().loadUserData(this)
    }

    // Sets up the Actionbar that is for the drawer
    private fun setupActionBar(){
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener{
            toggleDrawer()
        }

    }

    // Handles the Draw function to slide out and reveal the profile and SIGN OUT button
    private fun toggleDrawer(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else{
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    // Handles the back arrow function for screen and if the user profile draw is drawn
    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else{
            doubleBackToExit()
        }
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    // A function to retrieve the user data from Firebase
    fun updateNavigationUserDetails(user: com.epsilon.projects.models.User){
        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(nav_user_image)

        tv_username.text = user.name

    }

    // The navigation items for the ActionBar
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Launches My Profile option in the nav bar
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }

            // Activates the SIGN OUT function and returns to SIGN IN & SIGN UP screen
            R.id.nav_sign_out -> {
                // signs out the user from Firebase
                FirebaseAuth.getInstance().signOut()

                //Sends the user back to the IntroActivity (SIGN IN & UP)
                val intent = Intent(this, IntroActivty::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


}