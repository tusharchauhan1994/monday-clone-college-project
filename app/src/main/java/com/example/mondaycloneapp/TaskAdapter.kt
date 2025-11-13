package com.example.mondaycloneapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Item
import com.example.mondaycloneapp.models.User
import com.google.firebase.database.FirebaseDatabase

private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1

sealed class ListItem {
    data class GroupHeader(val title: String) : ListItem()
    data class TaskItem(val task: Item) : ListItem()
}

class TaskAdapter(private val listItems: List<ListItem>, private val listener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemLongClick(item: Item)
    }

    override fun getItemViewType(position: Int): Int {
        return when (listItems[position]) {
            is ListItem.GroupHeader -> TYPE_HEADER
            is ListItem.TaskItem -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.group_header, parent, false)
            GroupViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listItem = listItems[position]
        if (holder is GroupViewHolder && listItem is ListItem.GroupHeader) {
            holder.bind(listItem)
        } else if (holder is TaskViewHolder && listItem is ListItem.TaskItem) {
            holder.bind(listItem.task, listener)
        }
    }

    override fun getItemCount() = listItems.size

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupTitle: TextView = itemView.findViewById(R.id.group_title)

        fun bind(header: ListItem.GroupHeader) {
            groupTitle.text = header.title
        }
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskName: TextView = itemView.findViewById(R.id.task_name)
        private val taskAssignee: TextView = itemView.findViewById(R.id.task_assignee)
        private val taskPriority: TextView = itemView.findViewById(R.id.task_priority)
        private val taskStatus: TextView = itemView.findViewById(R.id.task_status)
        private val taskDueDate: TextView = itemView.findViewById(R.id.task_due_date)
        private val db = FirebaseDatabase.getInstance().reference

        fun bind(task: Item, listener: OnItemClickListener) {
            taskName.text = task.name
            taskPriority.text = task.priority
            taskStatus.text = task.status
            taskDueDate.text = task.dueDate

            itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, UpdateTaskActivity::class.java)
                intent.putExtra("item", task)
                context.startActivity(intent)
            }

            itemView.setOnLongClickListener {
                listener.onItemLongClick(task)
                true
            }

            task.assignee?.let { userId ->
                db.child("users").child(userId).get().addOnSuccessListener {
                    val user = it.getValue(User::class.java)
                    taskAssignee.text = user?.name ?: "Unassigned"
                }
            } ?: run {
                taskAssignee.text = "Unassigned"
            }
        }
    }
}