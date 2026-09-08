package com.example.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SurfaceColor
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance & Usage Stats") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            Text("Daily Queries", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            
            val queryChartModel = entryModelOf(1f to 5f, 2f to 15f, 3f to 10f, 4f to 25f, 5f to 20f, 6f to 30f, 7f to 12f)
            
            Surface(
                color = SurfaceColor,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                Chart(
                    chart = lineChart(
                        lines = listOf(lineSpec(lineColor = PrimaryBlue))
                    ),
                    model = queryChartModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Token Usage", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            
            val tokenChartModel = entryModelOf(1f to 1000f, 2f to 3000f, 3f to 1500f, 4f to 5000f, 5f to 4000f, 6f to 6000f, 7f to 2000f)
            
            Surface(
                color = SurfaceColor,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                Chart(
                    chart = lineChart(
                        lines = listOf(lineSpec(lineColor = PrimaryPurple))
                    ),
                    model = tokenChartModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
