package com.finetract

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val etLimit = findViewById<EditText>(R.id.et_limit)
        val btnSave = findViewById<Button>(R.id.btn_save_limit)

        // Pre-fill current limit
        val currentLimit = TransactionManager.getDailyLimit(this)
        etLimit.setText(currentLimit.toString())

        btnSave.setOnClickListener {
            val limitStr = etLimit.text.toString()
            if (limitStr.isNotEmpty()) {
                val limit = limitStr.toFloatOrNull()
                if (limit != null) {
                    TransactionManager.setDailyLimit(this, limit)
                    Toast.makeText(this, "Limit saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
