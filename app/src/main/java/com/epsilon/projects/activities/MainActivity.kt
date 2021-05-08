package com.epsilon.projects.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.epsilon.projects.R
import com.epsilon.projects.adapters.HouseholdItemsAdapter
import com.epsilon.projects.firebase.FirestoreClass
import com.epsilon.projects.models.Household
import com.epsilon.projects.models.User
import com.epsilon.projects.utils.Constants
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_chore_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    /**
     * A companion object to declare the constants.
     */
    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        const val CREATE_HOUSE_REQUEST_CODE: Int = 12
    }

    // A global variable for User Name
    private lateinit var mUserName: String

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)

        // This is used to align the xml view to this class
        setContentView(R.layout.activity_main)

        setupActionBar()

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        nav_view.setNavigationItemSelectedListener(this)

        // Get the current logged in user details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)

        fab_create_household.setOnClickListener {
            val intent = Intent(this, CreateHouseHoldActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_HOUSE_REQUEST_CODE)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is added in Base Activity.
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {

                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }

            R.id.nav_sign_out -> {
                // Here sign outs the user from firebase in this device.
                FirebaseAuth.getInstance().signOut()

                // Send the user to the intro screen of the application.
                val intent = Intent(this, IntroActivty::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            // Get the user updated details.
            FirestoreClass().loadUserData(this)
        } else if (resultCode == Activity.RESULT_OK
            && requestCode == CREATE_HOUSE_REQUEST_CODE
        ) {
            // Get the latest boards list.
            FirestoreClass().getHouseList(this)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    /**
     * A function for opening and closing the Navigation Drawer.
     */
    private fun toggleDrawer() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * A function to get the current user details from firebase.
     */
    fun updateNavigationUserDetails(user: com.epsilon.projects.models.User, readHouseholdList: Boolean) {

        hideProgressDialog()

        mUserName = user.name

        // The instance of the header view of the navigation view.
        val headerView = nav_view.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val nav_user_image = headerView.findViewById<ImageView>(R.id.nav_user_image)

        // Load the user image in the ImageView.
        Glide
            .with(this)
            .load(user.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder
            .into(nav_user_image)

        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername.text = user.name


        if (readHouseholdList) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getHouseList(this)
        }
    }

    /**
     * A function to populate the result of BOARDS list in the UI i.e in the recyclerView.
     */
    fun populateHouseholdListToUI(householdList: ArrayList<Household>) {

        hideProgressDialog()

        if (householdList.size > 0) {

            rv_houses_list.visibility = View.VISIBLE
            tv_no_houses_available.visibility = View.GONE

            rv_houses_list.layoutManager = LinearLayoutManager(this)
            rv_houses_list.setHasFixedSize(true)

            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter = HouseholdItemsAdapter(this, householdList)
            rv_houses_list.adapter = adapter // Attach the adapter to the recyclerView.

            adapter.setOnClickListener(object :
                HouseholdItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Household) {
                    val intent = Intent(this@MainActivity, ChoreListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentID)
                    startActivity(intent)
                }
            })
        } else {
            rv_houses_list.visibility = View.GONE
            tv_no_houses_available.visibility = View.VISIBLE
        }
    }


}