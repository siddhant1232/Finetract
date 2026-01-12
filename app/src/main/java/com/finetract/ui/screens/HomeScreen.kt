package com.finetract.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finetract.ui.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(uiState: UiState) {
    val balance = uiState.monthlyIncome - uiState.monthlyExpense
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        }
        
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Balance", style = MaterialTheme.typography.titleMedium)
                    Text("₹${String.format("%.2f", balance)}", style = MaterialTheme.typography.headlineLarge)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Income", style = MaterialTheme.typography.labelMedium)
                            Text("₹${uiState.monthlyIncome}", color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Expenses", style = MaterialTheme.typography.labelMedium)
                            Text("₹${uiState.monthlyExpense}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        
        item {
            Text("Recent Transactions", style = MaterialTheme.typography.titleLarge)
        }
        
        items(uiState.transactions.take(10), key = { it.id }) { transaction ->
            ListItem(
                headlineContent = { Text(transaction.note.ifEmpty { "Transaction" }) },
                supportingContent = { 
                    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
                    Text(date) 
                },
                trailingContent = {
                    val sign = if (transaction.type == com.finetract.data.local.entities.TransactionType.INCOME) "+" else "-"
                    val color = if (transaction.type == com.finetract.data.local.entities.TransactionType.INCOME) 
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Text("$sign ₹${transaction.amount}", color = color, style = MaterialTheme.typography.titleMedium)
                }
            )
            Divider()
        }
    }
}
