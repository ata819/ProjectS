package com.epsilon.projects.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

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
    const val HOUSEHOLD_DETAIL: String = "household_details"
    const val ID: String = "id"
    const val EMAIL: String = "email"
    const val HOUSEHOLD_MEMBERS_LIST: String = "household_members_list"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"

    const val PROJECTS_PREFERENCES = "projectSPrefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    const val CHORE_LIST_ITEM_POSITION: String = "chore_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAsDBtSnY:APA91bEXGxVM9axgSk7sO12NENl9T_dTWmddCt7YwgVlh4J7Wrf6DzvOrQX0XNnhWEslXD8WvYTW5x9lj6Opur8dF0NAGSVau4TDncDKG5l-qbc3vInaaKBOJZiyEN7PaMEri-4Qirtq "
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"


    // Handles the user's new profile image from their phone storage
    fun showImageChooser(activity:Activity){
        // Stores the user's selected image from their phone storage
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Launches the image selection update process
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    // A function to get the necessary extension for the image
    fun getFileExtension(activity: Activity, uri: Uri?): String?{
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}
