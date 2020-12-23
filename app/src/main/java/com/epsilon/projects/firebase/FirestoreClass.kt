package com.epsilon.projects.firebase

import com.epsilon.projects.activities.SignUpActivity
import com.epsilon.projects.models.User
import com.epsilon.projects.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity, userInfo : User){
        mFireStore.collection(Constants.USERS)

    }

}