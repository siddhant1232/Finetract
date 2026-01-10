package com.finetract

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.Locale

object XFactorManager {
    private const val PREF_NAME = "finetract_xfactor"
    
    // Storage Keys
    private const val KEY_STREAK = "streak_days"
    private const val KEY_WEEKLY_SAVED = "weekly_saved"
    private const val KEY_WEEKDAY_SUM = "weekday_sum"
    private const val KEY_WEEKDAY_COUNT = "weekday_count"
    private const val KEY_WEEKEND_SUM = "weekend_sum" 
    private const val KEY_WEEKEND_COUNT = "weekend_count"
    private const val KEY_TXN_SUM = "txn_total_sum"
    private const val KEY_TXN_COUNT = "txn_total_count"
    private const val KEY_LAST_BIG_SPEND_WARNING = "last_big_alert_date"
    private const val KEY_LAST_REFLECTION_MONTH = "last_reflection_month"
    private const val KEY_VALID_DAYS_FOR_LIMIT = "valid_days_limit_calc"
    private const val KEY_TOTAL_SPEND_FOR_LIMIT = "total_spend_limit_calc"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // --- 1. Daily Spending Status (Mood) ---
    fun getMoodMessage(context: Context): String {
        // Priority: Check Big Spend / Weekend / Reflection first? 
        // Spec says: ALERT > CAUTION > GOOD.
        // But also check for Big Spend (Soft Warning) or Monthly Reflection.
        
        // Let's first calculate the Mood based on Ratio
        val spend = TransactionManager.getTodaySpend(context)
        val limit = TransactionManager.getDailyLimit(context)
        if (limit == 0f) return "Set a daily limit to see insights."
        
        val ratio = spend / limit
        
        return when {
            ratio > 0.9f -> {
                listOf("Today went out of control.", "You crossed your comfort zone today.").random()
            }
            ratio > 0.6f -> {
                listOf("Spending is slightly high today.", "Be mindful for the rest of the day.").random()
            }
            else -> {
                // If it's early in the day (e.g. 0 spend), maybe show streak or savings?
                // If spend is very low, show Good.
                listOf("You were disciplined today.", "Nice control today.").random()
            }
        }
    }

    // --- 2. Weekend Awareness ---
    fun updateWeekendStats(context: Context, amount: Float) {
        val prefs = getPrefs(context)
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val isWeekend = day == Calendar.SATURDAY || day == Calendar.SUNDAY || day == Calendar.FRIDAY // Spec says Fri/Sat/Sun
        
        if (isWeekend) {
            val sum = prefs.getFloat(KEY_WEEKEND_SUM, 0f) + amount
            val count = prefs.getInt(KEY_WEEKEND_COUNT, 0) + 1
            prefs.edit().putFloat(KEY_WEEKEND_SUM, sum).putInt(KEY_WEEKEND_COUNT, count).apply()
        } else {
            val sum = prefs.getFloat(KEY_WEEKDAY_SUM, 0f) + amount
            val count = prefs.getInt(KEY_WEEKDAY_COUNT, 0) + 1
            prefs.edit().putFloat(KEY_WEEKDAY_SUM, sum).putInt(KEY_WEEKDAY_COUNT, count).apply()
        }
    }

    fun getWeekendWarning(context: Context): String? {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        // Show only on Friday morning or first weekend txn (We can simpler check: is it Fri/Sat/Sun?)
        if (day != Calendar.FRIDAY && day != Calendar.SATURDAY && day != Calendar.SUNDAY) return null
        
        val prefs = getPrefs(context)
        val wDayCount = prefs.getInt(KEY_WEEKDAY_COUNT, 0)
        val wEndCount = prefs.getInt(KEY_WEEKEND_COUNT, 0)
        if (wDayCount < 5 || wEndCount < 2) return null // Need some data
        
        val wDayAvg = prefs.getFloat(KEY_WEEKDAY_SUM, 0f) / wDayCount
        val wEndAvg = prefs.getFloat(KEY_WEEKEND_SUM, 0f) / wEndCount
        
        return if (wEndAvg >= wDayAvg * 1.3f) {
             listOf("You usually spend more on weekends.", "Weekends tend to increase your spending.").random()
        } else null
    }

