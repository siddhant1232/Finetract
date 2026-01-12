package com.finetract.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finetract.ui.MainViewModel
import com.finetract.ui.UiState

@Composable
fun WalletScreen(uiState: UiState, viewModel: MainViewModel) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Category") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Wallet & Budgets", style = MaterialTheme.typography.headlineMedium)
            }

            items(uiState.categories, key = { it.id }) { category ->
                val spent = uiState.categoryTotals.find { it.categoryId == category.id }?.totalAmount ?: 0.0
                val progress = if (category.budgetLimit > 0) (spent / category.budgetLimit).toFloat() else 0f
                
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category.name, style = MaterialTheme.typography.titleMedium)
                            Text("Limit: ₹${category.budgetLimit}", style = MaterialTheme.typography.labelLarge)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth(),
                            color = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Spent ₹$spent of ₹${category.budgetLimit}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        var name by remember { mutableStateOf("") }
        var limit by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("New Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    TextField(value = limit, onValueChange = { limit = it }, label = { Text("Budget Limit") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCategory(name, limit.toDoubleOrNull() ?: 0.0)
                    showAddCategoryDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
