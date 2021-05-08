package com.epsilon.projects.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.epsilon.projects.R
import com.epsilon.projects.firebase.FirestoreClass
import com.epsilon.projects.models.Household
import com.epsilon.projects.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_house_hold.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class CreateHouseHoldActivity : BaseActivity() {

    private var mSelectedImageFileUri : Uri? = null

    private lateinit var mUserName: String

    private var mHouseholdImageURL : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_house_hold)

        setupActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        iv_household_image.setOnClickListener{
            // Handles if permissions for phone storage is allowed
            if(ContextCompat.checkSelfPermission(
                            this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)

            } else{
                // Asks for permission to access the phones service from the AndroidManifest
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_create.setOnClickListener{
            if(mSelectedImageFileUri != null){
                uploadHouseImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createHouse()
            }
        }

    }

    private fun createHouse(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        var house = Household(
                et_household_name.text.toString(),
                mHouseholdImageURL,
                mUserName,
                assignedUsersArrayList
        )

        FirestoreClass().createHouse(this, house)
    }

    private fun uploadHouseImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        // Creating the reference id for storage
        val sRef : StorageReference = FirebaseStorage.getInstance()
                .reference.child("Household_IMAGE" + System.currentTimeMillis()
                        + "." + Constants.getFileExtension(this,mSelectedImageFileUri))

        // Adding the file reference string to Firebase cloud
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
            // Image upload successful
            taskSnapshot ->
            Log.i(
                    "Household Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )
            // Get the URL form the snapshot above
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                uri ->
                Log.i("Downloadable Image URL", uri.toString())
                // Sets the url to a variable string
                mHouseholdImageURL = uri.toString()

                createHouse()
            }
        }.addOnFailureListener{
            exception ->
            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }


    }

    fun houseCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    // Sets up the Actionbar
    private fun setupActionBar(){
        setSupportActionBar(toolbar_create_household_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_household_title)
        }

        toolbar_create_household_activity.setNavigationOnClickListener{
            onBackPressed() }


    }

    // A function that handles the runtime requested permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // If permission is granted
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                // If permission is denied
                Toast.makeText(
                        this,
                        "Permission was denied for storage access. Allow it in settings",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
                && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
                && data!!.data != null) {
            // The uri of the selected image from phone storage
            mSelectedImageFileUri = data.data
            try {
                // Try to load the user image in the ImageView
                Glide
                        .with(this)
                        .load(mSelectedImageFileUri) // URI of the image
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder) // default placeholder image
                        .into(iv_household_image) // the view in which the image will be loaded

            }catch (e: IOException){
                e.printStackTrace()
            }
        }

    }

}