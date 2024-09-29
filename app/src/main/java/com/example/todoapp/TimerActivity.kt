package com.example.todoapp

import com.example.todoapp.widget.TaskWidgetProvider

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.databinding.ActivityTimerBinding

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private lateinit var sharedPreferences: SharedPreferences

    private var isRunning = false
    private var timerSeconds = 0

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            timerSeconds++
            saveTimerState() // Save state every second
            updateTimerText()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("TimerData", Context.MODE_PRIVATE)

        // Restore state
        timerSeconds = sharedPreferences.getInt("timerSeconds", 0)
        isRunning = sharedPreferences.getBoolean("isRunning", false)
        if (isRunning) {
            startTimer()
        } else {
            updateTimerText()
        }

        binding.start.setOnClickListener { startTimer() }
        binding.stop.setOnClickListener { stopTimer() }
        binding.reset.setOnClickListener { resetTimer() }

        // Detailed view stuff (DO NOT DELETE!)
        val task = intent.getParcelableExtra<Task>("note")
        if (task != null) {
            val textView: TextView = findViewById(R.id.detailedNote)
            textView.text = task.text
        }
    }

    private fun startTimer() {
        if (!isRunning) {
            handler.postDelayed(runnable, 1000)
            isRunning = true
            binding.start.isEnabled = false
            binding.stop.isEnabled = true
            binding.reset.isEnabled = true
            updateWidget()
            saveTimerState()
        }
    }

    private fun stopTimer() {
        if (isRunning) {
            handler.removeCallbacks(runnable)
            isRunning = false
            binding.start.isEnabled = true
            binding.start.text = "Resume"
            binding.stop.isEnabled = false
            binding.reset.isEnabled = true
            updateWidget()
            saveTimerState()
        }
    }

    private fun resetTimer() {
        stopTimer()
        timerSeconds = 0
        updateTimerText()
        binding.start.isEnabled = true
        binding.reset.isEnabled = false
        binding.start.text = "Start"
        updateWidget()
        saveTimerState()
    }

    private fun updateTimerText() {
        val hours = timerSeconds / 3600
        val minutes = (timerSeconds % 3600) / 60
        val seconds = timerSeconds % 60
        val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        binding.timerText.text = time
    }

    private fun saveTimerState() {
        val sharedEdit = sharedPreferences.edit()
        sharedEdit.putInt("timerSeconds", timerSeconds)
        sharedEdit.putBoolean("isRunning", isRunning)
        sharedEdit.apply()
    }

    override fun onPause() {
        super.onPause()
        saveTimerState()
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
}