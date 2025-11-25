package com.example.mondaycloneapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Board

class FavoriteBoardsAdapter(
    private var boards: List<Board>,
    private val onAddBoardClick: () -> Unit,
    private val onBoardClick: (Board) -> Unit,
    private val onStarClick: (Board) -> Unit
) : RecyclerView.Adapter<FavoriteBoardsAdapter.FavoriteBoardViewHolder>() {

    private val TYPE_ADD = 0
    private val TYPE_BOARD = 1

    override fun getItemViewType(position: Int): Int {
        return if (boards.isEmpty()) TYPE_ADD else {
            if (position < boards.size) TYPE_BOARD else TYPE_ADD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteBoardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_favorite_board, parent, false)
        return FavoriteBoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteBoardViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ADD) {
            holder.bindAddButton(onAddBoardClick)
        } else {
            holder.bind(boards[position], onBoardClick, onStarClick)
        }
    }

    override fun getItemCount(): Int {
        return boards.size + 1
    }

    fun updateBoards(newBoards: List<Board>) {
        boards = newBoards
        notifyDataSetChanged()
    }

    inner class FavoriteBoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val boardNameTextView: TextView = itemView.findViewById(R.id.tv_board_name)
        private val workspaceNameTextView: TextView = itemView.findViewById(R.id.tv_workspace_name)
        private val addFavoriteImageView: ImageView = itemView.findViewById(R.id.iv_add_favorite)
        private val starImageView: ImageView = itemView.findViewById(R.id.iv_star)
        private val boardPreviewImageView: ImageView = itemView.findViewById(R.id.iv_board_preview)
        private val bottomContainer: View = itemView.findViewById(R.id.bottom_container)

        fun bind(board: Board, onBoardClick: (Board) -> Unit, onStarClick: (Board) -> Unit) {
            boardNameTextView.text = board.name
            workspaceNameTextView.text = "Main workspace" // This is a placeholder
            addFavoriteImageView.visibility = View.GONE
            starImageView.visibility = View.VISIBLE
            boardPreviewImageView.visibility = View.VISIBLE
            bottomContainer.visibility = View.VISIBLE

            itemView.setOnClickListener {
                onBoardClick(board)
            }
            starImageView.setOnClickListener {
                onStarClick(board)
            }
        }

        fun bindAddButton(onAddBoardClick: () -> Unit) {
            addFavoriteImageView.visibility = View.VISIBLE
            starImageView.visibility = View.GONE
            boardPreviewImageView.visibility = View.GONE
            bottomContainer.visibility = View.GONE
            boardNameTextView.visibility = View.GONE
            workspaceNameTextView.visibility = View.GONE
            itemView.setOnClickListener {
                onAddBoardClick()
            }
        }
    }
}
