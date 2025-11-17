package com.example.mondaycloneapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Item
import java.text.SimpleDateFormat
import java.util.Locale

class ItemAdapter(
    private var items: List<Item>,
    private val listener: OnItemClickListener // Add a listener property
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // Interface to handle click events in the hosting activity
    interface OnItemClickListener {
        fun onItemLongClick(item: Item)
    }

    // This class holds the UI elements for a single row.
    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        val itemName: TextView = view.findViewById(R.id.tv_item_name)
        val itemStatus: TextView = view.findViewById(R.id.tv_item_status)
        val itemDueDate: TextView = view.findViewById(R.id.tv_due_date)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = items[position]
                val context = v?.context
                val intent = Intent(context, UpdateTaskActivity::class.java)
                intent.putExtra("item", item)
                context?.startActivity(intent)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemLongClick(items[position])
                return true
            }
            return false
        }
    }

    // This is called when the RecyclerView needs a new row layout.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    // This is called to bind the data from an Item object to the UI elements.
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name
        holder.itemStatus.text = "Status: ${item.status}"

        if (item.dueDate != null) {
            holder.itemDueDate.text = "Due Date: ${item.dueDate}"
            holder.itemDueDate.visibility = View.VISIBLE
        } else {
            holder.itemDueDate.visibility = View.GONE
        }
    }

    // This tells the RecyclerView how many items are in the list.
    override fun getItemCount(): Int {
        return items.size
    }
}