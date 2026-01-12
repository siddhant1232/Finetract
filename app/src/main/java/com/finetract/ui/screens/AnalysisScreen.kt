package com.finetract.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.finetract.ui.UiState

@Composable
fun AnalysisScreen(uiState: UiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Analysis", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Text("Daily Expenses (This Month)", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            SimpleBarChart(
                data = uiState.dailyTotals.map { it.totalAmount.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Text("Expenses by Category", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            uiState.categoryTotals.forEach { total ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(total.categoryName)
                    Text("₹${total.totalAmount}", style = MaterialTheme.typography.titleMedium)
                }
                val progress = if (uiState.monthlyExpense > 0) (total.totalAmount / uiState.monthlyExpense).toFloat() else 0f
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Float>, modifier: Modifier, color: Color) {
    val maxValue = (data.maxOrNull() ?: 1f).coerceAtLeast(1f)
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / (data.size.coerceAtLeast(1) * 2f - 1f)
        data.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            drawRect(
                color = color,
                topLeft = Offset(x = index * barWidth * 2, y = size.height - barHeight),
                size = Size(width = barWidth, height = barHeight)
            )
        }
        
        // Baseline
        drawLine(
            color = Color.Gray,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )
    }
}
