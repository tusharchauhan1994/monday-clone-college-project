package com.example.mondaycloneapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

// This dialog shows the options when the FAB is tapped in HomeActivity
class DialogFabOptions : DialogFragment() {

    // Ensures the pop-up window has a clean background
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use the XML layout for the FAB options
        return inflater.inflate(R.layout.dialog_fab_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btn_close_fab_options).setOnClickListener {
            dismiss()
        }

        // Click listener for "Add item" option
        view.findViewById<View>(R.id.option_add_item).setOnClickListener {
            dismiss() // Close the current dialog
            // Open the new Add Item dialog
            DialogAddItem().show(parentFragmentManager, "AddItemDialog")
        }

        // Click listener for "Add Board" option
        view.findViewById<View>(R.id.option_add_board).setOnClickListener {
            dismiss()
            DialogAddBoard().show(parentFragmentManager, "AddBoardDialog")
        }
    }
}