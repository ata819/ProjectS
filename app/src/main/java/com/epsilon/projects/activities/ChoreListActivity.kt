package com.epsilon.projects.activities

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.epsilon.projects.R
import com.epsilon.projects.adapters.ChoreListItemsAdapter
import com.epsilon.projects.firebase.FirestoreClass
import com.epsilon.projects.models.Card
import com.epsilon.projects.models.Chore
import com.epsilon.projects.models.Household
import com.epsilon.projects.utils.Constants
import kotlinx.android.synthetic.main.activity_chore_list.*
import kotlinx.android.synthetic.main.item_chore.*

class ChoreListActivity : BaseActivity() {

    private lateinit var mHouseDetails: Household

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chore_list)

        var choreDocumentId = ""
        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            choreDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getHouseDetails(this, choreDocumentId )


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

        val addChoreList = Chore(resources.getString(R.string.add_list))
        household.choreList.add(addChoreList)

        rv_chore_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_chore_list.setHasFixedSize(true)
        val adapter = ChoreListItemsAdapter(this, household.choreList )
        rv_chore_list.adapter = adapter

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

}