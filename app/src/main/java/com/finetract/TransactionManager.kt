package com.finetract

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TransactionManager {
    private const val PREF_NAME = "finetract_prefs"
    private const val KEY_DAILY_LIMIT = "daily_limit"
    private const val KEY_TODAY_SPEND = "today_spend"
    private const val KEY_LAST_RESET_DATE = "last_reset_date"
    private const val KEY_PROCESSED_TXNS = "processed_txns"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Checks date and resets if it's a new day
    fun checkAndReset(context: Context) {
        val prefs = getPrefs(context)
        val lastDate = prefs.getString(KEY_LAST_RESET_DATE, "")
        val today = getTodayDate()

        if (lastDate != today) {
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putFloat(KEY_TODAY_SPEND, 0f)
                .putStringSet(KEY_PROCESSED_TXNS, emptySet()) // New day, new transactions
                .apply()
        }
    }

    fun getDailyLimit(context: Context): Float {
        return getPrefs(context).getFloat(KEY_DAILY_LIMIT, 5000f)
    }

    fun setDailyLimit(context: Context, limit: Float) {
        getPrefs(context).edit().putFloat(KEY_DAILY_LIMIT, limit).apply()
    }

    fun getTodaySpend(context: Context): Float {
        checkAndReset(context)
        return getPrefs(context).getFloat(KEY_TODAY_SPEND, 0f)
    }

    // In-memory cache for debounce (cleared on app kill, which is fine for "spam" prevention)
    private val debounceMap = mutableMapOf<String, Long>()
    private const val DEBOUNCE_WINDOW_MS = 10_000L // 10 Seconds

    fun addTransaction(context: Context, amount: Float, uniqueId: String, timestamp: Long): Boolean {
        checkAndReset(context)
        
        // 1. Date Check
        if (!isSameDay(timestamp)) return false

        // 2. Exact Duplicate Check (Persistence)
        val prefs = getPrefs(context)
        val processed = prefs.getStringSet(KEY_PROCESSED_TXNS, mutableSetOf()) ?: mutableSetOf()
        if (processed.contains(uniqueId)) return false

        // 3. Time-Window Debounce (Heuristic)
        // Key for debounce is stricter: amount + timestamp(ish) OR just dedupe purely by amount/time flow
        // The User's "Spam" problem: Same Amount, Same Package, New Timestamp (by seconds).
        // Solution: If we see SAME AMOUNT from SAME PACKAGE within 2 mins -> Ignore.
        // We need to parse package from uniqueId or pass it safely.
        // Simplest: The uniqueId passed in IS "pkg|amount|time".
        // Let's rely on a separate key construction here or simple tokenzing.
        val parts = uniqueId.split("|")
        if (parts.size >= 2) {
            val debounceKey = "${parts[0]}|${parts[1]}" // Pkg + Amount
            val lastTime = debounceMap[debounceKey] ?: 0L
            val now = System.currentTimeMillis() // Use NOW, not notif time, to prevent processing delays from interfering? 
            // Better to use the Notification Timestamp passed in validation.
            
            if (kotlin.math.abs(timestamp - lastTime) < DEBOUNCE_WINDOW_MS) {
                // Too close to previous identical amount
                return false
            }
            debounceMap[debounceKey] = timestamp
        }

        val current = getTodaySpend(context)
        val newTotal = current + amount
        
        val newSet = HashSet(processed)
        newSet.add(uniqueId)

        prefs.edit()
            .putFloat(KEY_TODAY_SPEND, newTotal)
            .putStringSet(KEY_PROCESSED_TXNS, newSet)
            .apply()
            
        return true
    }

    fun isLimitExceeded(context: Context): Boolean {
        return getTodaySpend(context) > getDailyLimit(context)
    }
    
    private fun isSameDay(timestamp: Long): Boolean {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date) == getTodayDate()
    }
}
