package com.example.todoapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.todoapp.MainActivity
import com.example.todoapp.R
import com.example.todoapp.TimerActivity

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val taskCount = getTaskCount(context)
            val timerStatus = getTimerStatus(context)

            // Set up the layout
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.taskCount, "Only $taskCount more!")
            views.setTextViewText(R.id.timerStatus, "Timer is $timerStatus")


            val mainActivityIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.taskCount, mainPendingIntent)


            val timerActivityIntent = Intent(context, TimerActivity::class.java)
            val timerPendingIntent = PendingIntent.getActivity(context, 0, timerActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.timerStatus, timerPendingIntent)

            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getTaskCount(context: Context): Int {
            val sharedPreferences = context.getSharedPreferences("NoteData", Context.MODE_PRIVATE)
            val taskSet = sharedPreferences.getStringSet("taskList", emptySet())
            return taskSet?.size ?: 0
        }

        private fun getTimerStatus(context: Context): String {
            val sharedPreferences = context.getSharedPreferences("TimerData", Context.MODE_PRIVATE)
            val isRunning = sharedPreferences.getBoolean("isRunning", false)
            return if (isRunning) "Running" else "Stopped"
        }


        fun updateWidget(context: Context) {
            val intent = Intent(context, TaskWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, TaskWidgetProvider::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
