package com.example.mondaycloneapp

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.mondaycloneapp.models.Board
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.ExecutionException

class BoardWidgetItemFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var boards: List<Board> = emptyList()

    override fun onCreate() {
        // Data is fetched in onDataSetChanged
    }

    override fun onDataSetChanged() {
        fetchBoards()
    }

    override fun onDestroy() {
        boards = emptyList()
    }

    override fun getCount(): Int = boards.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.board_widget_item)
        if (position < boards.size) {
            val board = boards[position]
            views.setTextViewText(R.id.tv_board_name, board.name)
            views.setTextViewText(R.id.tv_last_changed, "Changed recently") // Placeholder

            val fillInIntent = Intent().apply {
                putExtra("board_id", board.id)
            }
            views.setOnClickFillInIntent(R.id.tv_board_name, fillInIntent)
        }
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun fetchBoards() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            boards = emptyList()
            return
        }
        try {
            val db = FirebaseDatabase.getInstance().reference
            val query = db.child("boards").orderByChild("members/$userId").equalTo(true)
            val dataSnapshot = Tasks.await(query.get())
            boards = dataSnapshot.children.mapNotNull { it.getValue(Board::class.java) }.reversed()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}