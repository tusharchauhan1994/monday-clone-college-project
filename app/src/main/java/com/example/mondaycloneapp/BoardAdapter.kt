package com.example.mondaycloneapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Board

class BoardAdapter(private val boards: List<Board>, private val listener: (Board) -> Unit) :
    RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val board = boards[position]
        holder.bind(board)
        holder.itemView.setOnClickListener { listener(board) }
    }

    override fun getItemCount() = boards.size

    class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val boardName: TextView = itemView.findViewById(R.id.board_name)

        fun bind(board: Board) {
            boardName.text = board.name
        }
    }
}