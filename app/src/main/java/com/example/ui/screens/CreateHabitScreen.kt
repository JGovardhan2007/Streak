package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StreakViewModel

enum class FrequencyType {
    DAILY, WEEKLY_TARGET, SPECIFIC_DAYS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    viewModel: StreakViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var habitName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🏋️") }

    // Enhanced Target Frequency State
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var weeklyCount by remember { mutableIntStateOf(3) }
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = remember { mutableStateListOf("Mon", "Wed", "Fri") }

    val emojis = listOf("🏋️", "🏃", "📚", "💧", "🧘", "🍳", "🎸", "🚴", "🛌", "🎨", "💻", "🧠", "🎯")

    // Derive formatted frequency description
    val frequencyGoalString = when (frequencyType) {
        FrequencyType.DAILY -> "Daily"
        FrequencyType.WEEKLY_TARGET -> "$weeklyCount times per week"
        FrequencyType.SPECIFIC_DAYS -> if (selectedDays.isEmpty()) "Daily" else selectedDays.joinToString(", ")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Habit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("create_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Habit Name Input
            item {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Daily 20m Morning Yoga") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_habit_name")
                )
            }

            // Enhanced Target Frequency Selector
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TARGET FREQUENCY",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Mode Selector Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = frequencyType == FrequencyType.DAILY,
                                onClick = { frequencyType = FrequencyType.DAILY },
                                label = { Text("Daily", fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("chip_freq_daily")
                            )
                            FilterChip(
                                selected = frequencyType == FrequencyType.WEEKLY_TARGET,
                                onClick = { frequencyType = FrequencyType.WEEKLY_TARGET },
                                label = { Text("Weekly Goal", fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("chip_freq_weekly")
                            )
                            FilterChip(
                                selected = frequencyType == FrequencyType.SPECIFIC_DAYS,
                                onClick = { frequencyType = FrequencyType.SPECIFIC_DAYS },
                                label = { Text("Specific Days", fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("chip_freq_specific")
                            )
                        }

                        // Sub-options based on mode
                        when (frequencyType) {
                            FrequencyType.DAILY -> {
                                Text(
                                    text = "🔥 Commitment: Every day of the week (7 days)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FrequencyType.WEEKLY_TARGET -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Select how many times per week:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        FilledIconButton(
                                            onClick = { if (weeklyCount > 1) weeklyCount-- },
                                            enabled = weeklyCount > 1,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        ) {
                                            Text(
                                                text = "$weeklyCount times / week",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }

                                        FilledIconButton(
                                            onClick = { if (weeklyCount < 6) weeklyCount++ },
                                            enabled = weeklyCount < 6,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase")
                                        }
                                    }
                                }
                            }
                            FrequencyType.SPECIFIC_DAYS -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Select target days:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        daysOfWeek.forEach { day ->
                                            val isSelected = selectedDays.contains(day)
                                            val primaryColor = MaterialTheme.colorScheme.primary
                                            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) primaryColor else surfaceVariant)
                                                    .clickable {
                                                        if (isSelected) selectedDays.remove(day)
                                                        else selectedDays.add(day)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.take(2),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Target Summary Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🎯 Target: $frequencyGoalString",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Emoji / Icon Picker
            item {
                val accentColor = MaterialTheme.colorScheme.primary
                Text(
                    text = "CHOOSE ICON EMOJI",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(emojis) { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) accentColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) accentColor else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (habitName.isNotBlank()) {
                                val cleanName = habitName.trim()
                                viewModel.createHabit(
                                    name = cleanName,
                                    category = "General",
                                    emoji = selectedEmoji,
                                    squadName = cleanName,
                                    invitedFriends = emptyList(),
                                    frequencyGoal = frequencyGoalString
                                )
                                viewModel.shareHabitCircleInviteLink(context, "habit_new", cleanName, cleanName)
                                onBackClick()
                            }
                        },
                        enabled = habitName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_create_and_share_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share Link")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Habit & Share Invite Link", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (habitName.isNotBlank()) {
                                val cleanName = habitName.trim()
                                viewModel.createHabit(
                                    name = cleanName,
                                    category = "General",
                                    emoji = selectedEmoji,
                                    squadName = cleanName,
                                    invitedFriends = emptyList(),
                                    frequencyGoal = frequencyGoalString
                                )
                                onBackClick()
                            }
                        },
                        enabled = habitName.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_create_habit_btn")
                    ) {
                        Text("Create Habit", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
