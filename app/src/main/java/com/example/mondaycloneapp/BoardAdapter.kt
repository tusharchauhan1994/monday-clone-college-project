package com.example.mondaycloneapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Board
import java.util.concurrent.TimeUnit

class BoardAdapter(
    private val context: Context,
    private val boards: List<Board>,
    private val onBoardClick: (Board) -> Unit,
    private val onBoardLongClick: (Board) -> Unit
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val boardName: TextView = itemView.findViewById(R.id.tv_board_name)
        val lastChanged: TextView = itemView.findViewById(R.id.tv_last_changed)
        val starIcon: ImageView = itemView.findViewById(R.id.iv_star)
        val boardIcon: ImageView = itemView.findViewById(R.id.iv_board_icon)

        fun bind(board: Board) {
            boardName.text = board.name
            lastChanged.text = getTimeAgo(board.createdAt)

            itemView.setOnClickListener {
                onBoardClick(board)
            }

            itemView.setOnLongClickListener {
                onBoardLongClick(board)
                true
            }

            starIcon.setOnClickListener {
                // Handle star/favorite toggle
            }
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Changed just now"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "Changed ${minutes}min ago"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "Changed ${hours}h ago"
                }
                diff < TimeUnit.DAYS.toMillis(30) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "Changed ${days}d ago"
                }
                else -> {
                    val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                    "Changed ${months}mo ago"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_board, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(boards[position])
    }

    override fun getItemCount() = boards.size
}