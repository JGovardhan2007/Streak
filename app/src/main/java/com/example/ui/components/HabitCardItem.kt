package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.Delete
import com.example.data.local.HabitEntity
import com.example.data.local.SquadMemberEntity
import com.example.ui.theme.StreakOrange
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HabitCardItem(
    habit: HabitEntity,
    squadMembers: List<SquadMemberEntity>,
    onCardClick: () -> Unit,
    onCheckInClick: () -> Unit,
    onNudgeClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    nudgeTimestamp: Long = 0L,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val onActiveColor = MaterialTheme.colorScheme.onPrimary
    val buttonBgColor by animateColorAsState(
        targetValue = if (habit.isCheckedInToday) activeColor else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "CheckInBgAnimation"
    )

    // Micro-animation states for nudge jiggle / shake
    val shakeTranslationX = remember { Animatable(0f) }
    val shakeRotationZ = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }
    var isNudgedHighlight by remember { mutableStateOf(false) }

    LaunchedEffect(nudgeTimestamp) {
        if (nudgeTimestamp > 0L) {
            isNudgedHighlight = true
            coroutineScope {
                launch {
                    pulseScale.animateTo(1.03f, tween(90))
                    pulseScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    val jiggleX = listOf(-16f, 16f, -12f, 12f, -8f, 8f, -4f, 4f, 0f)
                    for (offset in jiggleX) {
                        shakeTranslationX.animateTo(
                            targetValue = offset,
                            animationSpec = tween(durationMillis = 35, easing = LinearEasing)
                        )
                    }
                }
                launch {
                    val jiggleAngles = listOf(-2.5f, 2.5f, -1.8f, 1.8f, -1.0f, 1.0f, -0.5f, 0.5f, 0f)
                    for (angle in jiggleAngles) {
                        shakeRotationZ.animateTo(
                            targetValue = angle,
                            animationSpec = tween(durationMillis = 35, easing = LinearEasing)
                        )
                    }
                }
            }
            delay(3000)
            isNudgedHighlight = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer {
                translationX = shakeTranslationX.value
                rotationZ = shakeRotationZ.value
                scaleX = pulseScale.value
                scaleY = pulseScale.value
            }
            .clickable { onCardClick() }
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(22.dp),
        border = if (isNudgedHighlight) BorderStroke(2.dp, StreakOrange) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isNudgedHighlight) 8.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Nudge active banner tag
            AnimatedVisibility(
                visible = isNudgedHighlight,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StreakOrange.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, StreakOrange.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Reminder Alert",
                            tint = StreakOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Squad Reminder Received • Time to check in!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StreakOrange
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = habit.emoji, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = habit.squadName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${habit.frequencyGoal} (${habit.weeklyCompletedCount}/${habit.weeklyTargetCount})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlameStreakBadge(streakCount = habit.currentStreak)
                    if (onDeleteClick != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("delete_habit_card_btn_${habit.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Leave Circle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Squad members avatar stack
                Column {
                    Text(
                        text = "SQUAD STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SquadAvatarStack(members = squadMembers)
                }

                // Actions: Quick Check-in button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onCheckInClick,
                        enabled = !habit.isCheckedInToday,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBgColor,
                            contentColor = if (habit.isCheckedInToday) onActiveColor else MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = activeColor,
                            disabledContentColor = onActiveColor
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("check_in_btn_${habit.id}")
                    ) {
                        if (habit.isCheckedInToday) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Done", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Text("Mark Done", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
