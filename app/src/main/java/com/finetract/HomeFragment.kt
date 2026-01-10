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
        
        // Report Card Trigger
        if (ReportCardManager.isNewReportAvailable(requireContext())) {
            // Only show if we actually have data to generate a report
            if (ReportCardManager.generateReport(requireContext()) != null) {
                ReportCardDialogFragment().show(parentFragmentManager, "ReportCard")
            }
        }
    }

    private fun updateDashboard() {
        val context = requireContext()
        // Ensure data is fresh
        TransactionManager.checkAndReset(context)

        val spend = TransactionManager.getTodaySpend(context)
        val limit = TransactionManager.getDailyLimit(context)
        val remaining = limit - spend
        
        // --- 1. Smart Header (Friend Mode) ---
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Hey! Ready to go? ⚡"
            in 12..16 -> "Afternoon grind! 🎓"
            in 17..22 -> "Evening chill? 🌙"
            else -> "Burning the oil? 🦉"
        }
        view?.findViewById<TextView>(R.id.tv_greeting)?.text = greeting
        
        val dateStr = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date())
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

        view?.findViewById<TextView>(R.id.tv_spend_amount)?.text = formatMoney(context, spend)
        view?.findViewById<TextView>(R.id.tv_limit_info)?.text = "/ " + formatMoney(context, limit)
        
        val progressBar = view?.findViewById<com.google.android.material.progressindicator.CircularProgressIndicator>(R.id.progress_limit)
        val animator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", progressBar?.progress ?: 0, progress.coerceIn(0, 100))
        animator.duration = 800
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.start()
        progressBar?.setIndicatorColor(statusColor)

        // --- 3. Daily Insight (Smart Context Engine) ---
        val smartInsight = XFactorManager.getSmartHomeInsight(context)
        val tvInsight = view?.findViewById<TextView>(R.id.tv_daily_insight)
        // If privacy mode, maybe we hide the specific numeric insight if it contains numbers?
        // Smart messages like "You still have 500 left" contain numbers.
        // Simple regex replace for digits if privacy mode?
        var insightText = smartInsight.text
        if (TransactionManager.isPrivacyMode(context)) {
             insightText = insightText.replace(Regex("₹?\\d+"), "••••")
        }
        tvInsight?.text = insightText
        tvInsight?.setTextColor(ContextCompat.getColor(context, smartInsight.colorRes))

        // --- 3.5 Ghost Hunter (Subscription Radar) ---
        val ghosts = GhostHunterManager.scanForGhosts(context)
        val cardGhost = view?.findViewById<View>(R.id.card_ghost_alert)
        if (ghosts.isNotEmpty()) {
            cardGhost?.visibility = View.VISIBLE
            val ghost = ghosts.first()
            val ghostText = "Ghost Detected: ${ghost.merchant} (₹${ghost.amount.toInt()})"
            view?.findViewById<TextView>(R.id.tv_ghost_text)?.text = if (TransactionManager.isPrivacyMode(context)) "Ghost Detected: ${ghost.merchant} (••••)" else ghostText
        } else {
            cardGhost?.visibility = View.GONE
        }

        // --- 4. Dashboard Grid Stats ---
        val stats = TransactionManager.getTodayStats(context)
        
        // Count (Safe to show?) -> Yes, usually count isn't sensitive.
        view?.findViewById<TextView>(R.id.tv_stat_count)?.text = "${stats.count}"
        
        // Highest
        val maxText = if (stats.maxAmount > 0) formatMoney(context, stats.maxAmount) else "-"
        view?.findViewById<TextView>(R.id.tv_stat_max)?.text = maxText
        
        // Left
        val tvLeft = view?.findViewById<TextView>(R.id.tv_stat_left)
        val tvLeftLabel = view?.findViewById<TextView>(R.id.tv_stat_left_label)
        
        if (remaining < 0) {
            tvLeft?.text = "-" + formatMoney(context, kotlin.math.abs(remaining))
            tvLeft?.setTextColor(ContextCompat.getColor(context, R.color.danger_red))
            tvLeftLabel?.text = "Over"
            tvLeftLabel?.setTextColor(ContextCompat.getColor(context, R.color.danger_red))
        } else {
            tvLeft?.text = formatMoney(context, remaining)
            tvLeft?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            tvLeftLabel?.text = "Left"
            tvLeftLabel?.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }

        // --- 5. Behavioral Nudge (Styled Pill) ---
        // Nudge usually doesn't have numbers. "Great control" etc.
        val tvNudge = view?.findViewById<TextView>(R.id.tv_nudge)
        
        val nudgeText = when {
            XFactorManager.getWeekendWarning(context) != null -> XFactorManager.getWeekendWarning(context)
            ratio > 0.8f && ratio <= 1.0f -> "⚠️ Maybe slow down?"
            else -> "✨ You're crushing it!"
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
            progress >= 90 -> R.color.warning_yellow 
            else -> R.color.success_green
        }
        
        tvNudge?.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.parseColor(pillColor))
        tvNudge?.setTextColor(ContextCompat.getColor(context, if (progress >= 90 && ratio <= 1.0f) R.color.text_primary else pillText)) 

        // --- Click Action with Haptics & Privacy Toggle (Double Tap) ---
        val gestureDetector = android.view.GestureDetector(context, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent): Boolean {
                view?.findViewById<View>(R.id.container_progress)?.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, InsightsFragment())
                    .addToBackStack(null)
                    .commit()
                return true
            }

            override fun onDoubleTap(e: android.view.MotionEvent): Boolean {
                // Toggle Privacy Mode
                val current = TransactionManager.isPrivacyMode(context)
                TransactionManager.setPrivacyMode(context, !current)
                view?.findViewById<View>(R.id.container_progress)?.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                updateDashboard() // Refresh UI
                return true
            }
        })

        view?.findViewById<View>(R.id.container_progress)?.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            true
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

    // Helper to mask values
    private fun formatMoney(context: android.content.Context, amount: Float): String {
        return if (TransactionManager.isPrivacyMode(context)) "••••" else "₹${amount.toInt()}"
    }

    private fun formatMoney(context: android.content.Context, text: String): String {
        return if (TransactionManager.isPrivacyMode(context)) "••••" else text
    }
}
