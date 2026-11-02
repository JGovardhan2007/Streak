package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.HabitEntity
import com.example.data.local.SquadMemberEntity
import com.example.ui.StreakViewModel
import com.example.ui.theme.StreakOrange

enum class WidgetType {
    COMPACT_2X2,
    SQUAD_4X2,
    STATS_4X1
}

@Composable
fun WidgetPreviewCard(
    viewModel: StreakViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()
    val squadMembers by viewModel.allSquadMembers.collectAsState()
    val currentThemeAccent by viewModel.currentThemeAccent.collectAsState()
    val isAppDarkMode by viewModel.isDarkMode.collectAsState()

    val accentColor = currentThemeAccent.primaryColor

    var selectedType by remember { mutableStateOf(WidgetType.COMPACT_2X2) }
    var isWidgetDarkMode by remember(isAppDarkMode) { mutableStateOf(isAppDarkMode) }
    var showSetupHelpDialog by remember { mutableStateOf(false) }

    val primaryHabit = habits.firstOrNull { !it.isCheckedInToday } ?: habits.firstOrNull()

    val widgetBgColor = if (isWidgetDarkMode) Color(0xFF16181F) else Color(0xFFF3F4F6)
    val widgetTextColor = if (isWidgetDarkMode) Color(0xFFF9FAFB) else Color(0xFF111827)
    val widgetSubtextColor = if (isWidgetDarkMode) Color(0xFF9CA3AF) else Color(0xFF4B5563)
    val widgetBorderColor = if (isWidgetDarkMode) Color(0xFF2E323D) else Color(0xFFE5E7EB)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_screen_widget_preview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Card Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = "Widget Icon",
                            tint = accentColor,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Home Screen Widgets (${currentThemeAccent.displayName})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Choose from 3 launcher widget styles",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showSetupHelpDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Widget Setup Guide",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector Tabs (3 Widget Options)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedType == WidgetType.COMPACT_2X2,
                    onClick = { selectedType = WidgetType.COMPACT_2X2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Compact 2x2", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                SegmentedButton(
                    selected = selectedType == WidgetType.SQUAD_4X2,
                    onClick = { selectedType = WidgetType.SQUAD_4X2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Circle 4x2", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                SegmentedButton(
                    selected = selectedType == WidgetType.STATS_4X1,
                    onClick = { selectedType = WidgetType.STATS_4X1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("Stats Bar 4x1", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Widget Canvas Simulated Outer Frame
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulated_widget_canvas"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE HOMESCREEN PREVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Mode Toggle Pill
                        Surface(
                            shape = CircleShape,
                            color = widgetBgColor,
                            border = BorderStroke(1.dp, widgetBorderColor),
                            modifier = Modifier.clickable { isWidgetDarkMode = !isWidgetDarkMode }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isWidgetDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Toggle Preview Theme",
                                    tint = widgetTextColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isWidgetDarkMode) "Dark" else "Light",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = widgetTextColor
                                )
                            }
                        }
                    }

                    // Render Simulated Widget Type
                    AnimatedContent(
                        targetState = selectedType,
                        label = "WidgetTypeAnimation"
                    ) { target ->
                        when (target) {
                            WidgetType.COMPACT_2X2 -> {
                                SimulatedCompactWidget(
                                    primaryHabit = primaryHabit,
                                    totalStreak = stats.totalStreakDays,
                                    completedCount = stats.completedHabits,
                                    totalCount = stats.totalHabits,
                                    bgColor = widgetBgColor,
                                    textColor = widgetTextColor,
                                    subtextColor = widgetSubtextColor,
                                    borderColor = widgetBorderColor,
                                    accentColor = accentColor,
                                    onCheckIn = {
                                        primaryHabit?.let { viewModel.markHabitDone(it.id) }
                                    }
                                )
                            }
                            WidgetType.SQUAD_4X2 -> {
                                SimulatedSquadWidget(
                                    primaryHabit = primaryHabit,
                                    squadMembers = squadMembers,
                                    totalStreak = stats.totalStreakDays,
                                    bgColor = widgetBgColor,
                                    textColor = widgetTextColor,
                                    subtextColor = widgetSubtextColor,
                                    borderColor = widgetBorderColor,
                                    accentColor = accentColor,
                                    onCheckIn = {
                                        primaryHabit?.let { viewModel.markHabitDone(it.id) }
                                    },
                                    onNudge = {
                                        primaryHabit?.let { viewModel.sendReaction(it.id, "Squad", "NUDGE") }
                                    }
                                )
                            }
                            WidgetType.STATS_4X1 -> {
                                SimulatedStatsWidget(
                                    totalStreak = stats.totalStreakDays,
                                    completedCount = stats.completedHabits,
                                    totalCount = stats.totalHabits,
                                    bgColor = widgetBgColor,
                                    textColor = widgetTextColor,
                                    subtextColor = widgetSubtextColor,
                                    borderColor = widgetBorderColor,
                                    accentColor = accentColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer action note
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎨 Colors automatically synchronize with your selected App Color Theme (${currentThemeAccent.displayName})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showSetupHelpDialog) {
        AlertDialog(
            onDismissRequest = { showSetupHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📲 How to Add Widgets to Android Home Screen")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Long press any open space on your Android home screen.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "2. Tap 'Widgets' from the pop-up menu.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "3. Scroll to 'Streak'. You will see 3 distinct widget options:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "• Compact (2x2): Quick check-in for top habit\n• Circle (4x2): Circle activity & ⚡ Nudge button\n• Streak Stats (4x1): Fire streak counter & completion bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "4. Touch & drag your favorite widget onto your home screen!",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSetupHelpDialog = false }) {
                    Text("Got it!", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SimulatedCompactWidget(
    primaryHabit: HabitEntity?,
    totalStreak: Int,
    completedCount: Int,
    totalCount: Int,
    bgColor: Color,
    textColor: Color,
    subtextColor: Color,
    borderColor: Color,
    accentColor: Color,
    onCheckIn: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STREAK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = accentColor
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StreakOrange.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "🔥 $totalStreak Days",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = StreakOrange,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = primaryHabit?.name ?: "All Habits Complete!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                maxLines = 1
            )

            Text(
                text = "$completedCount of $totalCount Habits Complete Today",
                style = MaterialTheme.typography.labelSmall,
                color = subtextColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCheckIn,
                enabled = primaryHabit != null && !primaryHabit.isCheckedInToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.Black,
                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                    disabledContentColor = Color.Black.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (primaryHabit?.isCheckedInToday == true) {
                    Text("Done ✓", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Text("Quick Check-in ✓", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SimulatedSquadWidget(
    primaryHabit: HabitEntity?,
    squadMembers: List<SquadMemberEntity>,
    totalStreak: Int,
    bgColor: Color,
    textColor: Color,
    subtextColor: Color,
    borderColor: Color,
    accentColor: Color,
    onCheckIn: () -> Unit,
    onNudge: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "STREAK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔥 $totalStreak Days",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = StreakOrange
                    )
                }

                // Member Avatars Stack
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    squadMembers.take(4).forEach { member ->
                        AsyncImage(
                            model = member.avatarUrl,
                            contentDescription = member.name,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, bgColor, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = primaryHabit?.name ?: "All Habits Complete!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1
                    )
                    Text(
                        text = "Circle Activity: 3 members checked in",
                        style = MaterialTheme.typography.labelSmall,
                        color = subtextColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Send Nudge button
                    IconButton(
                        onClick = onNudge,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StreakOrange.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.ElectricBolt,
                                    contentDescription = "Nudge Circle",
                                    tint = StreakOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Check-in button
                    Button(
                        onClick = onCheckIn,
                        enabled = primaryHabit != null && !primaryHabit.isCheckedInToday,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black,
                            disabledContainerColor = accentColor.copy(alpha = 0.4f),
                            disabledContentColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (primaryHabit?.isCheckedInToday == true) {
                            Text("Done ✓", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Text("Check-in ✓", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedStatsWidget(
    totalStreak: Int,
    completedCount: Int,
    totalCount: Int,
    bgColor: Color,
    textColor: Color,
    subtextColor: Color,
    borderColor: Color,
    accentColor: Color
) {
    val pct = if (totalCount > 0) ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 0
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StreakOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🔥 $totalStreak",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = StreakOrange,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "$pct% Daily Goals Met",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Text(
                        text = "$completedCount / $totalCount Habits Completed Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = subtextColor
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor
            ) {
                Text(
                    text = "Open ⚡",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
