package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.CategoryConsistency
import com.example.ui.DayConsistencyData
import com.example.ui.StreakViewModel
import com.example.ui.UserSquadInsight
import com.example.ui.WeeklyTrendPoint
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.StreakOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: StreakViewModel,
    modifier: Modifier = Modifier
) {
    val insights by viewModel.insightsData.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val accentColor = LocalAccentColor.current

    var selectedChartType by remember { mutableStateOf("Curve") } // "Curve" or "Bar"
    var selectedSquadFilter by remember { mutableStateOf("All Squads") }
    var selectedTrendPoint by remember { mutableStateOf<WeeklyTrendPoint?>(null) }

    val squadOptions = remember(habits) {
        listOf("All Squads") + habits.map { it.squadName }.distinct()
    }

    val filteredUserInsights = remember(insights.userSquadInsights, selectedSquadFilter) {
        if (selectedSquadFilter == "All Squads") {
            insights.userSquadInsights
        } else {
            insights.userSquadInsights.filter { it.squadName.equals(selectedSquadFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Habit & Circle Insights",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Consistency trends & circle activity patterns",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Summary KPIs Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("insights_kpi_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "OVERALL CONSISTENCY",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${insights.overallConsistencyScore}%",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 36.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = accentColor.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = "Growth",
                                                tint = accentColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "+${insights.weeklyGrowthPercentage}% this week",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = accentColor
                                            )
                                        }
                                    }
                                }
                            }

                            // Peak active day indicator badge
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = StreakOrange.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, StreakOrange.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🔥", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PEAK DAY",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = StreakOrange
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = insights.peakSquadActiveDay,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${insights.peakSquadActiveRate}% active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Completion Trends Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("insights_chart_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "WEEKLY COMPLETION TREND",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Daily habit check-in distribution (Mon - Sun)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Toggle chart mode (Bar / Curve)
                            SingleChoiceSegmentedButtonRow {
                                SegmentedButton(
                                    selected = selectedChartType == "Curve",
                                    onClick = { selectedChartType = "Curve" },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                ) {
                                    Icon(
                                        Icons.Outlined.ShowChart,
                                        contentDescription = "Curve Chart",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                SegmentedButton(
                                    selected = selectedChartType == "Bar",
                                    onClick = { selectedChartType = "Bar" },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                ) {
                                    Icon(
                                        Icons.Outlined.BarChart,
                                        contentDescription = "Bar Chart",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Chart View
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                        ) {
                            if (selectedChartType == "Curve") {
                                TrendCurveChart(
                                    points = insights.trendPoints,
                                    accentColor = accentColor,
                                    onPointSelected = { selectedTrendPoint = it }
                                )
                            } else {
                                TrendBarChart(
                                    points = insights.trendPoints,
                                    accentColor = accentColor,
                                    onPointSelected = { selectedTrendPoint = it }
                                )
                            }
                        }

                        // Selected point popup detail
                        selectedTrendPoint?.let { pt ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = accentColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.BarChart,
                                            contentDescription = "Detail",
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${pt.label}: ${(pt.completionPercentage * 100).toInt()}% Completion",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${pt.totalCheckIns} / ${pt.targetCheckIns} Check-ins",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Squad Member 'Most Active' Days & Consistency Header
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CIRCLE MEMBER INSIGHTS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Peak active days & consistency heatmaps for circle members",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Squad Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(squadOptions) { option ->
                            val isSelected = selectedSquadFilter == option
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSquadFilter = option },
                                label = { Text(option, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // List of Squad Members with 'Most Active Day' and 7-Day Activity Intensity Heatmap
            items(filteredUserInsights, key = { "${it.userId}_${it.name}" }) { memberInsight ->
                UserSquadInsightCard(
                    userInsight = memberInsight,
                    accentColor = accentColor,
                    onReactionClick = { actionType ->
                        viewModel.sendReaction("habit_gym", memberInsight.name, actionType)
                    }
                )
            }

            // Habit Category Breakdown
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("insights_category_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "HABIT CATEGORY PERFORMANCE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        insights.categoryConsistencies.forEach { cat ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(cat.emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat.category,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Text(
                                        text = "${cat.completedCount} / ${cat.totalHabits} Completed (${(cat.percentage * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { cat.percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = accentColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendCurveChart(
    points: List<WeeklyTrendPoint>,
    accentColor: Color,
    onPointSelected: (WeeklyTrendPoint) -> Unit
) {
    if (points.isEmpty()) return

    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                val pointCount = points.size

                if (pointCount >= 2) {
                    val stepX = width / (pointCount - 1)

                    // Horizontal grid lines
                    listOf(0f, 0.5f, 1.0f).forEach { ratio ->
                        val y = height * (1f - ratio)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val chartPath = Path()
                    val fillPath = Path()

                    val coordinates = points.mapIndexed { i, pt ->
                        val x = i * stepX
                        val y = height * (1f - pt.completionPercentage.coerceIn(0.05f, 0.95f))
                        Offset(x, y)
                    }

                    chartPath.moveTo(coordinates[0].x, coordinates[0].y)
                    fillPath.moveTo(coordinates[0].x, height)
                    fillPath.lineTo(coordinates[0].x, coordinates[0].y)

                    for (i in 0 until coordinates.size - 1) {
                        val p1 = coordinates[i]
                        val p2 = coordinates[i + 1]
                        val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                        val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)

                        chartPath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p2.x, p2.y
                        )
                        fillPath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p2.x, p2.y
                        )
                    }

                    fillPath.lineTo(coordinates.last().x, height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )

                    drawPath(
                        path = chartPath,
                        color = accentColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    coordinates.forEachIndexed { _, coord ->
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = coord
                        )
                        drawCircle(
                            color = accentColor,
                            radius = 3.5.dp.toPx(),
                            center = coord
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable { onPointSelected(pt) }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun TrendBarChart(
    points: List<WeeklyTrendPoint>,
    accentColor: Color,
    onPointSelected: (WeeklyTrendPoint) -> Unit
) {
    if (points.isEmpty()) return

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            points.forEach { pt ->
                val barHeightFraction = pt.completionPercentage.coerceIn(0.12f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onPointSelected(pt) }
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(barHeightFraction)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(accentColor, accentColor.copy(alpha = 0.45f))
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPointSelected(pt) }
                ) {
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UserSquadInsightCard(
    userInsight: UserSquadInsight,
    accentColor: Color,
    onReactionClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (userInsight.isUser) BorderStroke(1.5.dp, accentColor) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_insight_card_${userInsight.name}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: User details + Consistency badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = userInsight.avatarUrl,
                        contentDescription = userInsight.name,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, accentColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userInsight.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (userInsight.isUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = accentColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "YOU",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Circle: ${userInsight.squadName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Overall Consistency Score
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${userInsight.overallConsistencyPercentage}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = accentColor
                        )
                        Text(
                            text = "Consistency",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Highlight: Most Active Day Badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = StreakOrange.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, StreakOrange.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Peak",
                            tint = StreakOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Most Active Day: ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = userInsight.mostActiveDay,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = StreakOrange
                        )
                    }
                    Text(
                        text = "Peak ${userInsight.activeTimeOfDay}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Activity Intensity Bar Distribution (Mon-Sun)
            Text(
                text = "7-Day Active Intensity Pattern (Mon - Sun)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                daysOfWeek.forEachIndexed { idx, dayLabel ->
                    val intensity = userInsight.dayActivityScores.getOrElse(idx) { 0.5f }
                    val isPeakDay = dayLabel == userInsight.mostActiveDayShort || (idx == 2 && userInsight.mostActiveDayShort == "Wed")

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((36 * intensity).dp.coerceAtLeast(6.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isPeakDay) StreakOrange
                                    else accentColor.copy(alpha = (intensity * 0.9f).coerceIn(0.2f, 1f))
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isPeakDay) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (isPeakDay) StreakOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (!userInsight.isUser) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onReactionClick("CHEER") },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cheer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onReactionClick("NUDGE") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StreakOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nudge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
