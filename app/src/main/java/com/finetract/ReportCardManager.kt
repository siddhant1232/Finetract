package com.finetract

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ReportCardManager {

    data class ReportCard(
        val grade: String,         // A, B, C, D
        val disciplineDays: Int,   // Days under limit
        val totalDays: Int,        // Days tracked
        val subjectGrades: Map<String, String>, // Category -> Status ("Good"/"Needs Attention")
        val remark: String,
        val tip: String
    )

    fun generateReport(context: Context): ReportCard? {
        val history = TransactionManager.getHistory(context)
        val allTransactions = TransactionManager.getTransactions(context)

        // 1. Determine Target Month (Previous Month)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1) // Go back 1 month
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)

        // 2. Filter History for Target Month
        val monthlyRecords = history.filter {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            val cal = Calendar.getInstance()
            if (date != null) cal.time = date
            cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
        }

        if (monthlyRecords.isEmpty()) return null // No data for previous month

        // 3. Calculate Grade (Discipline)
        val totalDays = monthlyRecords.size
        val disciplinedDays = monthlyRecords.count { it.spend <= it.limit }
        val ratio = if (totalDays > 0) disciplinedDays.toFloat() / totalDays else 0f

        val grade = when {
            ratio >= 0.8f -> "A"
            ratio >= 0.6f -> "B"
            ratio >= 0.4f -> "C"
            else -> "D"
        }

        // 4. Subject-Wise Performance (Transactions)
        val monthlyTxns = allTransactions.filter {
            val cal = Calendar.getInstance()
            cal.time = Date(it.timestamp)
            cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
        }
        
        val categorySpends = monthlyTxns.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }
            
        // Simple Threshold: If category spend > 20% of Total Monthly Limit (approx), mark as "Needs Attention"
        // Or simpler: Compare to Average?
        // Let's use internal heuristics for student categories.
        val subjectGrades = mutableMapOf<String, String>()
        val totalMonthlySpend = monthlyRecords.sumOf { it.spend.toDouble() }.toFloat()
        
        // Supported Subjects
        val subjects = listOf("Food", "Travel", "Entertainment", "Shopping", "Education", "Other")
        
        for (subject in subjects) {
            val spend = categorySpends[subject] ?: 0f
            if (spend == 0f) {
                subjectGrades[subject] = "-"
                continue
            }
            
            // Logic: "Needs Attention" if > 30% of total spend (Concentration Risk)
            val concentration = if (totalMonthlySpend > 0) spend / totalMonthlySpend else 0f
            
            val status = when {
                subject == "Food" && concentration > 0.4f -> "Needs Attention"
                subject == "Entertainment" && concentration > 0.2f -> "Needs Attention"
                subject == "Shopping" && concentration > 0.3f -> "Needs Attention"
                else -> "Good"
            }
            subjectGrades[subject] = status
        }

        // 5. Teacher's Remark
        val remark = when {
            grade == "A" -> "Excellent discipline! You are mastering your finances."
            grade == "B" -> "Good work, but watch out for those few slip-ups."
            grade == "C" -> "Spending was irregular. Try creating a daily routine."
            else -> "Action needed. Small daily savings will help you recover."
        }

        // 6. Improvement Tip
        val tip = when {
            subjectGrades["Food"] == "Needs Attention" -> "Food is your biggest expense. Try a weekly cap."
            subjectGrades["Entertainment"] == "Needs Attention" -> "Entertainment costs add up fast. Limit movie nights."
            subjectGrades["Shopping"] == "Needs Attention" -> "Impulse buying detected. Wait 24h before buying."
            grade == "A" -> "You're doing great. Consider saving the surplus!"
            else -> "Try dealing specifically in cash for one week."
        }

        return ReportCard(grade, disciplinedDays, totalDays, subjectGrades, remark, tip)
    }

    private const val PREF_LAST_REPORT_MONTH = "last_report_month"

    fun isNewReportAvailable(context: Context): Boolean {
        // Logic: specific to "First open of new month"
        val prefs = context.getSharedPreferences("finetract_prefs", Context.MODE_PRIVATE)
        val lastReportMonth = prefs.getString(PREF_LAST_REPORT_MONTH, "")
        
        val currentMonthForReport = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        // If stored "Last Report" is older than "Current Month", AND we have data for previous month...
        // Wait, if today is Feb 1st, we want to show Jan report.
        // We should mark "Feb" (or unique report ID) as "shown".
        // Let's simple check: Have we shown the report for [Current Month's Reporting Cycle] yes/no?
        
        return lastReportMonth != currentMonthForReport
    }

    fun markReportShown(context: Context) {
        val prefs = context.getSharedPreferences("finetract_prefs", Context.MODE_PRIVATE)
        val currentMonthForReport = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        prefs.edit().putString(PREF_LAST_REPORT_MONTH, currentMonthForReport).apply()
    }
}
