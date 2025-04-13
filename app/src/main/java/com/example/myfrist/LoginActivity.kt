package com.example.myfrist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnGotoRegister)

        btnLogin.setOnClickListener {
            val inputUser = etUsername.text.toString()
            val inputPass = etPassword.text.toString()

            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val usersJson = prefs.getString("users", "{}")
            val usersMap: Map<String, String> = Gson().fromJson(
                usersJson, object : TypeToken<Map<String, String>>() {}.type
            )

            if (usersMap[inputUser] == inputPass) {
                prefs.edit().putString("current_user", inputUser).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = prefs.getString("current_user", null)
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
