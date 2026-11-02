package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val emoji: String,
    val currentStreak: Int,
    val bestStreak: Int,
    val isCheckedInToday: Boolean,
    val lastCheckInTime: String? = null,
    val deadlineHoursRemaining: Int = 12,
    val isAtRisk: Boolean = false,
    val squadName: String,
    val frequencyGoal: String = "Daily",
    val weeklyTargetCount: Int = 7,
    val weeklyCompletedCount: Int = 5,
    val streakFreezeUsed: Boolean = false,
    val scheduleType: String = "Daily"
)

@Entity(tableName = "squad_members")
data class SquadMemberEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val name: String,
    val avatarUrl: String,
    val isUser: Boolean = false,
    val isHost: Boolean = false,
    val hasCheckedInToday: Boolean,
    val checkInTime: String? = null,
    val currentStreak: Int
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val habitName: String,
    val userName: String,
    val userAvatar: String,
    val actionType: String, // "CHECK_IN", "NUDGE", "CHEER", "HIGH_FIVE", "CREATE", "STREAK_FREEZE"
    val message: String,
    val timestamp: Long,
    val formattedTime: String,
    val note: String? = null,
    val photoProofUrl: String? = null
)

@Entity(tableName = "weekly_history")
data class WeeklyHistoryEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val dayLabel: String, // "M", "T", "W", "T", "F", "S", "S"
    val dayIndex: Int, // 0 to 6
    val isCompleted: Boolean
)
