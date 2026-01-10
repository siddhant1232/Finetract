package com.finetract

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.math.roundToInt

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val context = requireContext()
        // Ensure data is fresh
        TransactionManager.checkAndReset(context)

        val spend = TransactionManager.getTodaySpend(context)
        val limit = TransactionManager.getDailyLimit(context)
        val remaining = limit - spend
        
        // --- 1. Smart Header ---
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good Morning! ☀️"
            in 12..16 -> "Good Afternoon! 🎓"
            in 17..22 -> "Good Evening! 🌙"
            else -> "Late Night Grind? 🦉"
        }
        view?.findViewById<TextView>(R.id.tv_greeting)?.text = greeting
        
        val dateStr = java.text.SimpleDateFormat("MMM d, EEEE", java.util.Locale.getDefault()).format(java.util.Date())
        view?.findViewById<TextView>(R.id.tv_date)?.text = dateStr

        // --- 2. Main Progress ---
        val progress = if (limit > 0) ((spend / limit) * 100).toInt() else 0
        val ratio = if (limit > 0) spend / limit else 0f
        
        val colorRes = when {
            spend > limit -> R.color.danger_red
            progress >= 90 -> R.color.warning_yellow
            else -> R.color.success_green
        }
        val statusColor = ContextCompat.getColor(context, colorRes)

        view?.findViewById<TextView>(R.id.tv_spend_amount)?.text = "₹${spend.toInt()}"
        view?.findViewById<TextView>(R.id.tv_limit_info)?.text = "of ₹${limit.toInt()}"
        
        val progressBar = view?.findViewById<com.google.android.material.progressindicator.CircularProgressIndicator>(R.id.progress_limit)
        val animator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", progressBar?.progress ?: 0, progress.coerceIn(0, 100))
        animator.duration = 800
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.start()
        progressBar?.setIndicatorColor(statusColor)

        // --- 3. Daily Insight ---
        val tvInsight = view?.findViewById<TextView>(R.id.tv_daily_insight)
        tvInsight?.text = when {
            ratio <= 0.6f -> "You’re well within your limit."
            ratio <= 0.9f -> "Careful, you're getting close."
            else -> "You’ve crossed your limit."
        }
        tvInsight?.setTextColor(statusColor)

        // --- 4. Dashboard Grid Stats ---
        val stats = TransactionManager.getTodayStats(context)
        
        // Count
        view?.findViewById<TextView>(R.id.tv_stat_count)?.text = "${stats.count}"
        
        // Highest
        val maxText = if (stats.maxAmount > 0) "₹${stats.maxAmount.toInt()}" else "-"
        view?.findViewById<TextView>(R.id.tv_stat_max)?.text = maxText
        
        // Left
        val tvLeft = view?.findViewById<TextView>(R.id.tv_stat_left)
        val tvLeftLabel = view?.findViewById<TextView>(R.id.tv_stat_left_label)
        
        if (remaining < 0) {
            tvLeft?.text = "-₹${kotlin.math.abs(remaining.toInt())}"
            tvLeft?.setTextColor(ContextCompat.getColor(context, R.color.danger_red))
            tvLeftLabel?.text = "Over"
            tvLeftLabel?.setTextColor(ContextCompat.getColor(context, R.color.danger_red))
        } else {
            tvLeft?.text = "₹${remaining.toInt()}"
            tvLeft?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            tvLeftLabel?.text = "Left"
            tvLeftLabel?.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }

        // --- 5. Behavioral Nudge (Styled Pill) ---
        val tvNudge = view?.findViewById<TextView>(R.id.tv_nudge)
        
        val nudgeText = when {
            XFactorManager.getWeekendWarning(context) != null -> XFactorManager.getWeekendWarning(context)
            ratio > 0.8f && ratio <= 1.0f -> "⚠️ Slow down for today"
            else -> "✨ Great control so far!"
        }
        tvNudge?.text = nudgeText
        
        // Dynamic Pill Color
        val pillColor = when {
            ratio > 0.9f -> "#FFEBEE" // Red tint
            progress >= 90 -> "#FFFDE7" // Yellow tint
            else -> "#F1F8E9" // Green tint
        }
        val pillText = when {
            ratio > 0.9f -> R.color.danger_red
            progress >= 90 -> R.color.warning_yellow // Actually text might need darker yellow/orange
            else -> R.color.success_green
        }
        
        tvNudge?.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.parseColor(pillColor))
        tvNudge?.setTextColor(ContextCompat.getColor(context, if (progress >= 90 && ratio <= 1.0f) R.color.text_primary else pillText)) // Fallback for yellow readability

        // --- Click Action ---
        view?.findViewById<View>(R.id.container_progress)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InsightsFragment())
                .addToBackStack(null)
                .commit()
        }
        
        // --- Entry Animation (Subtle Stagger) ---
        val grid = view?.findViewById<View>(R.id.container_stats)
        val nudge = view?.findViewById<View>(R.id.tv_nudge)
        
        grid?.alpha = 0f
        grid?.translationY = 20f
        grid?.animate()?.alpha(1f)?.translationY(0f)?.setDuration(500)?.setStartDelay(100)?.start()
        
        nudge?.alpha = 0f
        nudge?.translationY = 20f
        nudge?.animate()?.alpha(1f)?.translationY(0f)?.setDuration(500)?.setStartDelay(200)?.start()
    }
}
