package com.example.todoapp

import android.util.Log
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.widget.Toast

class ItemAdapter(private val taskList: ArrayList<Task>, private val context: Context) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    var onItemClick: ((Task) -> Unit)? = null   // detailed view part

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTextView: TextView = itemView.findViewById(R.id.notes)
        val editButton: ImageButton = itemView.findViewById(R.id.imageButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.imageButton2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val task = taskList[position]


        holder.taskTextView.text = task.text

        // Set up the click listener
        holder.editButton.setOnClickListener {
            showEditDialog(position)
        }

        // Set up the click listener for the delete button
        holder.deleteButton.setOnClickListener {
            taskList.removeAt(position) // Remove task from list
            notifyItemRemoved(position) // Notify adapter
            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
            triggerVibration() // Trigger vibration
        }

        holder.itemView.setOnClickListener { // detailed view part
            onItemClick?.invoke(task)
        }
    }


    private fun showEditDialog(position: Int) {
        val task = taskList[position]

        // Create an AlertDialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Task")

        // Create an EditText
        val input = EditText(context)
        input.setText(task.text)
        builder.setView(input)

        // dialog buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val newTaskText = input.text.toString()

            // Update the task in the list
            if (newTaskText.isNotEmpty()) {
                taskList[position] = Task(newTaskText)
                notifyItemChanged(position) // Notify adapter of the change
                Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show()
                triggerVibration() // Trigger vibration (DELETE if crashes! )
            } else {
                Toast.makeText(context, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    // Trigger vibration
    private fun triggerVibration() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
            Log.d("ItemAdapter", "**************Vibration detected**************")
        } else {
            Log.d("ItemAdapter", "**************No vibrator found**************")
        }
        }
    }

