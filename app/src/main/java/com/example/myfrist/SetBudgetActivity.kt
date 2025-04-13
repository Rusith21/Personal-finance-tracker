package com.example.myfrist

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetBudgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        val etBudget = findViewById<EditText>(R.id.etBudget)
        val btnSaveBudget = findViewById<Button>(R.id.btnSaveBudget)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = userPrefs.getString("current_user", null)

        if (username != null) {
            val currentBudget = prefs.getInt("budget_$username", 0)
            etBudget.setText(currentBudget.toString())

            btnSaveBudget.setOnClickListener {
                val newBudget = etBudget.text.toString().toIntOrNull()
                if (newBudget != null) {
                    prefs.edit().putInt("budget_$username", newBudget).apply()
                    Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
