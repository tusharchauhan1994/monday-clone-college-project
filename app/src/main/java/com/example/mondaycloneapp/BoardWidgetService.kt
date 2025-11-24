package com.example.mondaycloneapp

import android.content.Intent
import android.widget.RemoteViewsService

class BoardWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return BoardWidgetItemFactory(applicationContext, intent)
    }
}