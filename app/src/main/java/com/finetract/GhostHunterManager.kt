package com.finetract

import android.content.Context
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object GhostHunterManager {

    data class GhostSubscription(
        val merchant: String,
        val amount: Float,
        val detectedDate: Long,
        val cycleDays: Int = 30
    )

    fun scanForGhosts(context: Context): List<GhostSubscription> {
        val transactions = TransactionManager.getTransactions(context)
        val ghosts = mutableListOf<GhostSubscription>()

        // 1. Group by Merchant
        val byMerchant = transactions.groupBy { it.merchant }

        for ((merchant, txns) in byMerchant) {
            // Ignore generic merchants like "UPI" or "Cash" (if any)
            if (merchant.equals("UPI", ignoreCase = true) || merchant.equals("Cash", ignoreCase = true)) continue
            
            // Need at least 2 transactions to detect a pattern (or 1 if we are aggressive, but 2 is safer)
            // For demo/short history, we might relax this or look for known names.
            // Let's look for known keywords too: "Netflix", "Spotify", "Apple", "Prime"
            if (isKnownSubscription(merchant)) {
                // If it's a known sub, even 1 transaction is suspicious if it looks like a sub amount
                // But let's stick to pattern matching for "Ghost" logic mainly.
            }

            // 2. Group by Amount within Merchant (Subs usually imply exact same price)
            val byAmount = txns.groupBy { it.amount }
            
            for ((amount, samePriceTxns) in byAmount) {
                if (samePriceTxns.size < 2) continue
                
                // Sort by date
                val sorted = samePriceTxns.sortedBy { it.timestamp }
                
                // Check gaps
                for (i in 0 until sorted.size - 1) {
                    val t1 = sorted[i]
                    val t2 = sorted[i+1]
                    val diffMs = t2.timestamp - t1.timestamp
                    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
                    
                    // Allow slack: 25 to 35 days (Monthly)
                    if (diffDays in 25..35) {
                        ghosts.add(GhostSubscription(merchant, amount, t2.timestamp))
                        break // Found one pattern for this price/merchant, enough to flag
                    }
                }
            }
        }
        
        return ghosts
    }

    private fun isKnownSubscription(name: String): Boolean {
        val killers = listOf("Netflix", "Spotify", "Apple", "Prime", "Hotstar", "SonyLIV", "Youtube")
        return killers.any { name.contains(it, ignoreCase = true) }
    }
}
