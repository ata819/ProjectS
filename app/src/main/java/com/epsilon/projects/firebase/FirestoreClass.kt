package com.epsilon.projects.firebase

import android.app.Activity
import android.util.Log
import com.epsilon.projects.activities.MainActivity
import com.epsilon.projects.activities.MyProfileActivity
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

    fun loadUserData(activity: Activity){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)

                    when(activity){
                        is SignInActivity ->{
                            if(loggedInUser != null)
                                activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity ->{
                            if(loggedInUser != null)
                                activity.updateNavigationUserDetails(loggedInUser)
                        }
                        is MyProfileActivity ->{
                            if(loggedInUser != null)
                                activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }.addOnFailureListener{
                    e->
                when(activity){
                    is SignInActivity ->{
                            activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                            activity.hideProgressDialog()
                    }
                }
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