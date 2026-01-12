package com.finetract.util

import com.finetract.data.local.entities.TransactionType

data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val vendor: String,
    val isPending: Boolean = false
)

object SmsParser {
    private val DEBIT_REGEX = "(?i)debited by (?:Rs\\.?\\s?)?([\\d,.]+)".toRegex()
    private val CREDIT_REGEX = "(?i)credited by (?:Rs\\.?\\s?)?([\\d,.]+)".toRegex()
    private val REQUESTED_REGEX = "(?i)requested (?:Rs\\.?\\s?)?([\\d,.]+)".toRegex()
    
    private val TRF_TO_REGEX = "(?i)trf to ([\\s\\w]+?)(?: on|\\.|$)".toRegex()
    private val TRANSFER_FROM_REGEX = "(?i)transfer from ([\\s\\w]+?)(?: on|\\.|$)".toRegex()
    private val REQUESTED_BY_REGEX = "(?i)requested by ([\\s\\w]+?)(?: on|\\.|$)".toRegex()
    private val FRM_U_REGEX = "(?i)frm u on ([\\s\\w]+?)(?: on|\\.|$)".toRegex()

    fun parse(message: String): ParsedTransaction? {
        // Expense Check
        DEBIT_REGEX.find(message)?.let { match ->
            val amount = match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            val vendorMatch = TRF_TO_REGEX.find(message)
            val vendor = vendorMatch?.groupValues?.getOrNull(1)?.trim() ?: "Unknown"
            return ParsedTransaction(amount, TransactionType.EXPENSE, vendor)
        }

        // Income Check
        CREDIT_REGEX.find(message)?.let { match ->
            val amount = match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            val vendorMatch = TRANSFER_FROM_REGEX.find(message)
            val vendor = vendorMatch?.groupValues?.getOrNull(1)?.trim() ?: "Unknown"
            return ParsedTransaction(amount, TransactionType.INCOME, vendor)
        }

        // Pending/Request Check
        REQUESTED_REGEX.find(message)?.let { match ->
            val amount = match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            val vendorMatch = REQUESTED_BY_REGEX.find(message) ?: FRM_U_REGEX.find(message)
            val vendor = vendorMatch?.groupValues?.getOrNull(1)?.trim() ?: "External Request"
            return ParsedTransaction(amount, TransactionType.EXPENSE, vendor, isPending = true)
        }

        return null
    }
}
