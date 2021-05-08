package com.epsilon.projects.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.epsilon.projects.activities.MyProfileActivity

object Constants{

    const val USERS: String = "users"

    const val HOUSES:String = "houses"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"

    const val ASSIGNED_TO : String = "assignedTo"

    // Unique codes for IDing which permission is needed to be allowed
    const val  READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2


    const val DOCUMENT_ID : String = "documentID"
    const val CHORE_LIST: String = "choreList"

    // Handles the user's new profile image from their phone storage
    fun showImageChooser(activity:Activity){
        // Stores the user's selected image from their phone storage
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Launches the image selection update process
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    // A function to get the necessary extension for the image
    fun getFileExtension(activity: Activity, uri: Uri?): String?{
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
                .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}
