package com.lym.quietmind.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import com.lym.quietmind.viewmodel.DashboardViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("日", "周", "月")

    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = uiState.currentTab)
    val coroutineScope = rememberCoroutineScope()
    
    // Refresh data when the screen is navigated to
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }
    
    // Sync UI swipe -> ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (uiState.currentTab != pagerState.currentPage) {
            viewModel.setTab(pagerState.currentPage)
        }
    }
    
    // Sync ViewModel change -> UI swipe (for tab click)
    LaunchedEffect(uiState.currentTab) {
        if (pagerState.currentPage != uiState.currentTab) {
            pagerState.animateScrollToPage(uiState.currentTab)
        }
    }

    // Auto-update chart whenever recentSessions change
    LaunchedEffect(uiState.recentSessions) {
        if (uiState.recentSessions.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                val entries = uiState.recentSessions.mapIndexed { index, entity -> 
                    FloatEntry(x = index.toFloat(), y = entity.actualDuration.toFloat())
                }
                chartEntryModelProducer.setEntries(entries)
            }
        }
    }

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { 
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) { page ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Summary Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        title = "总时长(分钟)",
                        value = buildString { append(String.format("%.1f", uiState.totalDuration)) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "总打断次数",
                        value = uiState.totalInterruptions.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        title = "平均单次时长",
                        value = buildString { append(String.format("%.1f", uiState.avgDuration)) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "单场平均走神",
                        value = buildString { append(String.format("%.1f", uiState.avgInterruptions)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Line Chart
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("近期时长趋势 (分钟)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.recentSessions.isNotEmpty()) {
                            Chart(
                                chart = lineChart(),
                                chartModelProducer = chartEntryModelProducer,
                                startAxis = rememberStartAxis(
                                    title = "时长",
                                    titleComponent = textComponent(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textSize = 12.sp
                                    )
                                ),
                                bottomAxis = rememberBottomAxis(
                                    title = "序列",
                                    titleComponent = textComponent(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textSize = 12.sp
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text("此区间暂无数据", color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // GNPS Display
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "神经重塑评估 (GNPS)", 
                            color = MaterialTheme.colorScheme.onPrimaryContainer, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🧠 耐力维度 (Endurance): ${String.format("%.1f", uiState.gnpsEndurance)}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("💧 纯净维度 (Purity): ${String.format("%.1f", uiState.gnpsPurity)}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("🛡️ 抗压维度 (Resistance): ${String.format("%.1f", uiState.gnpsResistance)}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        } // End LazyColumn
        } // End HorizontalPager
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
