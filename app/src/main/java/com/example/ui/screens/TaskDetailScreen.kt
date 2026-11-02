package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.StreakViewModel
import com.example.ui.theme.StreakOrange

import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.InviteFriendDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    habitId: String,
    viewModel: StreakViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitState by viewModel.getHabitFlow(habitId).collectAsState(initial = null)
    val squadMembers by viewModel.getSquadMembersFlow(habitId).collectAsState(initial = emptyList())
    val historyEntries by viewModel.getWeeklyHistoryFlow(habitId).collectAsState(initial = emptyList())

    val habit = habitState ?: return
    var showInviteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showProofDialog by remember { mutableStateOf(false) }
    var proofNote by remember { mutableStateOf("") }
    var photoProofUrl by remember { mutableStateOf("") }
    var memberToRemove by remember { mutableStateOf<com.example.data.local.SquadMemberEntity?>(null) }

    val userMember = squadMembers.find { it.isUser }
    val isUserHost = userMember?.isHost == true || squadMembers.none { it.isHost && !it.isUser }

    // Pulse animation for central flame
    val infiniteTransition = rememberInfiniteTransition(label = "FlameGlow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = habit.squadName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("detail_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showInviteDialog = true },
                        modifier = Modifier.testTag("detail_share_circle_btn")
                    ) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share Circle Link")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("detail_delete_circle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Leave Circle",
                            tint = MaterialTheme.colorScheme.error
                        )
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
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Central Flame & Streak Counter Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("central_flame_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StreakOrange.copy(alpha = 0.12f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "${habit.emoji} ${habit.name}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pulsing Flame Icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(scale)
                                    .clip(CircleShape)
                                    .background(StreakOrange.copy(alpha = 0.18f))
                            )
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Flame",
                                tint = StreakOrange,
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(scale)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${habit.currentStreak} DAYS",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 38.sp,
                                letterSpacing = 1.sp
                            ),
                            color = StreakOrange
                        )

                        Text(
                            text = "ACTIVE CIRCLE STREAK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Check in action CTA
                        val accentColor = MaterialTheme.colorScheme.primary
                        val onAccentColor = MaterialTheme.colorScheme.onPrimary
                        Button(
                            onClick = { viewModel.markHabitDone(habit.id) },
                            enabled = !habit.isCheckedInToday,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = onAccentColor,
                                disabledContainerColor = accentColor.copy(alpha = 0.8f),
                                disabledContentColor = onAccentColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("detail_mark_done_btn")
                        ) {
                            if (habit.isCheckedInToday) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = "Done")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Checked in today", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Mark as Done Today", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showProofDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_proof_btn")
                            ) {
                                Text("📸 Add Proof", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            if (habit.isCheckedInToday) {
                                OutlinedButton(
                                    onClick = { viewModel.undoCheckIn(habit.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("undo_check_in_btn")
                                ) {
                                    Text("↩️ Undo Check-In", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val streakFreezes by viewModel.streakFreezesCount.collectAsState()
                        OutlinedButton(
                            onClick = { viewModel.useStreakFreeze(habit.id) },
                            enabled = !habit.isCheckedInToday && streakFreezes > 0,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("use_streak_freeze_btn")
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Streak Freeze (Available: $streakFreezes 🛡️)", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Frequency Goal & Weekly Target Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("frequency_goal_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        val accentColor = MaterialTheme.colorScheme.primary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TARGET FREQUENCY GOAL",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = habit.frequencyGoal,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val isTargetAchieved = habit.weeklyCompletedCount >= habit.weeklyTargetCount
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTargetAchieved) accentColor.copy(alpha = 0.2f) else StreakOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isTargetAchieved) "Target Reached" else "${maxOf(0, habit.weeklyTargetCount - habit.weeklyCompletedCount)} remaining",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isTargetAchieved) accentColor else StreakOrange,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Weekly Target Progress Bar
                        val progress = (habit.weeklyCompletedCount.toFloat() / habit.weeklyTargetCount.toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Target Progress",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${habit.weeklyCompletedCount} / ${habit.weeklyTargetCount} Completed",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = accentColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Scheduled Deadline Notification Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notification",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "1-Hour Deadline Alert Scheduled",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (habit.isCheckedInToday) "Checked in today — deadline alert disarmed!" else "Triggers 1 hour before daily deadline (${habit.deadlineHoursRemaining}h remaining)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Weekly History Grid (M T W T F S S)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WEEKLY CONSISTENCY GRID",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val accentColor = MaterialTheme.colorScheme.primary
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEachIndexed { idx, dayLabel ->
                                val entry = historyEntries.find { it.dayIndex == idx }
                                val isDone = entry?.isCompleted ?: false
                                val isToday = idx == 4

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isToday) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isDone -> accentColor
                                                    isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                }
                                            )
                                            .border(
                                                width = if (isToday) 2.dp else 0.dp,
                                                color = if (isToday) accentColor else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Done",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                text = if (idx > 4) "•" else "—",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Peer Reactions Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CIRCLE MOTIVATION & REACTIONS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Reminder button
                            OutlinedButton(
                                onClick = {
                                    val pendingMember = squadMembers.firstOrNull { !it.isUser && !it.hasCheckedInToday }
                                    val target = pendingMember?.name ?: "Squad"
                                    viewModel.sendReaction(habit.id, target, "NUDGE")
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reaction_nudge_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ElectricBolt, contentDescription = "Friendly Reminder", tint = StreakOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remind", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Cheer button
                            OutlinedButton(
                                onClick = {
                                    val doneMember = squadMembers.firstOrNull { !it.isUser && it.hasCheckedInToday }
                                    val target = doneMember?.name ?: "Squad"
                                    viewModel.sendReaction(habit.id, target, "CHEER")
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reaction_cheer_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = "Cheer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cheer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // High Five button
                            OutlinedButton(
                                onClick = {
                                    val member = squadMembers.firstOrNull { !it.isUser }
                                    val target = member?.name ?: "Squad"
                                    viewModel.sendReaction(habit.id, target, "HIGH_FIVE")
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reaction_highfive_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Handshake, contentDescription = "High Five", tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("High Five", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Circle Host Management Console
            if (isUserHost) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("host_management_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CIRCLE HOST MANAGEMENT",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = "Host Tools",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You are the creator of '${habit.squadName}'. You can manage circle members, send reminders, or share invite links.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { showInviteDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("host_add_member_btn")
                                ) {
                                    Icon(Icons.Outlined.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Member", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        squadMembers.filter { !it.isUser && !it.hasCheckedInToday }.forEach {
                                            viewModel.sendReaction(habit.id, it.name, "NUDGE")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("host_nudge_all_btn")
                                ) {
                                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = StreakOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remind All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Squad Status list
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CIRCLE MEMBERS STATUS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        onClick = { showInviteDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("squad_invite_friend_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Outlined.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Invite / Share Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(squadMembers, key = { it.id }) { member ->
                val accentColor = MaterialTheme.colorScheme.primary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = member.avatarUrl,
                                contentDescription = member.name,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (member.hasCheckedInToday) accentColor else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (member.isUser) "${member.name} (You)" else member.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (member.isHost) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7),
                                            contentColor = Color(0xFFD97706)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Host",
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Host",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = if (member.hasCheckedInToday) "Checked in ${member.checkInTime ?: ""}" else "Waiting for check-in...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (member.hasCheckedInToday) accentColor else StreakOrange
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (member.hasCheckedInToday) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = accentColor.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Checked In", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accentColor)
                                    }
                                }
                            } else if (!member.isUser) {
                                Button(
                                    onClick = { viewModel.sendReaction(habit.id, member.name, "NUDGE") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StreakOrange.copy(alpha = 0.15f),
                                        contentColor = StreakOrange
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("nudge_member_${member.id}")
                                ) {
                                    Text("Remind ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Host remove action
                            if (isUserHost && !member.isUser) {
                                IconButton(
                                    onClick = { memberToRemove = member },
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(32.dp)
                                        .testTag("remove_member_${member.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Remove Member",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Leave Circle Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_leave_squad_btn")
                ) {
                    Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Leave Habit Circle", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteFriendDialog(
            habitId = habit.id,
            habitName = habit.name,
            squadName = habit.squadName,
            viewModel = viewModel,
            onDismiss = { showInviteDialog = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                    text = "Leave '${habit.squadName}'?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to leave '${habit.name}'? Your active streak of ${habit.currentStreak} days will be reset, and you will no longer receive circle updates.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteHabit(habit.id) {
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_leave_squad_btn")
                ) {
                    Text("Leave Circle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_leave_squad_btn")
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

    if (memberToRemove != null) {
        val targetMember = memberToRemove!!
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
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
                    text = "Remove '${targetMember.name}'?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "As circle host, are you sure you want to remove ${targetMember.name} from '${habit.squadName}'?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mId = targetMember.id
                        val mName = targetMember.name
                        memberToRemove = null
                        viewModel.removeSquadMember(mId, mName, habit.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_remove_member_btn")
                ) {
                    Text("Remove Member", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { memberToRemove = null },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_remove_member_btn")
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

    if (showProofDialog) {
        AlertDialog(
            onDismissRequest = { showProofDialog = false },
            title = {
                Text(
                    text = "Check-in with Proof & Note",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add a photo proof URL or reflection note for your circle to see.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = proofNote,
                        onValueChange = { proofNote = it },
                        label = { Text("Reflection Note / Comment") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = photoProofUrl,
                        onValueChange = { photoProofUrl = it },
                        label = { Text("Photo Proof URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val note = proofNote.ifBlank { "Completed successfully today! ✅" }
                        val url = photoProofUrl.ifBlank { "https://images.unsplash.com/photo-1517838277536-f5f99be501cd" }
                        showProofDialog = false
                        viewModel.checkInHabitWithProof(habit.id, note, url)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_submit_proof_btn")
                ) {
                    Text("Submit Check-In", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showProofDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_proof_btn")
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
