package com.finetract.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.finetract.ui.MainViewModel
import com.finetract.ui.UiEffect
import com.finetract.ui.UiState
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(uiState: UiState, viewModel: MainViewModel) {
    var showLimitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            if (effect is UiEffect.ShowOverLimitPopup) {
                showLimitDialog = true
            }
        }
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Daily Limit Reached") },
            text = { Text("You have exceeded your daily spending limit of ₹${uiState.dailyLimit}. Current spend: ₹${uiState.dailyExpenditure}") },
            confirmButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    val progress = if (uiState.dailyLimit > 0) (uiState.dailyExpenditure / uiState.dailyLimit).toFloat() else 0f
    val remaining = (uiState.dailyLimit - uiState.dailyExpenditure).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Daily Overview", style = MaterialTheme.typography.headlineMedium)
        }
        
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Spent Today", style = MaterialTheme.typography.titleMedium)
                            Text("₹${uiState.dailyExpenditure}", style = MaterialTheme.typography.headlineLarge)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", style = MaterialTheme.typography.labelMedium)
                            Text("₹$remaining", color = if (remaining > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Limit: ₹${uiState.dailyLimit}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
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
