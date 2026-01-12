package com.finetract.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finetract.ui.MainViewModel
import com.finetract.ui.UiState

@Composable
fun ProfileScreen(uiState: UiState, viewModel: MainViewModel) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLimitEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings & Limits", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Spending Limit", style = MaterialTheme.typography.titleMedium)
                        Text("₹${uiState.dailyLimit}", style = MaterialTheme.typography.headlineSmall)
                    }
                    Button(onClick = { showLimitEdit = true }) {
                        Text("Edit")
                    }
                }
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("App Information", style = MaterialTheme.typography.titleMedium)
                Text("Version: 2.0.0 (Algorithmic Edition)", style = MaterialTheme.typography.bodyMedium)
                Text("Engine: Constraint-Driven Expenditure Tracker", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { showDeleteConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset All Data")
        }
    }

    if (showLimitEdit) {
        var limitInput by remember { mutableStateOf(uiState.dailyLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showLimitEdit = false },
            title = { Text("Set Daily Limit") },
            text = {
                TextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Daily Threshold (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newLimit = limitInput.toDoubleOrNull() ?: uiState.dailyLimit
                    viewModel.updateDailyLimit(newLimit)
                    showLimitEdit = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitEdit = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Confirm Reset") },
            text = { Text("This will permanently delete all transactions and categories. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
