package com.epsilon.projects.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epsilon.projects.R
import com.epsilon.projects.activities.ChoreListActivity
import com.epsilon.projects.models.Card
import com.epsilon.projects.models.SelectedMembers
import kotlinx.android.synthetic.main.item_card.view.*

open class CardListItemsAdapter (
        private val context: Context,
        private var list: ArrayList<Card>

        ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
            private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_card,
                        parent,
                        false

                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            holder.itemView.tv_card_name.text = model.name

            if((context as ChoreListActivity).mAssignedMembersDeatailList.size > 0){
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                for(i in context.mAssignedMembersDeatailList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMembersDeatailList[i].id == j){
                            val selectedMembers = SelectedMembers(
                                    context.mAssignedMembersDeatailList[i].id,
                                    context.mAssignedMembersDeatailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }

                }
                if(selectedMembersList.size > 0){
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                    }else{
                        holder.itemView.rv_card_selected_members_list.visibility = View.VISIBLE

                        holder.itemView.rv_card_selected_members_list.layoutManager =
                                GridLayoutManager(context, 4)
                        val adapter = CardMembersListItemsAdapter(context, selectedMembersList, false)
                        holder.itemView.rv_card_selected_members_list.adapter = adapter
                        adapter.setOnClickListener(
                                object: CardMembersListItemsAdapter.OnClickListener{
                                    override fun onClick() {
                                        if(onClickListener != null){
                                            onClickListener!!.onClick(position)
                                        }
                                    }
                                }
                        )

                    }
                }else{
                    holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickerListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}