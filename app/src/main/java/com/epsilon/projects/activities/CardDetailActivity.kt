package com.epsilon.projects.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.epsilon.projects.R
import com.epsilon.projects.adapters.CardMembersListItemsAdapter
import com.epsilon.projects.dialog.MembersListDialog
import com.epsilon.projects.firebase.FirestoreClass
import com.epsilon.projects.models.*
import com.epsilon.projects.utils.Constants
import kotlinx.android.synthetic.main.activity_card_detail.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CardDetailActivity : BaseActivity() {
    // These global variables handle information needed to pass to Firebase
    // Each relates to data needed to be stored and provides users with accurate accounts
    private lateinit var mHouseholdDetails : Household
    private var mChoreListPosition = -1
    private var mCardPosition = -1
    private lateinit var mMembersDetailsList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_detail)
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
        getIntentData()
        setupActionBar()

        et_name_card_details.setText(mHouseholdDetails.choreList[mChoreListPosition].cards[mCardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        btn_update_card_details.setOnClickListener{
            if(et_name_card_details.text.toString().isNotEmpty()){
                updateCardDetails()
            }
            else{
                Toast.makeText(this, "Enter a card name.", Toast.LENGTH_SHORT).show()
            }
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds = mHouseholdDetails.choreList[mChoreListPosition]
                .cards[mCardPosition].dueDate
        if(mSelectedDueDateMilliSeconds > 0){
            val simpleDataFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDataFormat.format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text = selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDataPicker()
        }
    }

    // Sets up the Actionbar
    private fun setupActionBar(){
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mHouseholdDetails.choreList[mChoreListPosition].cards[mCardPosition].name
        }

        toolbar_card_details_activity.setNavigationOnClickListener{onBackPressed()}
    }
    // inflates the menu to use in the action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handles when action bar items are pressed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card ->{
                alertDialogForDeleteCard( mHouseholdDetails
                        .choreList[mChoreListPosition].cards[mCardPosition].name)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // Handles add the extra pieces of data needed that is sent through intent
    private fun getIntentData(){
        if(intent.hasExtra(Constants.HOUSEHOLD_DETAIL)){
            mHouseholdDetails = intent.getParcelableExtra(Constants.HOUSEHOLD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.CHORE_LIST_ITEM_POSITION)){
            mChoreListPosition = intent.getIntExtra(Constants.CHORE_LIST_ITEM_POSITION, -1)

        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)

        }
        if(intent.hasExtra(Constants.HOUSEHOLD_MEMBERS_LIST)){
            mMembersDetailsList = intent.getParcelableArrayListExtra(
                    Constants.HOUSEHOLD_MEMBERS_LIST)!!
        }
    }

    private fun membersListDialog(){
        var cardAssignedMembersList = mHouseholdDetails.choreList[mChoreListPosition]
                .cards[mCardPosition].assignedTo

        if(cardAssignedMembersList.size > 0){
            for(i in mMembersDetailsList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailsList[i].id == j){
                        mMembersDetailsList[i].selected = true
                    }
                }
            }
        }else{
            for(i in mMembersDetailsList.indices){
                mMembersDetailsList[i].selected = false
            }

        }
        val listDialog = object: MembersListDialog(
                this,
                mMembersDetailsList,
                resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mHouseholdDetails.choreList[mChoreListPosition]
                                    .cards[mCardPosition].assignedTo.contains(user.id)){
                        mHouseholdDetails.choreList[mChoreListPosition]
                                .cards[mCardPosition].assignedTo.add(user.id)
                    }
                }else{
                    mHouseholdDetails.choreList[mChoreListPosition]
                            .cards[mCardPosition].assignedTo.remove(user.id)
                    for(i in mMembersDetailsList.indices){
                        if(mMembersDetailsList[i].id == user.id){
                            mMembersDetailsList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    // Handles when a chore list is added or updated
    fun addUpdateChoreListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    // Updates the card details
    private fun updateCardDetails(){
        // Stores the updated card information (name, createdBy, assignedTo, and due date)
        val card = Card(
        et_name_card_details.text.toString(),
        mHouseholdDetails.choreList[mChoreListPosition].cards[mCardPosition].createdBy,
        mHouseholdDetails.choreList[mChoreListPosition].cards[mCardPosition].assignedTo,
                mSelectedDueDateMilliSeconds
        )

        val choreList: ArrayList<Chore> = mHouseholdDetails.choreList
        choreList.removeAt(choreList.size - 1)

        // Uses the card position to assign the updated card details
        mHouseholdDetails.choreList[mChoreListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateChoreList(this, mHouseholdDetails )
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mHouseholdDetails.choreList[mChoreListPosition].cards
        cardsList.removeAt(mCardPosition)

        val choreList: ArrayList<Chore> = mHouseholdDetails.choreList
        choreList.removeAt(choreList.size-1)

        choreList[mChoreListPosition].cards = cardsList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateChoreList(this, mHouseholdDetails )


    }

    // Handles the delete warning when deleting a card
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
                resources.getString(
                        R.string.confirmation_message_to_delete_card,
                        cardName
                )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            deleteCard()
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMemberList = mHouseholdDetails.choreList[mChoreListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
        for(i in mMembersDetailsList.indices){
            for(j in cardAssignedMemberList){
                if(mMembersDetailsList[i].id == j){
                    val selectedMember = SelectedMembers(
                            mMembersDetailsList[i].id,
                            mMembersDetailsList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }
        if(selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("", ""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            rv_selected_members_list.layoutManager = GridLayoutManager(this, 6)

            val adapter = CardMembersListItemsAdapter(this, selectedMembersList, true)

            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(
                    object: CardMembersListItemsAdapter.OnClickListener{
                        override fun onClick() {
                            membersListDialog()
                        }

                    }
            )
        }else{
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year =
                c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day
        val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                    // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                    val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                    val sMonthOfYear =
                            if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                    val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                    // Selected date it set to the TextView to make it visible to user.
                    tv_select_due_date.text = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                    // The formatter will parse the selected date in to Date object
                    // so we can simply get date in to milliseconds.
                    val theDate = sdf.parse(selectedDate)
                    mSelectedDueDateMilliSeconds = theDate!!.time
                },
                year,
                month,
                day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }

}