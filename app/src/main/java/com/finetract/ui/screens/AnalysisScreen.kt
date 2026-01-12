package com.finetract.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finetract.ui.UiState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel

@Composable
fun AnalysisScreen(uiState: UiState) {
    val model = remember(uiState.dailyTotals) {
        if (uiState.dailyTotals.isNotEmpty()) {
            CartesianChartModel(
                LineCartesianLayerModel.build {
                    series(uiState.dailyTotals.map { it.totalAmount })
                }
            )
        } else {
            null
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Expenditure Trends", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily Spend Trend", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (model != null) {
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                layers = arrayOf(rememberLineCartesianLayer()),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                            ),
                            model = model,
                            modifier = Modifier.height(250.dp)
                        )
                    } else {
                        Box(modifier = Modifier.height(250.dp).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("No data available for the period")
                        }
                    }
                }
            }
        }

        item {
            Text("Category Breakdown", style = MaterialTheme.typography.titleLarge)
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
