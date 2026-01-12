package com.finetract.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.finetract.data.local.entities.Transaction
import com.finetract.data.repository.FinanceRepository
import com.finetract.util.SmsParser
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmsReceiverEntryPoint {
        fun repository(): FinanceRepository
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val body = sms.displayMessageBody
            val sender = sms.displayOriginatingAddress ?: ""
            
            Log.d("SmsReceiver", "Received SMS from $sender: $body")

            // Optional: Filter by bank short-codes (e.g., starts with "AD-" or ends with "-SBI")
            // if (!sender.contains("SBI", ignoreCase = true)) continue

            val parsed = SmsParser.parse(body)
            if (parsed != null && !parsed.isPending) {
                saveTransaction(context, parsed.amount, parsed.type, "${parsed.vendor} (SMS)")
            }
        }
    }

    private fun saveTransaction(context: Context, amount: Double, type: com.finetract.data.local.entities.TransactionType, note: String) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SmsReceiverEntryPoint::class.java
        )
        val repository = entryPoint.repository()

        scope.launch {
            try {
                // For SMS transactions, we'll use a default "Uncategorized" category if it exists, 
                // or just the first available category for now. 
                // In a real app, we'd map vendor to category or ask the user.
                val categories = repository.getAllCategories().firstOrNull()
                val categoryId = categories?.firstOrNull()?.id ?: 1L // Fallback to ID 1

                repository.insertTransaction(
                    Transaction(
                        amount = amount,
                        timestamp = System.currentTimeMillis(),
                        categoryId = categoryId,
                        note = note,
                        type = type
                    )
                )
                Log.d("SmsReceiver", "Transaction saved: $amount from SMS")
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error saving SMS transaction", e)
            }
        }
    }
}
