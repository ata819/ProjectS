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

// A class that handles Google Firebase integration with the backend
class FirestoreClass {

    // Creates an instance of Firebase Firestore to use
    private val mFireStore = FirebaseFirestore.getInstance()

    // Provides registration information to Firebase
    fun registerUser(activity:SignUpActivity, userInfo : User){
        mFireStore.collection(Constants.USERS)
                // provides the documentation of user id
                .document(getCurrentUserID())
                // SetOption is set to merge on userInfo
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener {activity.userRegisteredSuccess()
                }.addOnFailureListener{
                    e->
                    Log.e(activity.javaClass.simpleName, "Error")
                }

    }

    // Retrieves housing details
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

    // Handles the creation of a new household
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

    // Retrieves houses list based on the user from Firebase and presents it in th app
    fun getHouseList(activity: MainActivity){
        mFireStore.collection(Constants.HOUSES)
                // Creates a array query of houses nased on the user and which houses they are assigned to
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

    //Handles the creation or update of chores
    fun addUpdateChoreList(activity: Activity, house:Household){
        val choreListHashMap = HashMap<String, Any>()
        choreListHashMap[Constants.CHORE_LIST] = house.choreList

        mFireStore.collection(Constants.HOUSES)
                .document(house.documentID)
                .update(choreListHashMap)
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName, "Chore list has been upload success.")
                    if(activity is ChoreListActivity){
                        activity.addUpdateChoreListSuccess()
                    }
                    else if(activity is CardDetailActivity){
                        activity.addUpdateChoreListSuccess()
                    }
                }.addOnFailureListener{
                    exception ->
                    if(activity is ChoreListActivity)
                        activity.hideProgressDialog()
                    else if (activity is CardDetailActivity)
                        activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating house", exception)
                }
    }

    // Updates the user profile and sends it to Firebase when the update button is pressed
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS) // Collection name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap field of what to update
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data has updated successfully ")
                Toast.makeText(activity, "Profile Updated Success", Toast.LENGTH_LONG).show()
                when(activity){
                    is MainActivity ->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity ->{
                        activity.profileUpdateSuccess()
                    }

                }
            }.addOnFailureListener{
                e ->
                when(activity){
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                    }

                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                Toast.makeText(activity, "Error when updating the profile", Toast.LENGTH_SHORT).show()
            }
    }

    // Used to retrieve use data from the Firestore database
    fun loadUserData(activity: Activity, readHouseholdList: Boolean = false){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)
                    // Depending on what activity that called for it
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
                    is MyProfileActivity->{
                        activity.hideProgressDialog()
                    }
                }
                    Log.e("SignInUser", "Error Writing Document", e)
                }


    }

    // A function to get the user id opf the currently logged in user
    fun getCurrentUserID(): String{

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
        //return FirebaseAuth.getInstance().currentUser!!.uid
    }

    // A function to get the list of users assign to a specified household
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS) // Collection name
                .whereIn(Constants.ID, assignedTo) // The database field name and IDs of the members
                .get()
                .addOnSuccessListener {
                    document->
                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    val usersList : ArrayList<User> = ArrayList()

                    for(i in document.documents){
                        // Converts all the document snapshots to the objects using the data model class
                        val user = i.toObject(User::class.java)!!
                        usersList.add(user)
                    }
                    if(activity is MembersActivity)
                        activity.setupMembersList(usersList)
                    else if(activity is ChoreListActivity)
                        activity.householdMembersDetailsList(usersList)
                }.addOnFailureListener{ e ->
                    if(activity is MembersActivity)
                        activity.hideProgressDialog()
                    else if(activity is ChoreListActivity)
                        activity.hideProgressDialog()
                    Log.e(
                            activity.javaClass.simpleName, "Error while fetching users", e
                    )
                }
    }

    // A fucntion to get user details from Firestore Database using their email
    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
                .whereEqualTo(Constants.EMAIL, email)
                .get()
                .addOnSuccessListener {
                    document ->
                    if(document.documents.size > 0){
                        val user = document.documents[0].toObject(User::class.java)!!
                        activity.memberDetails(user)
                    }else{
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar("No such member exist")
                    }
                }.addOnFailureListener{ e ->
                    activity.hideProgressDialog()
                    Log.e(
                            activity.javaClass.simpleName, "Error while getting user details", e
                    )
                }
    }
    fun assignMemberToHouse(activity: MembersActivity, household: Household, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = household.assignedTo

        mFireStore.collection(Constants.HOUSES)
                .document(household.documentID)
                .update(assignedToHashMap)
                .addOnSuccessListener {
                    activity.memberAssignSuccess(user)
                }.addOnFailureListener{ e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while add member",e)

                }

    }
}