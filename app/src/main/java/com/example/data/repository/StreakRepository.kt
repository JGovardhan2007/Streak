package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class HabitDetailData(
    val habit: HabitEntity,
    val squadMembers: List<SquadMemberEntity>,
    val history: List<WeeklyHistoryEntity>
)

data class LeaderboardUser(
    val name: String,
    val avatarUrl: String,
    val isUser: Boolean,
    val totalStreak: Int,
    val habitsCompleted: Int,
    val totalHabits: Int,
    val consistencyPercentage: Int
)

class StreakRepository(private val dao: StreakDao) {

    val habits: Flow<List<HabitEntity>> = dao.getAllHabits()
    val activityLogs: Flow<List<ActivityLogEntity>> = dao.getActivityLogs()
    val allSquadMembers: Flow<List<SquadMemberEntity>> = dao.getAllSquadMembers()
    val allWeeklyHistory: Flow<List<WeeklyHistoryEntity>> = dao.getAllWeeklyHistory()

    fun getHabitById(id: String): Flow<HabitEntity?> = dao.getHabitById(id)

    fun getSquadMembersForHabit(habitId: String): Flow<List<SquadMemberEntity>> =
        dao.getSquadMembersForHabit(habitId)

    fun getWeeklyHistoryForHabit(habitId: String): Flow<List<WeeklyHistoryEntity>> =
        dao.getWeeklyHistoryForHabit(habitId)

