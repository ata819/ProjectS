package com.epsilon.projects.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager

import androidx.recyclerview.widget.LinearLayoutManager
import com.epsilon.projects.R
import com.epsilon.projects.adapters.ChoreListItemsAdapter
import com.epsilon.projects.firebase.FirestoreClass
import com.epsilon.projects.models.Card
import com.epsilon.projects.models.Chore
import com.epsilon.projects.models.Household
import com.epsilon.projects.models.User
import com.epsilon.projects.utils.Constants
import kotlinx.android.synthetic.main.activity_chore_list.*


class ChoreListActivity : BaseActivity() {

    private lateinit var mHouseDetails: Household

    private lateinit var mHouseholdDocumentId: String

    lateinit var mAssignedMembersDeatailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chore_list)
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

        mHouseholdDocumentId = ""
        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mHouseholdDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getHouseDetails(this, mHouseholdDocumentId )


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getHouseDetails(this, mHouseholdDocumentId )
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun cardDetails(choreListPosition: Int, cardPosition: Int){
        val intent = Intent(this, CardDetailActivity::class.java)
        intent.putExtra(Constants.HOUSEHOLD_DETAIL, mHouseDetails)
        intent.putExtra(Constants.CHORE_LIST_ITEM_POSITION, choreListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.HOUSEHOLD_MEMBERS_LIST, mAssignedMembersDeatailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.HOUSEHOLD_DETAIL, mHouseDetails)
                startActivityForResult(intent, MEMBER_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Sets up the Actionbar
    private fun setupActionBar(){
        setSupportActionBar(toolbar_chore_list_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mHouseDetails.name
        }

        toolbar_chore_list_activity.setNavigationOnClickListener{onBackPressed()}
    }

    fun householdDetails(household: Household){

        mHouseDetails = household
        hideProgressDialog()
        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this, mHouseDetails.assignedTo)

    }

    fun updateChoreList(position: Int, listName: String, model: Chore) {

        val chore = Chore(listName, model.createdBy)

        mHouseDetails.choreList[position] = chore
        mHouseDetails.choreList.removeAt(mHouseDetails.choreList.size - 1)

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateChoreList(this, mHouseDetails)
    }

    fun deleteChoreList(position: Int) {

        mHouseDetails.choreList.removeAt(position)

        mHouseDetails.choreList.removeAt(mHouseDetails.choreList.size - 1)

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateChoreList(this, mHouseDetails)
    }

    fun addUpdateChoreListSuccess(){
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getHouseDetails(this, mHouseDetails.documentID)
    }

    fun createChoreList(choreListName: String){
        val chore = Chore(choreListName, FirestoreClass().getCurrentUserID())
        mHouseDetails.choreList.add(0, chore)
        mHouseDetails.choreList.removeAt(mHouseDetails.choreList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateChoreList(this, mHouseDetails)
    }

    fun addCardToChoreList(position: Int, cardName: String){
        mHouseDetails.choreList.removeAt(mHouseDetails.choreList.size-1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())

        val card = Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = mHouseDetails.choreList[position].cards
        cardsList.add(card)

        val chore = Chore(
                mHouseDetails.choreList[position].title,
                mHouseDetails.choreList[position].createdBy,
                cardsList
        )

        mHouseDetails.choreList[position] = chore

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateChoreList(this, mHouseDetails)

    }

    fun householdMembersDetailsList(list: ArrayList<User>){
        mAssignedMembersDeatailList = list
        hideProgressDialog()


        val addChoreList = Chore(resources.getString(R.string.add_list))
        mHouseDetails.choreList.add(addChoreList)

        rv_chore_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_chore_list.setHasFixedSize(true)
        val adapter = ChoreListItemsAdapter(this, mHouseDetails.choreList )
        rv_chore_list.adapter = adapter
    }

    companion object{
        const val MEMBER_REQUEST_CODE : Int = 13
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }

}