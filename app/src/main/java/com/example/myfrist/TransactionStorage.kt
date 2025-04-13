package com.example.myfrist

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionStorage {

    fun save(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val user = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("current_user", null) ?: return

        val json = Gson().toJson(transactions)
        prefs.edit().putString("transactions_$user", json).apply()
    }

    fun load(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val user = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("current_user", null) ?: return mutableListOf()

        val json = prefs.getString("transactions_$user", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun getUserKey(context: Context): String? {
        return context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("current_user", null)
    }
}
