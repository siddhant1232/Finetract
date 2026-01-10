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
        
        // 1. Progress & Daily Insight
        val progress = if (limit > 0) ((spend / limit) * 100).toInt() else 0
        val ratio = if (limit > 0) spend / limit else 0f
        
        // Color Logic
        val colorRes = when {
            spend > limit -> R.color.danger_red
            progress >= 90 -> R.color.warning_yellow
            else -> R.color.success_green
        }
        val statusColor = ContextCompat.getColor(context, colorRes)

        // Bind Main Progress
        view?.findViewById<TextView>(R.id.tv_spend_amount)?.text = "₹${spend.toInt()}"
        view?.findViewById<TextView>(R.id.tv_limit_info)?.text = getString(R.string.label_spent_of) + " ₹${limit.toInt()}"
        
        val progressBar = view?.findViewById<com.google.android.material.progressindicator.CircularProgressIndicator>(R.id.progress_limit)
        
        // Animation: Smooth Progress
        val animator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", progressBar?.progress ?: 0, progress.coerceIn(0, 100))
        animator.duration = 800 // Smooth transition
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.start()
        
        progressBar?.setIndicatorColor(statusColor)

        // Bind Daily Insight (Mandatory)
        val tvInsight = view?.findViewById<TextView>(R.id.tv_daily_insight)
        tvInsight?.text = when {
            ratio <= 0.6f -> "You’re well within your limit today."
            ratio <= 0.9f -> "You’re getting close to today’s limit."
            else -> "You’ve crossed today’s limit."
        }
        tvInsight?.setTextColor(statusColor)
        
        // 2. Today at a Glance
        val stats = TransactionManager.getTodayStats(context)
        view?.findViewById<TextView>(R.id.tv_glance_txns)?.text = "• ${stats.count} transactions today"
        
        val maxText = if (stats.maxAmount > 0) "• Highest: ₹${stats.maxAmount.toInt()} (${stats.maxCategory})" else "• Highest: -"
        view?.findViewById<TextView>(R.id.tv_glance_highest)?.text = maxText
        
        view?.findViewById<TextView>(R.id.tv_glance_remaining)?.text = "• Remaining: ₹${remaining.toInt()}"

        // 3. Behavioral Nudge (X-Factor / Contextual)
        val tvNudge = view?.findViewById<TextView>(R.id.tv_nudge)
        
        // Simple Logic for Nudge as requested
        val nudgeText = when {
            // Priority: Weekend -> Near Limit -> Under Limit
            XFactorManager.getWeekendWarning(context) != null -> XFactorManager.getWeekendWarning(context)
            ratio > 0.8f && ratio <= 1.0f -> "Consider slowing down for the rest of the day."
            else -> "Great control so far 👏" // Default positive reinforcement
        }
        tvNudge?.text = nudgeText
        
        // Add Click Listener to open Analytics
        view?.findViewById<View>(R.id.container_progress)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InsightsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