    suspend fun markHabitDone(habitId: String) {
        val existingHabit = dao.getHabitByIdDirect(habitId) ?: return
        if (existingHabit.isCheckedInToday) return

        val newStreak = existingHabit.currentStreak + 1
        val updatedHabit = existingHabit.copy(
            isCheckedInToday = true,
            currentStreak = newStreak,
            bestStreak = maxOf(existingHabit.bestStreak, newStreak),
            lastCheckInTime = "Just now",
            isAtRisk = false,
            weeklyCompletedCount = existingHabit.weeklyCompletedCount + 1
        )
        dao.updateHabit(updatedHabit)

        val userMember = SquadMemberEntity(
            id = "user_$habitId",
            habitId = habitId,
            name = "You",
            avatarUrl = "https://i.pravatar.cc/150?u=you",
            isUser = true,
            hasCheckedInToday = true,
            checkInTime = "Just now",
            currentStreak = newStreak
        )
        dao.updateSquadMember(userMember)

        val historyEntry = WeeklyHistoryEntity(
            id = "history_${habitId}_4",
            habitId = habitId,
            dayLabel = "F",
            dayIndex = 4,
            isCompleted = true
        )
        dao.updateWeeklyHistory(historyEntry)

        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = existingHabit.name,
            userName = "You",
            userAvatar = "https://i.pravatar.cc/150?u=you",
            actionType = "CHECK_IN",
            message = "You completed ${existingHabit.name}! Streak is now 🔥 $newStreak Days!",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now"
        )
        dao.insertActivityLog(log)
    }

    suspend fun undoCheckIn(habitId: String) {
        val existingHabit = dao.getHabitByIdDirect(habitId) ?: return
        if (!existingHabit.isCheckedInToday) return

        val restoredStreak = maxOf(0, existingHabit.currentStreak - 1)
        val updatedHabit = existingHabit.copy(
            isCheckedInToday = false,
            currentStreak = restoredStreak,
            lastCheckInTime = "Not checked in",
            weeklyCompletedCount = maxOf(0, existingHabit.weeklyCompletedCount - 1)
        )
        dao.updateHabit(updatedHabit)

        val userMember = SquadMemberEntity(
            id = "user_$habitId",
            habitId = habitId,
            name = "You",
            avatarUrl = "https://i.pravatar.cc/150?u=you",
            isUser = true,
            hasCheckedInToday = false,
            checkInTime = "Pending",
            currentStreak = restoredStreak
        )
        dao.updateSquadMember(userMember)

        val historyEntry = WeeklyHistoryEntity(
            id = "history_${habitId}_4",
            habitId = habitId,
            dayLabel = "F",
            dayIndex = 4,
            isCompleted = false
        )
        dao.updateWeeklyHistory(historyEntry)

        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = existingHabit.name,
            userName = "You",
            userAvatar = "https://i.pravatar.cc/150?u=you",
            actionType = "UNDO",
            message = "You undid check-in for ${existingHabit.name}.",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now"
        )
        dao.insertActivityLog(log)
    }

    suspend fun checkInHabitWithProof(habitId: String, note: String?, photoProofUrl: String?) {
        val existingHabit = dao.getHabitByIdDirect(habitId) ?: return
        if (existingHabit.isCheckedInToday) return

        val newStreak = existingHabit.currentStreak + 1
        val updatedHabit = existingHabit.copy(
            isCheckedInToday = true,
            currentStreak = newStreak,
            bestStreak = maxOf(existingHabit.bestStreak, newStreak),
            lastCheckInTime = "Just now with proof",
            isAtRisk = false,
            weeklyCompletedCount = existingHabit.weeklyCompletedCount + 1
        )
        dao.updateHabit(updatedHabit)

        val userMember = SquadMemberEntity(
            id = "user_$habitId",
            habitId = habitId,
            name = "You",
            avatarUrl = "https://i.pravatar.cc/150?u=you",
            isUser = true,
            hasCheckedInToday = true,
            checkInTime = "Just now",
            currentStreak = newStreak
        )
        dao.updateSquadMember(userMember)

        val historyEntry = WeeklyHistoryEntity(
            id = "history_${habitId}_4",
            habitId = habitId,
            dayLabel = "F",
            dayIndex = 4,
            isCompleted = true
        )
        dao.updateWeeklyHistory(historyEntry)

        val proofSummary = if (!note.isNullOrBlank()) " Note: $note" else ""
        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = existingHabit.name,
            userName = "You",
            userAvatar = "https://i.pravatar.cc/150?u=you",
            actionType = "CHECK_IN_PROOF",
            message = "You completed ${existingHabit.name} with proof! Streak 🔥 $newStreak Days.$proofSummary",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now",
            note = note,
            photoProofUrl = photoProofUrl
        )
        dao.insertActivityLog(log)
    }

    suspend fun sendReaction(
        habitId: String,
        targetMemberName: String,
        actionType: String // "NUDGE", "CHEER", "HIGH_FIVE"
    ) {
        val habit = dao.getHabitByIdDirect(habitId) ?: return

        val actionMsg = when (actionType) {
            "NUDGE" -> "You sent a friendly reminder to $targetMemberName to check in for ${habit.name}! ⚡"
            "CHEER" -> "You cheered $targetMemberName on ${habit.name}! 🎉"
            else -> "You sent a High Five to $targetMemberName! 🙌"
        }

        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = habit.name,
            userName = "You",
            userAvatar = "https://i.pravatar.cc/150?u=you",
            actionType = actionType,
            message = actionMsg,
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now"
        )
        dao.insertActivityLog(log)
    }

    suspend fun createHabit(
        name: String,
        category: String,
        emoji: String,
        squadName: String,
        invitedFriends: List<String>,
        frequencyGoal: String = "Daily"
    ) {
        val habitId = "habit_${UUID.randomUUID().toString().take(8)}"
        val targetCount = when {
            frequencyGoal.contains("2") -> 2
            frequencyGoal.contains("3") -> 3
            frequencyGoal.contains("4") -> 4
            frequencyGoal.contains("5") -> 5
            frequencyGoal.contains("6") -> 6
            else -> 7
        }

        val newHabit = HabitEntity(
            id = habitId,
            name = name,
            category = category,
            emoji = emoji.ifEmpty { "🔥" },
            currentStreak = 1,
            bestStreak = 1,
            isCheckedInToday = true,
            lastCheckInTime = "Just now",
            deadlineHoursRemaining = 12,
            isAtRisk = false,
            squadName = squadName.ifEmpty { "General Circle" },
            frequencyGoal = frequencyGoal,
            weeklyTargetCount = targetCount,
            weeklyCompletedCount = 1
        )

        dao.insertHabit(newHabit)

        // User member
        val members = mutableListOf(
            SquadMemberEntity(
                id = "user_$habitId",
                habitId = habitId,
                name = "You",
                avatarUrl = "https://i.pravatar.cc/150?u=you",
                isUser = true,
                isHost = true,
                hasCheckedInToday = true,
                checkInTime = "Just now",
                currentStreak = 1
            )
        )

        invitedFriends.forEachIndexed { idx, friendName ->
            members.add(
                SquadMemberEntity(
                    id = "member_${habitId}_$idx",
                    habitId = habitId,
                    name = friendName,
                    avatarUrl = "https://i.pravatar.cc/150?u=${friendName.lowercase().replace(" ", "")}",
                    isUser = false,
                    hasCheckedInToday = false,
                    checkInTime = null,
                    currentStreak = 0
                )
            )
        }

        dao.insertSquadMembers(members)

        // Default weekly history
        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        val history = days.mapIndexed { index, dayLabel ->
            WeeklyHistoryEntity(
                id = "history_${habitId}_$index",
                habitId = habitId,
                dayLabel = dayLabel,
                dayIndex = index,
                isCompleted = index == 4 // Today (Friday)
            )
        }
        dao.insertWeeklyHistory(history)

        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = name,
            userName = "You",
            userAvatar = "https://i.pravatar.cc/150?u=you",
            actionType = "CREATE",
            message = "You formed a new circle '$squadName' for $emoji $name!",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now"
        )
        dao.insertActivityLog(log)
    }

    fun generateInviteLink(habitId: String, habitName: String, squadName: String): String {
        val sanitizeHabit = java.net.URLEncoder.encode(habitName, "UTF-8")
        val sanitizeSquad = java.net.URLEncoder.encode(squadName, "UTF-8")
        val codePrefix = habitName.filter { it.isLetterOrDigit() }.take(3).uppercase().ifEmpty { "CIR" }
        val codeSuffix = habitId.takeLast(4).uppercase().ifEmpty { "7892" }
        val code = "$codePrefix-$codeSuffix"
        return "https://streak.app/join?code=$code&habit=$sanitizeHabit&squad=$sanitizeSquad&habitId=$habitId"
    }

    suspend fun addFriendToCircle(habitId: String, friendName: String): SquadMemberEntity {
        val habit = dao.getHabitByIdDirect(habitId)
        val habitName = habit?.name ?: "Habit Circle"
        val squadName = habit?.squadName ?: "Squad"

        val cleanName = friendName.trim().ifEmpty { "New Friend" }
        val memberId = "member_${habitId}_${UUID.randomUUID().toString().take(6)}"
        val avatarUrl = "https://i.pravatar.cc/150?u=${cleanName.lowercase().replace(" ", "")}"

        val newMember = SquadMemberEntity(
            id = memberId,
            habitId = habitId,
            name = cleanName,
            avatarUrl = avatarUrl,
            isUser = false,
            hasCheckedInToday = false,
            checkInTime = null,
            currentStreak = 1
        )

        dao.insertSquadMembers(listOf(newMember))

        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = habitName,
            userName = cleanName,
            userAvatar = avatarUrl,
            actionType = "JOIN",
            message = "$cleanName joined the '$squadName' circle via invite link.",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now"
        )
        dao.insertActivityLog(log)

        return newMember
    }

    suspend fun joinCircleFromInviteLinkOrCode(linkOrCode: String, userName: String = "You"): HabitEntity {
        // Parse parameters from link or code
        var habitName = "Shared Habit Circle"
        var squadName = "Squad Circle"
        var habitId = ""

        if (linkOrCode.contains("habit=")) {
            try {
                val uri = android.net.Uri.parse(linkOrCode)
                habitName = uri.getQueryParameter("habit")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: habitName
                squadName = uri.getQueryParameter("squad")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: squadName
                habitId = uri.getQueryParameter("habitId") ?: ""
            } catch (e: Exception) {
                // Fallback parsing
            }
        } else if (linkOrCode.isNotBlank()) {
            // Code format, e.g. GYM-7892
            val cleanCode = linkOrCode.trim().uppercase()
            squadName = "$cleanCode Circle"
            habitName = when {
                cleanCode.contains("GYM") -> "Morning Gym"
                cleanCode.contains("WALK") -> "Evening Walk"
                cleanCode.contains("READ") -> "Daily Reading"
                cleanCode.contains("WAT") || cleanCode.contains("HYD") -> "Hydration Target"
                cleanCode.contains("COD") -> "Daily Coding"
                else -> "Shared $cleanCode Habit"
            }
        }

        // Check if habit already exists by ID or by name
        val allHabits = dao.getAllHabitsDirect()
        val existingHabit = if (habitId.isNotBlank()) {
            allHabits.find { it.id == habitId }
        } else {
            allHabits.find { it.name.equals(habitName, ignoreCase = true) }
        }

        if (existingHabit != null) {
            // Circle exists! Make sure user or friend is added to it
            val squad = dao.getSquadMembersForHabitDirect(existingHabit.id)
            val userInSquad = squad.find { it.isUser }
            if (userInSquad == null) {
                val userMember = SquadMemberEntity(
                    id = "user_${existingHabit.id}",
                    habitId = existingHabit.id,
                    name = userName,
                    avatarUrl = "https://i.pravatar.cc/150?u=you",
                    isUser = true,
                    hasCheckedInToday = false,
                    checkInTime = null,
                    currentStreak = 1
                )
                dao.insertSquadMembers(listOf(userMember))
            }

            val log = ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = existingHabit.id,
                habitName = existingHabit.name,
                userName = userName,
                userAvatar = "https://i.pravatar.cc/150?u=you",
                actionType = "JOIN",
                message = "$userName joined the '${existingHabit.squadName}' circle via invite link.",
                timestamp = System.currentTimeMillis(),
                formattedTime = "Just now"
            )
            dao.insertActivityLog(log)

            return existingHabit
        } else {
            // Create a new Habit circle for the user joining via link
            val newHabitId = habitId.ifEmpty { "habit_joined_${UUID.randomUUID().toString().take(6)}" }
            val emoji = when {
                habitName.contains("Gym", ignoreCase = true) -> "🏋️"
                habitName.contains("Walk", ignoreCase = true) -> "🏃"
                habitName.contains("Read", ignoreCase = true) -> "📚"
                habitName.contains("Water", ignoreCase = true) || habitName.contains("Hydrate", ignoreCase = true) -> "💧"
                habitName.contains("Code", ignoreCase = true) -> "💻"
                else -> "🔥"
            }

            val newHabit = HabitEntity(
                id = newHabitId,
                name = habitName,
                category = "Health & Fitness",
                emoji = emoji,
                currentStreak = 1,
                bestStreak = 1,
                isCheckedInToday = false,
                lastCheckInTime = null,
                deadlineHoursRemaining = 10,
                isAtRisk = false,
                squadName = squadName,
                frequencyGoal = "Daily",
                weeklyTargetCount = 7,
                weeklyCompletedCount = 0
            )

            dao.insertHabit(newHabit)

            val members = listOf(
                SquadMemberEntity(
                    id = "user_$newHabitId",
                    habitId = newHabitId,
                    name = "You",
                    avatarUrl = "https://i.pravatar.cc/150?u=you",
                    isUser = true,
                    hasCheckedInToday = false,
                    checkInTime = null,
                    currentStreak = 1
                ),
                SquadMemberEntity(
                    id = "inviter_$newHabitId",
                    habitId = newHabitId,
                    name = "Circle Host",
                    avatarUrl = "https://i.pravatar.cc/150?u=host",
                    isUser = false,
                    hasCheckedInToday = true,
                    checkInTime = "8:00 AM",
                    currentStreak = 12
                )
            )

            dao.insertSquadMembers(members)

            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            val history = days.mapIndexed { index, dayLabel ->
                WeeklyHistoryEntity(
                    id = "history_${newHabitId}_$index",
                    habitId = newHabitId,
                    dayLabel = dayLabel,
                    dayIndex = index,
                    isCompleted = false
                )
            }
            dao.insertWeeklyHistory(history)

            val log = ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = newHabitId,
                habitName = habitName,
                userName = "You",
                userAvatar = "https://i.pravatar.cc/150?u=you",
                actionType = "JOIN",
                message = "You joined '$squadName' ($emoji $habitName) using invite link! 🎉",
                timestamp = System.currentTimeMillis(),
                formattedTime = "Just now"
            )
            dao.insertActivityLog(log)

            return newHabit
        }
    }

    suspend fun removeSquadMember(memberId: String, memberName: String, habitId: String) {
        val habit = dao.getHabitByIdDirect(habitId)
        val habitName = habit?.name ?: "Habit Circle"
        dao.deleteSquadMember(memberId)
        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = habitName,
            userName = "Host (You)",
            userAvatar = "https://i.pravatar.cc/150?u=you",
            actionType = "REMOVE_MEMBER",
            message = "Circle Host removed '$memberName' from the circle.",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now"
        )
        dao.insertActivityLog(log)
    }

    suspend fun deleteHabit(habitId: String) {
        val habit = dao.getHabitByIdDirect(habitId)
        val habitName = habit?.name ?: "Habit Circle"
        dao.deleteHabit(habitId)
        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            habitName = habitName,
            userName = "You",
            userAvatar = "https://i.pravatar.cc/150?u=you",
            actionType = "LEAVE",
            message = "You left '$habitName' circle.",
            timestamp = System.currentTimeMillis(),
            formattedTime = "Just now"
        )
        dao.insertActivityLog(log)
    }
}
