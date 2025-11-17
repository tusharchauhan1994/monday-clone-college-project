package com.example.mondaycloneapp

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Item
import com.example.mondaycloneapp.models.User
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1

sealed class ListItem {
    data class GroupHeader(val title: String) : ListItem()
    data class TaskItem(val task: Item) : ListItem()
}

class TaskAdapter(private var listItems: List<ListItem>, private val listener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemLongClick(item: Item)
    }

    fun updateTasks(newListItems: List<ListItem>) {
        val diffCallback = TaskDiffCallback(listItems, newListItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listItems = newListItems
        diffResult.dispatchUpdatesTo(this)
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

            if (task.dueDate != null) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = inputFormat.parse(task.dueDate!!)
                    taskDueDate.text = outputFormat.format(date!!)
                } catch (e: Exception) {
                    taskDueDate.text = task.dueDate // Fallback to original string
                }
            } else {
                taskDueDate.text = ""
            }

            val statusColor = when (task.status) {
                "Working on it" -> R.color.status_working_on_it
                "Stuck" -> R.color.status_stuck
                "Done" -> R.color.status_done
                else -> R.color.status_working_on_it // Default color
            }

            val background = taskStatus.background as GradientDrawable
            background.setColor(ContextCompat.getColor(itemView.context, statusColor))

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
                    taskAssignee.text = user?.email ?: "Unassigned"
                }
            } ?: run {
                taskAssignee.text = "Unassigned"
            }
        }
    }
}

class TaskDiffCallback(
    private val oldList: List<ListItem>,
    private val newList: List<ListItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return if (oldItem is ListItem.TaskItem && newItem is ListItem.TaskItem) {
            oldItem.task.id == newItem.task.id
        } else if (oldItem is ListItem.GroupHeader && newItem is ListItem.GroupHeader) {
            oldItem.title == newItem.title
        } else {
            false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}