    // --- 3 & 4. Savings & Streaks (Called Daily on Reset) ---
    fun onDailyReset(context: Context, yesterdaySpend: Float, yesterdayLimit: Float) {
        val prefs = getPrefs(context)
        
        // Micro-Streaks & Savings
        if (yesterdaySpend <= yesterdayLimit) {
            // Streak
            val currentStreak = prefs.getInt(KEY_STREAK, 0) + 1
            prefs.edit().putInt(KEY_STREAK, currentStreak).apply()
            
            // Invisible Savings (Only if 10% under limit)
            if (yesterdaySpend <= (yesterdayLimit * 0.9f)) {
                val saved = yesterdayLimit - yesterdaySpend
                val totalSaved = prefs.getFloat(KEY_WEEKLY_SAVED, 0f) + saved
                prefs.edit().putFloat(KEY_WEEKLY_SAVED, totalSaved).apply()
            }
        } else {
            // Reset Streak
            prefs.edit().putInt(KEY_STREAK, 0).apply()
        }

        // Smart Limit Data Accumulation
        val validDays = prefs.getInt(KEY_VALID_DAYS_FOR_LIMIT, 0) + 1
        val totalSpend = prefs.getFloat(KEY_TOTAL_SPEND_FOR_LIMIT, 0f) + yesterdaySpend
        prefs.edit().putInt(KEY_VALID_DAYS_FOR_LIMIT, validDays)
            .putFloat(KEY_TOTAL_SPEND_FOR_LIMIT, totalSpend).apply()
    }

    fun getStreakMessage(context: Context): String? {
        val streak = getPrefs(context).getInt(KEY_STREAK, 0)
        if (streak in 3..7) {
             return "$streak disciplined days in a row."
        }
        return null
    }
    
    fun getSavingsMessage(context: Context): String? {
        val saved = getPrefs(context).getFloat(KEY_WEEKLY_SAVED, 0f)
        if (saved > 100) { // arbitrary threshold to show message
            return "You avoided overspending ₹${saved.toInt()} this week."
        }
        return null
    }

    // --- 5. Smart Limit Suggestion ---
    fun getSmartLimitSuggestion(context: Context): String? {
        val prefs = getPrefs(context)
        val validDays = prefs.getInt(KEY_VALID_DAYS_FOR_LIMIT, 0)
        if (validDays < 14) return null
        
        val total = prefs.getFloat(KEY_TOTAL_SPEND_FOR_LIMIT, 0f)
        val avg = total / validDays
        val currentLimit = TransactionManager.getDailyLimit(context)
        val suggested = (avg * 1.05f).toInt() // +5% buffer
        
        // Show if diff >= 10%
        val diff = kotlin.math.abs(suggested - currentLimit)
        if (diff >= (currentLimit * 0.1f)) {
            return "Based on habits, ₹$suggested/day may suit you better."
        }
        return null
    }

