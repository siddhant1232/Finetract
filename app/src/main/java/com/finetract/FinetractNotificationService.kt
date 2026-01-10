package com.finetract

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern

class FinetractNotificationService : NotificationListenerService() {

    companion object {
        const val TAG = "FinetractService"
        val LISTEN_PACKAGES = setOf(
            "com.google.android.apps.nbu.paisa.user", // GPay
            "com.phonepe.app", // PhonePe
            "net.one97.paytm", // Paytm
            "in.org.npci.upiapp", // BHIM
            "com.mand.notitest" // User's Test App
        )
        
        // Match amount with currency symbol: ₹ 100, Rs. 100, INR 100
        val AMOUNT_PATTERN = Pattern.compile("(?i)(?:₹|INR|Rs\\.?)\\s*([\\d,]+(?:\\.\\d{1,2})?)")
        
        val POSITIVE_KEYWORDS = listOf("paid", "sent", "transfer", "debited", "successful", "completed")
        val NEGATIVE_KEYWORDS = listOf("failed", "declined", "pending", "request", "reversed", "refund", "credite") // Added 'credite' for 'credited' (incoming money shouldn't count as expense usually?? User requirement says "expense", so ignore incoming)
        // User asked to track expenses. "Credited" is income. "Debited" or "Paid" is expense.
        // I will add "credited" to negative keywords just in case, unless user sends money to self? - Let's stick to explicit failure keywords first + "credited" if logic implies expense only.
        // Actually, user said "Extract Amount" but typically expense tracking implies outgoing.
        // Safe to ignore "failed", "declined", "pending". 
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Service Connected")
        DebugLogManager.log("Service Linked & Ready!")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (!LISTEN_PACKAGES.contains(packageName)) {
            DebugLogManager.log("Ignored: Pkg $packageName not known")
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val fullText = "$title $text".lowercase(java.util.Locale.getDefault())

        // 1. Check for FAILURE keywords first
        if (NEGATIVE_KEYWORDS.any { fullText.contains(it) }) {
             DebugLogManager.log("Ignored ($packageName): 'Failed/Pending' keyword detected.")
             return
        }

        // 2. Check for SUCCESS/TRANSACTION keywords
        if (!POSITIVE_KEYWORDS.any { fullText.contains(it) }) {
             // Maybe log locally but don't spam debug log if it's just a "Check Balance" notification
             DebugLogManager.log("Ignored: No success keyword found in '$fullText'") 
             return
        }

        DebugLogManager.log("Rx: $packageName") // Log received valid-ish notification

        // 3. Extract Amount
        val matcher = AMOUNT_PATTERN.matcher("$title $text") // Use original case
        if (matcher.find()) {
            try {
                val amountStr = matcher.group(1)?.replace(",", "") ?: return
                val amount = amountStr.toFloat()
                
                // Robust Unique ID: Package + Amount + Timestamp (ms)
                // Use postTime from SBN to ensure it's tied to the event time, not processing time
                val uniqueId = "${packageName}|${amount}|${sbn.postTime}"

                val added = TransactionManager.addTransaction(this, amount, uniqueId, sbn.postTime)
                if (added) {
                    val msg = "Success: ₹$amount"
                    Log.d(TAG, msg)
                    DebugLogManager.log(msg)
                    checkLimitAndNotify()
                } else {
                    DebugLogManager.log("Duplicate ignored: ₹$amount")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing amount", e)
                DebugLogManager.log("Error parsing: ${e.message}")
            }
        } else {
            DebugLogManager.log("Ignored: No amount found.")
        }
    }

    private fun checkLimitAndNotify() {
        if (TransactionManager.isLimitExceeded(this)) {
            sendLimitAlert()
        }
    }

    private fun sendLimitAlert() {
        // Only alert if we haven't alerted today? 
        // User req: "Alert must trigger only once per day".
        // Use logic: Check if we already alerted today.
        // For simplicity, I'll rely on the user noticing the sticky notification or just standard approach.
        // To strictly follow "once per day", I need another pref key.
        val prefs = getSharedPreferences("finetract_internal", Context.MODE_PRIVATE)
        val lastAlert = prefs.getString("last_alert_date", "")
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        
        if (lastAlert == today) return // Already alerted

        val channelId = "limit_alert"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Budget Alerts", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Spending Limit Exceeded")
            .setContentText("You have crossed your daily spending limit of ₹${TransactionManager.getDailyLimit(this)}")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(999, notification)

        prefs.edit().putString("last_alert_date", today).apply()
    }
}
