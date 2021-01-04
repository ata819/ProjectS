package com.epsilon.projects.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.epsilon.projects.R
import com.epsilon.projects.firebase.FirestoreClass
import com.epsilon.projects.models.User
import com.epsilon.projects.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    // Unique codes for IDing which permission is needed to be allowed
    companion object{
        private const val  READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    // A global variable for the URI for an image on the user's phone
    private var mSelectedImageFileUri: Uri? = null

    // a global variable to store the user's details
    private lateinit var mUserDetails: User

    // a global variable for the user profile URI image
    private var mProfileImageURL : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

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

        // Calls Firestore to load the user data
        FirestoreClass().loadUserData(this)

        // Handles profile image updates when image is pressed
        iv_profile_user_image.setOnClickListener{

            // Handles if permissions for phone storage is allowed
            if(ContextCompat.checkSelfPermission(
                            this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                showImageChooser()

            } else{
                // Asks for permission to access the phones service from the AndroidManifest
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        // Handles updating the user's profile picture
        btn_update.setOnClickListener{
            // If image is not selected, calls to updates the mSelectedImageFileUri
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                // Updates the user's image with mSelectedImageFileUri in the database
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

    }

    // A function that handles the runtime requested permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // If permission is granted
        if(requestCode == READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
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

    // Handles the user's new profile image from their phone storage
    private fun showImageChooser(){
        // Stores the user's selected image from their phone storage
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Launches the image selection update process
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
                && requestCode == PICK_IMAGE_REQUEST_CODE
                && data!!.data != null) {
            // The uri of the selected image from phone storage
            mSelectedImageFileUri = data.data
            try {
                // Try to load the user image in the ImageView
                Glide
                        .with(this)
                        .load(mSelectedImageFileUri) // URI of the image
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder) // default placeholder image
                        .into(iv_profile_user_image) // the view in which the image will be loaded

            }catch (e: IOException){
                e.printStackTrace()
            }
        }

    }


    // Sets up the Actionbar
    private fun setupActionBar(){
        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener{onBackPressed()}
    }

    // A function to set up the existing user details in the UI
    fun setUserDataInUI(user: User){

        mUserDetails = user

        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.mobile != 0L){
            et_mobile.setText(user.mobile.toString())
        }
    }

    // A function used to update the user profile info to the database
    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        if(et_name.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = et_name.text.toString()
        }
        if(et_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }

        // Updates the details to the Firebase database
        FirestoreClass().updateUserProfileData(this, userHashMap)

    }

    // A function to upload the user image to Firebase cloud storage
    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri != null){

            // Creating the reference id for storage
            val sRef : StorageReference = FirebaseStorage.getInstance()
                    .reference.child("USER_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(mSelectedImageFileUri))

            // Adding the file reference string to Firebase cloud
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                // Image upload successful
                taskSnapshot ->
                Log.i(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                // Get the URL form the snapshot above
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    // Sets the url to a variable string
                    mProfileImageURL = uri.toString()
                    // Calls a function to update the profile details to the database
                    updateUserProfileData()
                }
            }.addOnFailureListener{
                exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    // A function to get the necessary extension for the image
    private fun getFileExtension(uri: Uri?): String?{
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    // A function to notify the user on successful profile update
    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}