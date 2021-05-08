package com.epsilon.projects.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.epsilon.projects.R
import com.epsilon.projects.activities.ChoreListActivity
import com.epsilon.projects.models.Chore
import kotlinx.android.synthetic.main.item_chore.view.*

open class ChoreListItemsAdapter(private val context: Context, private var list: ArrayList<Chore>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_chore, parent, false)

        // Creates a layout
        val layoutParams = LinearLayout.LayoutParams(
                (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Here the dynamic margins are applied to the view.
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.layoutParams = layoutParams

        return MyViewHolder(view)
        //return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chore, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            if(position == list.size-1){
                //IMPORTANT
                holder.itemView.tv_add_chore_list.visibility = View.VISIBLE
                holder.itemView.ll_chore_item.visibility = View.GONE
            }else{
                holder.itemView.tv_add_chore_list.visibility = View.GONE
                holder.itemView.ll_chore_item.visibility = View.VISIBLE
            }

            holder.itemView.tv_chore_list_title.text = model.title
            holder.itemView.tv_add_chore_list.setOnClickListener{
                holder.itemView.tv_add_chore_list.visibility = View.GONE
                holder.itemView.cv_add_chore_list_name.visibility = View.VISIBLE
            }

            holder.itemView.ib_close_list_name.setOnClickListener{
                holder.itemView.tv_add_chore_list.visibility = View.VISIBLE
                holder.itemView.cv_add_chore_list_name.visibility = View.GONE
            }

            holder.itemView.ib_done_list_name.setOnClickListener{
                val listName = holder.itemView.et_chore_list_name.text.toString()
                if(listName.isNotEmpty()){
                    if(context is ChoreListActivity){
                        context.createChoreList(listName)
                    }else{
                        Toast.makeText(context, "Please Enter chore Name", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int= (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int= (this * Resources.getSystem().displayMetrics.density).toInt()

    // Describes an item view and its metadata within the recyclerview
    class MyViewHolder(view:View): RecyclerView.ViewHolder(view)
}