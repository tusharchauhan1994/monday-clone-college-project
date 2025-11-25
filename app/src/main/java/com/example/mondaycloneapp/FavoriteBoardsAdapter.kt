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
    private val onBoardClick: (Board) -> Unit
) : RecyclerView.Adapter<FavoriteBoardsAdapter.FavoriteBoardViewHolder>() {

    private val TYPE_ADD = 0
    private val TYPE_BOARD = 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD else TYPE_BOARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteBoardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADD) {
            val view = layoutInflater.inflate(R.layout.list_item_favorite_board, parent, false)
            FavoriteBoardViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.list_item_favorite_board, parent, false)
            FavoriteBoardViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: FavoriteBoardViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ADD) {
            holder.bindAddButton(onAddBoardClick)
        } else {
            holder.bind(boards[position - 1], onBoardClick)
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
        private val addFavoriteImageView: ImageView = itemView.findViewById(R.id.iv_add_favorite)

        fun bind(board: Board, onBoardClick: (Board) -> Unit) {
            boardNameTextView.text = board.name
            addFavoriteImageView.visibility = View.GONE
            itemView.setOnClickListener {
                onBoardClick(board)
            }
        }

        fun bindAddButton(onAddBoardClick: () -> Unit) {
            boardNameTextView.visibility = View.GONE
            addFavoriteImageView.visibility = View.VISIBLE
            itemView.setOnClickListener {
                onAddBoardClick()
            }
        }
    }
}
