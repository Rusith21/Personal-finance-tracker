// ✅ Full MainActivity.kt with working buttons and export to CSV
package com.example.myfrist

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.util.*

data class Transaction(
    val title: String,
    val amount: Int,
    val category: String,
    val date: String,
    val type: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var btnAdd: Button
    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var btnOpenBudget: Button
    private lateinit var btnLogout: Button
    private lateinit var btnExportCsv: Button

    private var monthlyBudget = 0
    private var balance = 0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter

    private val categoryList = listOf("Food", "Transport", "Entertainment", "Bills", "Salary", "Shopping", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        tvBalance = findViewById(R.id.tvBalance)
        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerType = findViewById(R.id.spinnerType)
        btnAdd = findViewById(R.id.btnAdd)
        rvTransactions = findViewById(R.id.rvTransactions)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        btnOpenBudget = findViewById(R.id.btnOpenBudget)
        btnLogout = findViewById(R.id.btnLogout)
        btnExportCsv = findViewById(R.id.btnExportCsv)

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Income", "Expense"))
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)

        btnLogout.setOnClickListener {
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .edit().remove("current_user").apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val userKey = TransactionStorage.getUserKey(this) ?: ""
        monthlyBudget = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getInt("budget_$userKey", 0)

        transactions.clear()
        transactions.addAll(TransactionStorage.load(this))

        balance = transactions.sumOf {
            if (it.type == "Income") it.amount else -it.amount
        }

        adapter = TransactionAdapter(
            transactions,
            onEdit = { index -> showEditDialog(index, transactions[index]) },
            onDelete = { index ->
                val t = transactions[index]
                if (t.type == "Income") balance -= t.amount else balance += t.amount
                transactions.removeAt(index)
                adapter.notifyItemRemoved(index)
                TransactionStorage.save(this, transactions)
                tvBalance.text = "Balance: Rs. $balance"
                updateBudgetStatus()
            }
        )

        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter

        tvBalance.text = "Balance: Rs. $balance"
        updateBudgetStatus()

        etDate.setOnClickListener {
            showDatePickerDialog(etDate)
        }

        btnOpenBudget.setOnClickListener {
            startActivity(Intent(this, SetBudgetActivity::class.java))
        }

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString()
            val amt = etAmount.text.toString().toIntOrNull()
            val cat = spinnerCategory.selectedItem.toString()
            val date = etDate.text.toString()
            val type = spinnerType.selectedItem.toString()

            if (title.isNotBlank() && amt != null && date.isNotBlank()) {
                if (type == "Income") balance += amt else balance -= amt
                transactions.add(Transaction(title, amt, cat, date, type))
                adapter.notifyItemInserted(transactions.size - 1)
                TransactionStorage.save(this, transactions)
                tvBalance.text = "Balance: Rs. $balance"
                updateBudgetStatus()

                etTitle.text.clear()
                etAmount.text.clear()
                etDate.text.clear()
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        btnExportCsv.setOnClickListener {
            exportToCSV()
        }
    }

    private fun showEditDialog(index: Int, transaction: Transaction) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_transaction, null)
        val etDialogTitle = dialogView.findViewById<EditText>(R.id.dialogTitle)
        val etDialogAmount = dialogView.findViewById<EditText>(R.id.dialogAmount)
        val spinnerDialogCategory = dialogView.findViewById<Spinner>(R.id.dialogCategory)
        val etDialogDate = dialogView.findViewById<EditText>(R.id.dialogDate)

        spinnerDialogCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)
        spinnerDialogCategory.setSelection(categoryList.indexOf(transaction.category))

        etDialogTitle.setText(transaction.title)
        etDialogAmount.setText(transaction.amount.toString())
        etDialogDate.setText(transaction.date)

        etDialogDate.setOnClickListener {
            showDatePickerDialog(etDialogDate)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Transaction")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = etDialogTitle.text.toString()
                val newAmt = etDialogAmount.text.toString().toIntOrNull()
                val newCat = spinnerDialogCategory.selectedItem.toString()
                val newDate = etDialogDate.text.toString()

                if (newTitle.isNotBlank() && newAmt != null && newDate.isNotBlank()) {
                    if (transaction.type == "Income") balance -= transaction.amount else balance += transaction.amount
                    if (transaction.type == "Income") balance += newAmt else balance -= newAmt

                    transactions[index] = Transaction(newTitle, newAmt, newCat, newDate, transaction.type)
                    adapter.notifyItemChanged(index)
                    TransactionStorage.save(this, transactions)
                    tvBalance.text = "Balance: Rs. $balance"
                    updateBudgetStatus()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePickerDialog(targetField: EditText) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this,
            { _, year, month, day ->
                targetField.setText("%04d-%02d-%02d".format(year, month + 1, day))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun updateBudgetStatus() {
        val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val status = "Spent Rs. $totalExpense / Rs. $monthlyBudget"
        tvBudgetStatus.text = status
    }

    private fun exportToCSV() {
        val csvHeader = "Title,Amount,Category,Date,Type\n"
        val csvBody = transactions.joinToString("\n") {
            "${it.title},${it.amount},${it.category},${it.date},${it.type}"
        }

        val csvData = csvHeader + csvBody

        try {
            val fileName = "transactions_${System.currentTimeMillis()}.csv"
            val file = File(getExternalFilesDir(null), fileName)
            val fos = FileOutputStream(file)
            fos.write(csvData.toByteArray())
            fos.close()

            Toast.makeText(this, "Exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}