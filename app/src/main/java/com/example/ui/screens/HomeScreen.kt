package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StreakViewModel
import com.example.ui.components.CompletionRingCard
import com.example.ui.components.HabitAtRiskBanner
import com.example.ui.components.HabitCardItem
import com.example.ui.components.WidgetPreviewCard

import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.example.data.local.HabitEntity
import com.example.ui.components.JoinCircleDialog

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.StreakOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StreakViewModel,
    onHabitClick: (String) -> Unit,
    onCreateHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val habitsAtRisk by viewModel.habitsAtRisk.collectAsState()
    val allMembers by viewModel.allSquadMembers.collectAsState()
    val nudgeTimestamps by viewModel.nudgeTimestamps.collectAsState()

    var showJoinDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Shared Accountability",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showJoinDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("top_join_circle_btn")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Link,
                                        contentDescription = "Join Circle via Link",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onCreateHabitClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("top_add_habit_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Create", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Circle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_list")
        ) {
            // Daily completion ring
            item {
                CompletionRingCard(stats = dashboardStats)
            }

            // Habits at risk warning banner
            if (habitsAtRisk.isNotEmpty()) {
                item {
                    HabitAtRiskBanner(
                        habitsAtRisk = habitsAtRisk,
                        onCheckInClick = { habitId -> viewModel.markHabitDone(habitId) }
                    )
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE HABIT CIRCLES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${habits.size} Circles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (habits.isEmpty()) {
                item {
                    EmptyHabitsIllustrationCard(
                        onCreateHabitClick = onCreateHabitClick,
                        onJoinCircleClick = { showJoinDialog = true }
                    )
                }
            } else {
                // Habit Cards
                items(habits, key = { it.id }) { habit ->
                    val squadForHabit = allMembers.filter { it.habitId == habit.id }
                    val habitNudgeTime = nudgeTimestamps[habit.id] ?: 0L
                    HabitCardItem(
                        habit = habit,
                        squadMembers = squadForHabit,
                        onCardClick = { onHabitClick(habit.id) },
                        onCheckInClick = { viewModel.markHabitDone(habit.id) },
                        onNudgeClick = { viewModel.sendReaction(habit.id, "Squad", "NUDGE") },
                        onDeleteClick = { habitToDelete = habit },
                        nudgeTimestamp = habitNudgeTime
                    )
                }
            }
        }
    }

    if (showJoinDialog) {
        JoinCircleDialog(
            viewModel = viewModel,
            onDismiss = { showJoinDialog = false },
            onJoined = { newHabitId ->
                onHabitClick(newHabitId)
            }
        )
    }

    if (habitToDelete != null) {
        val targetHabit = habitToDelete!!
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Leave '${targetHabit.squadName}'?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to leave '${targetHabit.name}'? Your active streak of ${targetHabit.currentStreak} days will be reset, and you will no longer receive circle updates.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = targetHabit.id
                        habitToDelete = null
                        viewModel.deleteHabit(id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_delete_habit_home_btn")
                ) {
                    Text("Leave Circle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { habitToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_delete_habit_home_btn")
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyHabitsIllustrationCard(
    onCreateHabitClick: () -> Unit,
    onJoinCircleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeroBreathingAnimation")
    val floatScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeroScale"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val cardGradient = remember(surfaceColor, primaryContainer) {
        Brush.verticalGradient(
            colors = listOf(
                primaryContainer.copy(alpha = 0.25f),
                surfaceColor,
                surfaceColor
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("empty_habits_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visual Graphic Hero Illustration Container with breathing scale
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(scaleX = floatScale, scaleY = floatScale)
                ) {
                    // Outer glow halo
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        StreakOrange.copy(alpha = 0.25f),
                                        StreakOrange.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Inner Circle Accent
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(StreakOrange.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Circle",
                            tint = StreakOrange,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    // Floating Flame Badge Accent
                    Surface(
                        shape = CircleShape,
                        color = StreakOrange,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Build Your First Habit Circle!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Track daily goals together, stay accountable, and build strong streaks. Start your first circle with friends or join an existing habit circle!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Button
                Button(
                    onClick = onCreateHabitClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StreakOrange,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create_first_habit_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create your first habit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Button
                OutlinedButton(
                    onClick = onJoinCircleClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("join_first_habit_btn")
                ) {
                    Icon(imageVector = Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join via Invite Link or Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
