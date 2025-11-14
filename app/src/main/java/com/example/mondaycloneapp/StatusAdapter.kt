package com.example.mondaycloneapp

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class StatusAdapter(context: Context, statuses: Array<String>) : ArrayAdapter<String>(context, 0, statuses) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_status, parent, false)
        val status = getItem(position)

        val statusColorView = view.findViewById<View>(R.id.status_color)
        val statusTextView = view.findViewById<TextView>(R.id.status_text)

        statusTextView.text = status

        val statusColor = when (status) {
            "Working on it" -> R.color.status_working_on_it
            "Stuck" -> R.color.status_stuck
            "Done" -> R.color.status_done
            else -> android.R.color.transparent
        }

        val background = statusColorView.background as? GradientDrawable ?: GradientDrawable()
        background.shape = GradientDrawable.OVAL
        background.setColor(ContextCompat.getColor(context, statusColor))
        statusColorView.background = background

        return view
    }
}