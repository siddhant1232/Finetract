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
        
        // Progress Logic
        val progress = if (limit > 0) ((spend / limit) * 100).toInt() else 0
        
        // Color Logic
        val colorRes = when {
            spend > limit -> R.color.danger_red
            progress >= 90 -> R.color.warning_yellow
            else -> R.color.success_green
        }
        val statusColor = ContextCompat.getColor(context, colorRes)

        // Bind UI
        view?.findViewById<TextView>(R.id.tv_spend_amount)?.text = "₹${spend.toInt()}"
        view?.findViewById<TextView>(R.id.tv_limit_info)?.text = "/ ₹${limit.toInt()}"
        
        val progressBar = view?.findViewById<LinearProgressIndicator>(R.id.progress_limit)
        progressBar?.progress = progress.coerceIn(0, 100)
        progressBar?.setIndicatorColor(statusColor)

        // Status Message
        val statusMsg = view?.findViewById<TextView>(R.id.tv_status_message)
        if (spend > limit) {
             statusMsg?.text = getString(R.string.status_alert)
             statusMsg?.setTextColor(statusColor)
        } else {
             statusMsg?.text = getString(R.string.positive_reinforcement)
             statusMsg?.setTextColor(ContextCompat.getColor(context, R.color.primary))
        }
    }
}
