package com.finetract.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finetract.data.local.entities.TransactionType
import com.finetract.ui.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(uiState: UiState) {
    val groupedTransactions = uiState.transactions.groupBy {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.timestamp))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedTransactions.forEach { (month, transactions) ->
            item {
                Text(
                    text = month,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            items(transactions, key = { it.id }) { transaction ->
                ListItem(
                    headlineContent = { Text(transaction.note.ifEmpty { "Transaction" }) },
                    supportingContent = {
                        val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
                        Text(date)
                    },
                    trailingContent = {
                        val sign = if (transaction.type == TransactionType.INCOME) "+" else "-"
                        val color = if (transaction.type == TransactionType.INCOME)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        Text("$sign ₹${transaction.amount}", color = color, style = MaterialTheme.typography.titleMedium)
                    }
                )
                Divider()
            }
        }
    }
}
