package com.example.todoapp
//notify imports
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.widget.TaskWidgetProvider

class MainActivity : AppCompatActivity() {

    private lateinit var taskList: ArrayList<Task>
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    //notification
    companion object {
        private const val POST_NOTIFICATIONS_PERMISSION_CODE = 100
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("NoteData", Context.MODE_PRIVATE)

        // Initialize the task list
        taskList = ArrayList()

        // RecyclerView setup
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        itemAdapter = ItemAdapter(taskList, this)
        binding.recyclerView.adapter = itemAdapter

        // Load stored tasks (if any)
        loadTasks()

        // Button click listener to add a task
        binding.btnAddTask.setOnClickListener {
            val note = binding.editTextText.text.toString()

            // Save task to SharedPreferences
            if (note.isNotEmpty()) {
                addTask(this, note)
                binding.editTextText.text.clear()
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            }

        }

        itemAdapter.onItemClick = {         // Detailed task stuff
            val intent = Intent(this, TimerActivity::class.java)
            intent.putExtra("note", it)
            startActivity(intent)
        }


        //notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), POST_NOTIFICATIONS_PERMISSION_CODE)
            }
        }

    }

    private fun addTask(context: Context, newTask: String) {
        val sharedPreferences = context.getSharedPreferences("NoteData", Context.MODE_PRIVATE)
        val taskSet = sharedPreferences.getStringSet("taskList", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        taskSet.add(newTask)
        sharedPreferences.edit().putStringSet("taskList", taskSet).apply()
        updateWidget()
        loadTasks()
        sendRemainingTasksNotification(taskSet.size) // Send notification
    }

    private fun loadTasks() {
        val taskSet = sharedPreferences.getStringSet("taskList", emptySet()) ?: emptySet()
        taskList.clear()
        taskList.addAll(taskSet.map { Task(it) })
        itemAdapter.notifyDataSetChanged()
        sendRemainingTasksNotification(taskSet.size) // Send notification
    }

    private fun updateWidget() {
        val intent = Intent(this, TaskWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, TaskWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    //notification
    private fun sendRemainingTasksNotification(taskCount: Int) {
        // Check if the permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
            return
        }

        val channelId = "tasks_channel_id"
        val notificationId = 1

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notifications"
            val descriptionText = "Channel for remaining task notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }





            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Tasks Remaining")
            .setContentText("You have $taskCount tasks remaining.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Send the notification
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification)
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            POST_NOTIFICATIONS_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                    Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}