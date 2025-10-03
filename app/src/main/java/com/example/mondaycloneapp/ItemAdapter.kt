package com.example.mondaycloneapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Item

class ItemAdapter(private var items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // This class holds the UI elements for a single row.
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.tv_item_name)
        val itemStatus: TextView = view.findViewById(R.id.tv_item_status)
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
    }

    // This tells the RecyclerView how many items are in the list.
    override fun getItemCount(): Int {
        return items.size
    }
}