    // --- 6. Big Spend Awareness ---
    fun checkBigSpend(context: Context, amount: Float): String? {
        val prefs = getPrefs(context)
        val count = prefs.getInt(KEY_TXN_COUNT, 0)
        val sum = prefs.getFloat(KEY_TXN_SUM, 0f)
        
        // Update stats
        prefs.edit()
            .putInt(KEY_TXN_COUNT, count + 1)
            .putFloat(KEY_TXN_SUM, sum + amount)
            .apply()
            
        if (count < 5) return null // Warmup
        
        val avg = sum / count
        val threshold = TransactionManager.getLargePaymentThreshold(context)
        
        // Rules: > 2.5x avg AND (threshold not set OR < threshold)
        // If threshold IS set, and amount > threshold, it's already excluded/handled by TransactionManager logic?
        // User spec: "AND transactionAmount < largeThreshold" -> triggering only for "kinda big" but "not huge excluded rent"
        
        if (amount >= avg * 2.5f) {
             if (threshold > 0 && amount >= threshold) return null // Excluded by threshold rule
             
             // Check max ONCE per day
             val lastAlert = prefs.getString(KEY_LAST_BIG_SPEND_WARNING, "")
             val today = TransactionManager.getTodayDate() // Need to expose or duplicate
             if (lastAlert == today) return null
             
             prefs.edit().putString(KEY_LAST_BIG_SPEND_WARNING, today).apply()
             return "This spend was higher than your usual transactions."
        }
        return null
    }

    // --- 7. Monthly Reflection ---
    fun checkMonthlyReflection(context: Context): String? {
        val prefs = getPrefs(context)
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) // 0-11
        val lastMonthReflected = prefs.getInt(KEY_LAST_REFLECTION_MONTH, -1)
        
        if (lastMonthReflected != -1 && lastMonthReflected != currentMonth) {
            // New month started!
            prefs.edit().putInt(KEY_LAST_REFLECTION_MONTH, currentMonth).apply()
            // We could do complex retrieval of last month's data here, 
            // but simpler: just simple generic message or lightweight calc if we tracked it.
            // For now, let's keep it simple as per "Lightweight" spec
            return "A new month! Identify 1 habit to improve this month."
        }
        
        if (lastMonthReflected == -1) {
             prefs.edit().putInt(KEY_LAST_REFLECTION_MONTH, currentMonth).apply()
        }
        return null
    }
    
    // --- Master Message Selector ---
    fun getPrioritizedMessage(context: Context): MessageInfo {
        // Defines the single message to show on Home
        // Priority: ALERT (Mood) > Big Spend Warning > Weekend Warning > Limit Suggestion > Streak > Savings > GOOD (Mood)
        
        val spend = TransactionManager.getTodaySpend(context)
        val limit = TransactionManager.getDailyLimit(context)
        val ratio = if (limit > 0) spend / limit else 0f
        
        // 1. ALERT (Mood) - Critical
        if (ratio > 0.9f) {
             return MessageInfo(getMoodMessage(context), R.color.danger_red, true)
        }
        
        // 2. Big Spend Warning (We need to store if a warning is ACTIVE for display? 
        // Real-time triggering returns the string, but here we need state.
        // Let's assume the Big Spend Warning is transient (toast/snackbar) OR transient home state.
        // For simplicity, let's stick to Mood/Habit persistent messages here.
        
        // 3. Weekend Warning
        val weekendMsg = getWeekendWarning(context)
        if (weekendMsg != null) {
            return MessageInfo(weekendMsg, R.color.warning_yellow, false)
        }
        
        // 4. Smart Limit
        val limitMsg = getSmartLimitSuggestion(context)
        if (limitMsg != null) {
            return MessageInfo(limitMsg, R.color.primary, false)
        }
        
        // 5. Streaks (Positive)
        val streakMsg = getStreakMessage(context)
        if (streakMsg != null) {
            return MessageInfo(streakMsg, R.color.success_green, false)
        }
        
        // 6. CAUTION (Mood)
        if (ratio > 0.6f) {
             return MessageInfo(getMoodMessage(context), R.color.warning_yellow, false)
        }
        
        // 7. Savings (Positive)
        val savingsMsg = getSavingsMessage(context)
        if (savingsMsg != null) {
            return MessageInfo(savingsMsg, R.color.success_green, false)
        }

        // 8. GOOD (Mood) - Default
        return MessageInfo(getMoodMessage(context), R.color.success_green, false)
    }

    data class MessageInfo(val text: String, val colorRes: Int, val isAlert: Boolean)
}
