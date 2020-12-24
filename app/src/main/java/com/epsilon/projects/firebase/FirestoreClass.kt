package com.epsilon.projects.firebase

import android.util.Log
import com.epsilon.projects.activities.SignInActivity
import com.epsilon.projects.activities.SignUpActivity
import com.epsilon.projects.models.User
import com.epsilon.projects.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity, userInfo : User){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener {activity.userRegisteredSuccess()
                }.addOnFailureListener{
                    e->
                    Log.e(activity.javaClass.simpleName, "Error")
                }

    }

    fun signInUser(activity: SignInActivity){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)
                    if(loggedInUser != null)
                        activity.signInSuccess(loggedInUser)
                }.addOnFailureListener{
                    e->
                    Log.e("SignInUser", "Error Writing Document", e)
                }


    }

    fun getCurrentUserID(): String{

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
        //return FirebaseAuth.getInstance().currentUser!!.uid
    }

}