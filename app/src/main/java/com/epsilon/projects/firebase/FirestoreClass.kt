package com.epsilon.projects.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.epsilon.projects.activities.*
import com.epsilon.projects.models.Household
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

    fun getHouseDetails(activity: ChoreListActivity, documentId : String){
        mFireStore.collection(Constants.HOUSES)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                val household = document.toObject(Household::class.java)!!
                household.documentID = document.id
                activity.householdDetails(household)

            }.addOnFailureListener{ e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error in household retrieval")

            }
    }


    fun createHouse(activity: CreateHouseHoldActivity, house: Household){
        mFireStore.collection(Constants.HOUSES)
                .document()
                .set(house, SetOptions.merge())
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName, "Household created successfully")
                    Toast.makeText(activity, "Household created successfully", Toast.LENGTH_SHORT).show()
                    activity.houseCreatedSuccessfully()
                }.addOnFailureListener{
                    exception ->
                    activity.hideProgressDialog()
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating household",
                            exception
                    )
                }
    }

    fun getHouseList(activity: MainActivity){
        mFireStore.collection(Constants.HOUSES)
                .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
                .get()
                .addOnSuccessListener {
                    document ->
                    Log.i(activity.javaClass.simpleName, document.documents.toString())
                    val houseList: ArrayList<Household> = ArrayList()
                    for(i in document.documents){
                        val house = i.toObject(Household::class.java)!!
                        house.documentID = i.id
                        houseList.add(house)
                    }

                    activity.populateHouseholdListToUI(houseList)
                }.addOnFailureListener{ e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error in household retrieval")

                }
    }

    fun addUpdateChoreList(activity: ChoreListActivity, house:Household){
        val choreListHashMap = HashMap<String, Any>()
        choreListHashMap[Constants.CHORE_LIST] = house.choreList

        mFireStore.collection(Constants.HOUSES)
                .document(house.documentID)
                .update(choreListHashMap)
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName, "Chore list has been upload success.")

                    activity.addUpdateChoreListSuccess()
                }.addOnFailureListener{
                    exception ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating house", exception)
                }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data has updated successfully ")
                Toast.makeText(activity, "Profile Updated Success", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener{
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                Toast.makeText(activity, "Error when updating the profile", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserData(activity: Activity, readHouseholdList: Boolean = false){
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
                                activity.updateNavigationUserDetails(loggedInUser, readHouseholdList)
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