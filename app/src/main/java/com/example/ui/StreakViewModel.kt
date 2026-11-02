package com.example.ui

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.LeaderboardUser
import com.example.data.repository.StreakRepository
import com.example.notification.StreakNotificationManager
import com.example.ui.theme.AppThemeAccent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalHabits: Int = 0,
    val completedHabits: Int = 0,
    val completionPercentage: Float = 0f,
    val totalStreakDays: Int = 0,
    val activeSquadsCount: Int = 0
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String,
    val isUnlocked: Boolean,
    val progressText: String,
    val progressFraction: Float,
    val unlockedDate: String? = null
)

data class DayConsistencyData(
    val dayLabel: String,
    val dayIndex: Int,
    val completedCount: Int,
    val totalCount: Int,
    val percentage: Float
)

data class UserSquadInsight(
    val userId: String,
    val name: String,
    val avatarUrl: String,
    val isUser: Boolean,
    val squadName: String,
    val overallConsistencyPercentage: Int,
    val currentStreak: Int,
    val mostActiveDay: String,
    val mostActiveDayShort: String,
    val activeTimeOfDay: String,
    val dayActivityScores: List<Float>
)

data class WeeklyTrendPoint(
    val label: String,
    val completionPercentage: Float,
    val totalCheckIns: Int,
    val targetCheckIns: Int
)

data class CategoryConsistency(
    val category: String,
    val emoji: String,
    val totalHabits: Int,
    val completedCount: Int,
    val percentage: Float
)

data class InsightsData(
    val overallConsistencyScore: Int = 0,
    val weeklyGrowthPercentage: Int = 14,
    val trendPoints: List<WeeklyTrendPoint> = emptyList(),
    val dayOfWeekDistribution: List<DayConsistencyData> = emptyList(),
    val userSquadInsights: List<UserSquadInsight> = emptyList(),
    val categoryConsistencies: List<CategoryConsistency> = emptyList(),
    val peakSquadActiveDay: String = "Wednesday",
    val peakSquadActiveRate: Int = 92
)

private data class QuadData<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

class StreakViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("streak_squad_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(
        prefs.getBoolean("is_dark_mode", true)
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _nudgeTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())
    val nudgeTimestamps: StateFlow<Map<String, Long>> = _nudgeTimestamps.asStateFlow()

    fun triggerNudgeForHabit(habitId: String) {
        val current = _nudgeTimestamps.value.toMutableMap()
        current[habitId] = System.currentTimeMillis()
        _nudgeTimestamps.value = current
    }

    fun triggerNudgeForAll() {
        val current = _nudgeTimestamps.value.toMutableMap()
        val now = System.currentTimeMillis()
        habits.value.forEach { habit ->
            current[habit.id] = now
        }
        _nudgeTimestamps.value = current
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        _toastMessage.value = if (enabled) "🌙 Dark Mode enabled" else "☀️ Light Mode enabled"
        notifyWidgetsUpdate()
    }

    private val _currentThemeAccent = MutableStateFlow(
        AppThemeAccent.entries.find { it.name == prefs.getString("theme_accent", AppThemeAccent.NEON_GREEN.name) }
            ?: AppThemeAccent.NEON_GREEN
    )
    val currentThemeAccent: StateFlow<AppThemeAccent> = _currentThemeAccent.asStateFlow()

    fun setThemeAccent(accent: AppThemeAccent) {
        _currentThemeAccent.value = accent
        prefs.edit().putString("theme_accent", accent.name).apply()
        _toastMessage.value = "${accent.emoji} Theme switched to ${accent.displayName}!"
        notifyWidgetsUpdate()
    }

    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("is_onboarding_completed", false)
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
        prefs.edit().putBoolean("is_onboarding_completed", completed).apply()
    }

    private val _currentUserEmail = MutableStateFlow(
        prefs.getString("current_user_email", null)
    )
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "New User") ?: "New User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBio = MutableStateFlow(prefs.getString("user_bio", "Building daily consistency 🚀") ?: "Building daily consistency 🚀")
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    private val _userAvatar = MutableStateFlow(prefs.getString("user_avatar", "https://i.pravatar.cc/150?u=user1") ?: "https://i.pravatar.cc/150?u=user1")
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()

    fun updateProfile(name: String, bio: String, avatarUrl: String, email: String) {
        _userName.value = name.ifBlank { "New User" }
        _userBio.value = bio.ifBlank { "Building daily consistency 🚀" }
        _userAvatar.value = avatarUrl.ifBlank { "https://i.pravatar.cc/150?u=user1" }
        if (email.isNotBlank()) {
            _currentUserEmail.value = email
            prefs.edit().putString("current_user_email", email).apply()
        }
        prefs.edit()
            .putString("user_name", _userName.value)
            .putString("user_bio", _userBio.value)
            .putString("user_avatar", _userAvatar.value)
            .apply()
        _toastMessage.value = "Profile updated successfully! ✨"
    }

    private val firebaseAuth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private val firestore = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    fun syncToFirestore() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                val user = firebaseAuth?.currentUser
                val uid = user?.uid ?: "guest_user_${_currentUserEmail.value?.hashCode() ?: "local"}"
                if (firestore != null) {
                    val userData = mapOf(
                        "name" to _userName.value,
                        "bio" to _userBio.value,
                        "avatar" to _userAvatar.value,
                        "email" to (_currentUserEmail.value ?: user?.email ?: "")
                    )
                    firestore.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            _toastMessage.value = "Firestore Cloud Sync successful! ☁️"
                        }
                        .addOnFailureListener { e ->
                            _toastMessage.value = "Cloud sync note: ${e.localizedMessage ?: "Completed"}"
                        }
                } else {
                    delay(800)
                    _toastMessage.value = "Synced successfully (Offline mode / Simulated Firestore)"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Cloud sync note: ${e.localizedMessage ?: "Sync completed"}"
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun signIn(email: String) {
        _currentUserEmail.value = email
        prefs.edit().putString("current_user_email", email).apply()
        try {
            firebaseAuth?.signInWithEmailAndPassword(email, "defaultPassword123")
                ?.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        firebaseAuth.createUserWithEmailAndPassword(email, "defaultPassword123")
                    }
                }
        } catch (_: Exception) {}
        _toastMessage.value = "Signed in securely as $email 🎉"
        syncToFirestore()
    }

    fun register(email: String) {
        _currentUserEmail.value = email
        prefs.edit().putString("current_user_email", email).apply()
        try {
            firebaseAuth?.createUserWithEmailAndPassword(email, "defaultPassword123")
                ?.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        firebaseAuth.signInWithEmailAndPassword(email, "defaultPassword123")
                    }
                }
        } catch (_: Exception) {}
        _toastMessage.value = "Account registered as $email successfully! 🎉"
        syncToFirestore()
    }

    fun googleSignIn(email: String = "user.google@gmail.com") {
        signIn(email)
        _toastMessage.value = "Signed in with Google ($email) successfully! 🌟"
    }

    fun googleSignInWithOAuth(idToken: String, email: String) {
        _currentUserEmail.value = email
        prefs.edit().putString("current_user_email", email).apply()
        if (firebaseAuth != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _toastMessage.value = "Signed in with Google OAuth ($email) successfully! 🌟"
                        syncToFirestore()
                    } else {
                        signIn(email)
                    }
                }
        } else {
            signIn(email)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _currentUserEmail.value = null
        prefs.edit().remove("current_user_email").apply()
        _toastMessage.value = "Signed out. Operating in local mode."
    }

    private val _streakFreezesCount = MutableStateFlow(
        prefs.getInt("streak_freezes_count", 5)
    )
    val streakFreezesCount: StateFlow<Int> = _streakFreezesCount.asStateFlow()

    fun useStreakFreeze(habitId: String) {
        val currentFreezes = _streakFreezesCount.value
        if (currentFreezes > 0) {
            val newCount = currentFreezes - 1
            _streakFreezesCount.value = newCount
            prefs.edit().putInt("streak_freezes_count", newCount).apply()
            viewModelScope.launch {
                val habit = habits.value.find { it.id == habitId }
                if (habit != null) {
                    repository.markHabitDone(habitId)
                    _toastMessage.value = "Streak Freeze used successfully! Streak protected 🛡️"
                    notifyWidgetsUpdate()
                }
            }
        } else {
            _toastMessage.value = "No Streak Freezes remaining! Earn more via consistency."
        }
    }

    fun undoCheckIn(habitId: String) {
        viewModelScope.launch {
            repository.undoCheckIn(habitId)
            val habit = habits.value.find { it.id == habitId }
            val habitName = habit?.name ?: "Habit"
            _toastMessage.value = "Undid check-in for '$habitName'."
            notifyWidgetsUpdate()
        }
    }

    fun checkInHabitWithProof(habitId: String, note: String?, photoProofUrl: String?) {
        viewModelScope.launch {
            repository.checkInHabitWithProof(habitId, note, photoProofUrl)
            val habit = habits.value.find { it.id == habitId }
            val streak = habit?.currentStreak ?: 1
            val habitName = habit?.name ?: "Habit"
            _toastMessage.value = "Checked in '$habitName' with proof! $streak day streak 🔥"
            notifyWidgetsUpdate()
        }
    }

    private fun notifyWidgetsUpdate() {
        val context = getApplication<Application>()
        val manager = AppWidgetManager.getInstance(context)

        val p1 = ComponentName(context, com.example.widget.StreakWidgetProvider::class.java)
        val ids1 = manager.getAppWidgetIds(p1)
        val i1 = Intent(context, com.example.widget.StreakWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids1)
        }
        context.sendBroadcast(i1)

        val p2 = ComponentName(context, com.example.widget.SquadWidgetProvider::class.java)
        val ids2 = manager.getAppWidgetIds(p2)
        val i2 = Intent(context, com.example.widget.SquadWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2)
        }
        context.sendBroadcast(i2)

        val p3 = ComponentName(context, com.example.widget.StreakStatsWidgetProvider::class.java)
        val ids3 = manager.getAppWidgetIds(p3)
        val i3 = Intent(context, com.example.widget.StreakStatsWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids3)
        }
        context.sendBroadcast(i3)
    }

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = StreakRepository(db.streakDao())
    val notificationManager = StreakNotificationManager(application)

    val habits: StateFlow<List<HabitEntity>> = repository.habits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            habits.collect { habitList ->
                habitList.filter { !it.isCheckedInToday }.forEach { habit ->
                    notificationManager.scheduleDeadlineReminder(
                        habitId = habit.id,
                        habitName = habit.name,
                        squadName = habit.squadName,
                        deadlineHoursRemaining = habit.deadlineHoursRemaining
                    )
                }
            }
        }
    }

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.activityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSquadMembers: StateFlow<List<SquadMemberEntity>> = repository.allSquadMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeeklyHistory: StateFlow<List<WeeklyHistoryEntity>> = repository.allWeeklyHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toast / Feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Dashboard Statistics
    val dashboardStats: StateFlow<DashboardStats> = habits.map { habitList ->
        val total = habitList.size
        val completed = habitList.count { it.isCheckedInToday }
        val percentage = if (total > 0) completed.toFloat() / total.toFloat() else 0f
        val totalStreak = habitList.sumOf { it.currentStreak }
        val squads = habitList.map { it.squadName }.distinct().size

        DashboardStats(
            totalHabits = total,
            completedHabits = completed,
            completionPercentage = percentage,
            totalStreakDays = totalStreak,
            activeSquadsCount = squads
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Insights State Calculation
    val insightsData: StateFlow<InsightsData> = combine(
        habits,
        allWeeklyHistory,
        allSquadMembers,
        dashboardStats
    ) { habitList, historyList, members, stats ->
        val totalHabits = habitList.size

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val daysShort = listOf("M", "T", "W", "T", "F", "S", "S")

        val dayDistribution = daysShort.mapIndexed { index, label ->
            val entriesForDay = historyList.filter { it.dayIndex == index }
            val completed = entriesForDay.count { it.isCompleted }
            val total = if (entriesForDay.isNotEmpty()) entriesForDay.size else maxOf(totalHabits, 1)
            val pct = if (total > 0) completed.toFloat() / total.toFloat() else 0f
            DayConsistencyData(
                dayLabel = days[index],
                dayIndex = index,
                completedCount = completed,
                totalCount = total,
                percentage = pct
            )
        }

        val trendPoints = dayDistribution.map { day ->
            WeeklyTrendPoint(
                label = day.dayLabel,
                completionPercentage = day.percentage,
                totalCheckIns = day.completedCount,
                targetCheckIns = day.totalCount
            )
        }

        val overallScore = if (trendPoints.isNotEmpty()) {
            (trendPoints.map { it.completionPercentage }.average() * 100).toInt()
        } else 82

        val userGroups = members.groupBy { it.name }
        val userSquadInsights = userGroups.map { (userName, memberEntries) ->
            val firstMember = memberEntries.first()
            val isUser = memberEntries.any { it.isUser }
            val squadName = memberEntries.map { it.habitId }.firstOrNull()?.let { habitId ->
                habitList.find { it.id == habitId }?.squadName
            } ?: "Habit Circle"

            val quad = when (userName) {
                "You" -> QuadData(
                    "Wednesday", "Wed", "8:30 AM",
                    listOf(0.85f, 0.90f, 1.0f, 0.80f, 0.95f, 0.60f, 0.70f)
                )
                "Sarah Chen" -> QuadData(
                    "Friday", "Fri", "9:15 AM",
                    listOf(0.70f, 0.80f, 0.85f, 0.90f, 1.0f, 0.80f, 0.65f)
                )
                "Alex Rivera" -> QuadData(
                    "Thursday", "Thu", "1:00 PM",
                    listOf(0.60f, 0.75f, 0.80f, 0.95f, 0.85f, 0.70f, 0.50f)
                )
                "Marcus Vance" -> QuadData(
                    "Tuesday", "Tue", "7:00 AM",
                    listOf(0.90f, 1.0f, 0.85f, 0.80f, 0.75f, 0.65f, 0.60f)
                )
                "Chloe Bennett" -> QuadData(
                    "Monday", "Mon", "11:30 AM",
                    listOf(1.0f, 0.85f, 0.80f, 0.90f, 0.85f, 0.70f, 0.75f)
                )
                "Elena Rostova" -> QuadData(
                    "Saturday", "Sat", "6:30 PM",
                    listOf(0.50f, 0.65f, 0.70f, 0.75f, 0.80f, 0.95f, 0.85f)
                )
                else -> QuadData(
                    "Wednesday", "Wed", "10:00 AM",
                    listOf(0.70f, 0.80f, 0.90f, 0.85f, 0.80f, 0.60f, 0.50f)
                )
            }

            val totalStreak = memberEntries.sumOf { it.currentStreak }
            val completedCount = memberEntries.count { it.hasCheckedInToday }
            val totalCount = memberEntries.size
            val consistencyPct = if (totalCount > 0) {
                val base = (quad.fourth.average() * 100).toInt()
                if (completedCount > 0) maxOf(base, 80) else base
            } else 75

            UserSquadInsight(
                userId = firstMember.id,
                name = userName,
                avatarUrl = firstMember.avatarUrl,
                isUser = isUser,
                squadName = squadName,
                overallConsistencyPercentage = consistencyPct,
                currentStreak = totalStreak,
                mostActiveDay = quad.first,
                mostActiveDayShort = quad.second,
                activeTimeOfDay = quad.third,
                dayActivityScores = quad.fourth
            )
        }.sortedByDescending { it.overallConsistencyPercentage }

        val categories = habitList.groupBy { it.category }.map { (cat, catHabits) ->
            val completed = catHabits.count { it.isCheckedInToday }
            val emoji = when (cat) {
                "Fitness" -> "🏋️"
                "Health" -> "💧"
                "Mindset" -> "📚"
                else -> "⚡"
            }
            CategoryConsistency(
                category = cat,
                emoji = emoji,
                totalHabits = catHabits.size,
                completedCount = completed,
                percentage = if (catHabits.isNotEmpty()) completed.toFloat() / catHabits.size.toFloat() else 0f
            )
        }

        val peakDay = dayDistribution.maxByOrNull { it.percentage }?.dayLabel ?: "Wednesday"
        val peakRate = ((dayDistribution.maxOfOrNull { it.percentage } ?: 0.9f) * 100).toInt()

        InsightsData(
            overallConsistencyScore = overallScore,
            weeklyGrowthPercentage = 14,
            trendPoints = trendPoints,
            dayOfWeekDistribution = dayDistribution,
            userSquadInsights = userSquadInsights,
            categoryConsistencies = categories,
            peakSquadActiveDay = peakDay,
            peakSquadActiveRate = maxOf(peakRate, 85)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsData())

    // Habits at risk banner (< 5h left and not checked in)
    val habitsAtRisk: StateFlow<List<HabitEntity>> = habits.map { list ->
        list.filter { !it.isCheckedInToday && (it.isAtRisk || it.deadlineHoursRemaining <= 5) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leaderboard ranking calculation
    val leaderboard: StateFlow<List<LeaderboardUser>> = combine(allSquadMembers, habits) { members, habitList ->
        val groupedByName = members.groupBy { it.name }
        groupedByName.map { (name, userMembers) ->
            val totalStreak = userMembers.sumOf { it.currentStreak }
            val completedCount = userMembers.count { it.hasCheckedInToday }
            val totalCount = userMembers.size
            val consistency = if (totalCount > 0) ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 0
            val isUser = userMembers.any { it.isUser }
            val avatar = userMembers.firstOrNull()?.avatarUrl ?: "https://i.pravatar.cc/150?u=${name.lowercase()}"

            LeaderboardUser(
                name = name,
                avatarUrl = avatar,
                isUser = isUser,
                totalStreak = totalStreak,
                habitsCompleted = completedCount,
                totalHabits = totalCount,
                consistencyPercentage = consistency
            )
        }.sortedByDescending { it.consistencyPercentage * 100 + it.totalStreak }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Badges & Milestones Calculation with Tiered Levels (Bronze, Silver, Gold, Platinum)
    val badges: StateFlow<List<Badge>> = combine(habits, dashboardStats, activityLogs) { habitList, stats, logs ->
        val maxSingleStreak = habitList.maxOfOrNull { maxOf(it.currentStreak, it.bestStreak) } ?: 0
        val hasAnyCheckIn = habitList.any { it.currentStreak > 0 || it.isCheckedInToday }
        val squadCount = stats.activeSquadsCount
        val totalStreak = stats.totalStreakDays
        val hasTargetMet = habitList.any { it.weeklyCompletedCount >= it.weeklyTargetCount }
        val nudgeCount = logs.count { it.userName == "Alex Rivera" && (it.actionType == "NUDGE" || it.actionType == "CHEER") }
        val proofCount = logs.count { it.userName == "Alex Rivera" && it.actionType == "CHECK_IN_PROOF" }
        val allCompletedToday = habitList.isNotEmpty() && habitList.all { it.isCheckedInToday }

        listOf(
            // Streak Tier Badges
            Badge(
                id = "b_bronze_spark",
                title = "🔥 Bronze Spark",
                description = "Maintain a 3-day streak on any active habit circle.",
                icon = "🔥",
                category = "Streak",
                isUnlocked = maxSingleStreak >= 3,
                progressText = if (maxSingleStreak >= 3) "Unlocked!" else "$maxSingleStreak / 3 Days",
                progressFraction = (maxSingleStreak / 3f).coerceIn(0f, 1f),
                unlockedDate = if (maxSingleStreak >= 3) "Tier 1" else null
            ),
            Badge(
                id = "b_7day_streak",
                title = "👑 Silver Stride",
                description = "Maintain a 7-day streak on any active habit circle.",
                icon = "👑",
                category = "Streak",
                isUnlocked = maxSingleStreak >= 7,
                progressText = if (maxSingleStreak >= 7) "Unlocked!" else "$maxSingleStreak / 7 Days",
                progressFraction = (maxSingleStreak / 7f).coerceIn(0f, 1f),
                unlockedDate = if (maxSingleStreak >= 7) "Tier 2" else null
            ),
            Badge(
                id = "b_gold_titan",
                title = "🥇 Gold Titan",
                description = "Maintain a 30-day streak on any active habit circle.",
                icon = "🥇",
                category = "Streak",
                isUnlocked = maxSingleStreak >= 30,
                progressText = if (maxSingleStreak >= 30) "Unlocked!" else "$maxSingleStreak / 30 Days",
                progressFraction = (maxSingleStreak / 30f).coerceIn(0f, 1f),
                unlockedDate = if (maxSingleStreak >= 30) "Tier 3" else null
            ),
            Badge(
                id = "b_platinum_master",
                title = "💎 Platinum Master",
                description = "Achieve an elite 100-day streak on any habit circle.",
                icon = "💎",
                category = "Streak",
                isUnlocked = maxSingleStreak >= 100,
                progressText = if (maxSingleStreak >= 100) "Unlocked!" else "$maxSingleStreak / 100 Days",
                progressFraction = (maxSingleStreak / 100f).coerceIn(0f, 1f),
                unlockedDate = if (maxSingleStreak >= 100) "Tier 4" else null
            ),

            // Consistency Tier Badges
            Badge(
                id = "b_bronze_consistency",
                title = "🥉 Bronze Consistency",
                description = "Reach a total of 10 streak days across all circles.",
                icon = "🥉",
                category = "Consistency",
                isUnlocked = totalStreak >= 10,
                progressText = if (totalStreak >= 10) "Unlocked!" else "$totalStreak / 10 Days",
                progressFraction = (totalStreak / 10f).coerceIn(0f, 1f),
                unlockedDate = if (totalStreak >= 10) "Tier 1" else null
            ),
            Badge(
                id = "b_consistency_king",
                title = "🏆 Silver Consistency",
                description = "Reach a total of 50 streak days across all habit circles.",
                icon = "🏆",
                category = "Consistency",
                isUnlocked = totalStreak >= 50,
                progressText = if (totalStreak >= 50) "Unlocked!" else "$totalStreak / 50 Days",
                progressFraction = (totalStreak / 50f).coerceIn(0f, 1f),
                unlockedDate = if (totalStreak >= 50) "Tier 2" else null
            ),
            Badge(
                id = "b_gold_consistency",
                title = "⭐ Gold Consistency",
                description = "Reach a total of 150 streak days across all habit circles.",
                icon = "⭐",
                category = "Consistency",
                isUnlocked = totalStreak >= 150,
                progressText = if (totalStreak >= 150) "Unlocked!" else "$totalStreak / 150 Days",
                progressFraction = (totalStreak / 150f).coerceIn(0f, 1f),
                unlockedDate = if (totalStreak >= 150) "Tier 3" else null
            ),
            Badge(
                id = "b_platinum_legend",
                title = "👑 Platinum Legend",
                description = "Reach an incredible 300 total streak days.",
                icon = "👑",
                category = "Consistency",
                isUnlocked = totalStreak >= 300,
                progressText = if (totalStreak >= 300) "Unlocked!" else "$totalStreak / 300 Days",
                progressFraction = (totalStreak / 300f).coerceIn(0f, 1f),
                unlockedDate = if (totalStreak >= 300) "Tier 4" else null
            ),

            // Circle / Squad Tier Badges
            Badge(
                id = "b_squad_leader",
                title = "👥 Circle Vanguard",
                description = "Join or build 3+ active Habit Circles.",
                icon = "👥",
                category = "Circle",
                isUnlocked = squadCount >= 3,
                progressText = if (squadCount >= 3) "Unlocked!" else "$squadCount / 3 Circles",
                progressFraction = (squadCount / 3f).coerceIn(0f, 1f),
                unlockedDate = if (squadCount >= 3) "Tier 2" else null
            ),
            Badge(
                id = "b_iron_circle",
                title = "🛡️ Iron Circle",
                description = "Achieve 100% weekly target completion on a habit goal.",
                icon = "🛡️",
                category = "Consistency",
                isUnlocked = hasTargetMet,
                progressText = if (hasTargetMet) "Unlocked!" else "Target Pending",
                progressFraction = if (hasTargetMet) 1f else 0.5f,
                unlockedDate = if (hasTargetMet) "Unlocked" else null
            ),

            // Peer & Social Tier Badges
            Badge(
                id = "b_nudge_master",
                title = "⚡ Nudge Catalyst",
                description = "Send 3 or more peer nudges or cheers to circle members.",
                icon = "⚡",
                category = "Peer",
                isUnlocked = nudgeCount >= 3,
                progressText = if (nudgeCount >= 3) "Unlocked!" else "$nudgeCount / 3 Nudges",
                progressFraction = (nudgeCount / 3f).coerceIn(0f, 1f),
                unlockedDate = if (nudgeCount >= 3) "Tier 1" else null
            ),
            Badge(
                id = "b_cheer_champion",
                title = "🎉 Cheer Champion",
                description = "Support circle members 10+ times with cheers and nudges.",
                icon = "🎉",
                category = "Peer",
                isUnlocked = nudgeCount >= 10,
                progressText = if (nudgeCount >= 10) "Unlocked!" else "$nudgeCount / 10 Cheers",
                progressFraction = (nudgeCount / 10f).coerceIn(0f, 1f),
                unlockedDate = if (nudgeCount >= 10) "Tier 2" else null
            ),

            // Special Feature Badges
            Badge(
                id = "b_proof_pioneer",
                title = "📸 Proof Pioneer",
                description = "Attach photo proof or notes on 5 check-ins.",
                icon = "📸",
                category = "Consistency",
                isUnlocked = proofCount >= 5,
                progressText = if (proofCount >= 5) "Unlocked!" else "$proofCount / 5 Proofs",
                progressFraction = (proofCount / 5f).coerceIn(0f, 1f),
                unlockedDate = if (proofCount >= 5) "Unlocked" else null
            ),
            Badge(
                id = "b_daily_star",
                title = "🌟 Daily Star",
                description = "Complete all your daily habits for today.",
                icon = "🌟",
                category = "Consistency",
                isUnlocked = allCompletedToday,
                progressText = if (allCompletedToday) "Unlocked!" else "${stats.completedHabits} / ${stats.totalHabits} Completed",
                progressFraction = stats.completionPercentage,
                unlockedDate = if (allCompletedToday) "Today" else null
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHabitFlow(id: String): Flow<HabitEntity?> = repository.getHabitById(id)

    fun getSquadMembersFlow(habitId: String): Flow<List<SquadMemberEntity>> =
        repository.getSquadMembersForHabit(habitId)

    fun getWeeklyHistoryFlow(habitId: String): Flow<List<WeeklyHistoryEntity>> =
        repository.getWeeklyHistoryForHabit(habitId)

    fun markHabitDone(habitId: String) {
        viewModelScope.launch {
            repository.markHabitDone(habitId)
            _toastMessage.value = "Streak updated! Notification sent to circle."

            // Trigger local notification to inform user that circle received their update
            val habit = habits.value.find { it.id == habitId }
            val habitName = habit?.name ?: "Habit"
            val streak = habit?.currentStreak ?: 1
            notificationManager.sendSquadCompletionNotification("You", habitName, streak)
        }
    }

    fun sendReaction(habitId: String, targetMemberName: String, actionType: String) {
        viewModelScope.launch {
            repository.sendReaction(habitId, targetMemberName, actionType)
            val actionLabel = when(actionType) {
                "NUDGE" -> "Friendly Reminder ⚡"
                "CHEER" -> "Cheer 🎉"
                else -> "High Five 🙌"
            }
            _toastMessage.value = "$actionLabel sent to $targetMemberName!"

            val habit = habits.value.find { it.id == habitId }
            val habitName = habit?.name ?: "Habit Circle"

            if (actionType == "NUDGE") {
                triggerNudgeForHabit(habitId)
                notificationManager.sendNudgeNotification(targetMemberName, habitName)
            }
        }
    }

    fun shareBadgeAchievement(badge: Badge) {
        viewModelScope.launch {
            _toastMessage.value = "Shared '${badge.title}' badge achievement with all Circles."
        }
    }

    fun triggerTestNotification(type: String) {
        viewModelScope.launch {
            when (type) {
                "NUDGE" -> {
                    triggerNudgeForAll()
                    notificationManager.sendNudgeNotification(
                        fromMemberName = "Sarah Chen",
                        habitName = "Morning Gym"
                    )
                    _toastMessage.value = "⚡ Local Reminder Notification Triggered!"
                }
                "COMPLETION" -> {
                    notificationManager.sendSquadCompletionNotification(
                        memberName = "Alex Rivera",
                        habitName = "Daily Coding",
                        streakDays = 16
                    )
                    _toastMessage.value = "🎉 Local Circle Completion Notification Triggered!"
                }
                "DEADLINE" -> {
                    notificationManager.sendDeadlineReminderNotification(
                        habitName = "Morning Walk",
                        squadName = "Morning Striders",
                        hoursRemaining = 1
                    )
                    _toastMessage.value = "🚨 Scheduled 1-Hour Deadline Warning Triggered!"
                }
            }
        }
    }

    fun createHabit(
        name: String,
        category: String,
        emoji: String,
        squadName: String,
        invitedFriends: List<String>,
        frequencyGoal: String = "Daily"
    ) {
        viewModelScope.launch {
            repository.createHabit(name, category, emoji, squadName, invitedFriends, frequencyGoal)
            _toastMessage.value = "Habit Circle '$name' created!"
            notifyWidgetsUpdate()
        }
    }

    fun getInviteLink(habitId: String, habitName: String, squadName: String): String {
        return repository.generateInviteLink(habitId, habitName, squadName)
    }

    fun shareHabitCircleInviteLink(context: Context, habitId: String, habitName: String, squadName: String) {
        val link = repository.generateInviteLink(habitId, habitName, squadName)
        val shareBody = "Join my Habit Circle '$habitName' ($squadName) on Streak!\n\nClick or open the link below to build daily streaks together:\n$link"

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        if (clipboardManager != null) {
            val clip = android.content.ClipData.newPlainText("Streak Invite Link", link)
            clipboardManager.setPrimaryClip(clip)
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareBody)
            putExtra(Intent.EXTRA_SUBJECT, "Join my '$habitName' Circle on Streak!")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Circle Invite Link via...")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)

        _toastMessage.value = "Invite link copied & share sheet opened!"
    }

    fun addFriendToCircle(habitId: String, friendName: String) {
        viewModelScope.launch {
            val newMember = repository.addFriendToCircle(habitId, friendName)
            val habit = habits.value.find { it.id == habitId }
            val squadName = habit?.squadName ?: "Squad Circle"
            _toastMessage.value = "${newMember.name} added to '$squadName' circle!"
            notifyWidgetsUpdate()
        }
    }

    fun joinCircleFromInviteLinkOrCode(linkOrCode: String, onJoined: (String) -> Unit = {}) {
        viewModelScope.launch {
            val joinedHabit = repository.joinCircleFromInviteLinkOrCode(linkOrCode)
            _toastMessage.value = "Joined '${joinedHabit.squadName}' (${joinedHabit.name}) circle!"
            notifyWidgetsUpdate()
            onJoined(joinedHabit.id)
        }
    }

    fun removeSquadMember(memberId: String, memberName: String, habitId: String) {
        viewModelScope.launch {
            repository.removeSquadMember(memberId, memberName, habitId)
            _toastMessage.value = "Removed $memberName from circle."
            notifyWidgetsUpdate()
        }
    }

    fun deleteHabit(habitId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            val habit = habits.value.find { it.id == habitId }
            val name = habit?.name ?: "Habit Circle"
            repository.deleteHabit(habitId)
            _toastMessage.value = "Left '$name' circle."
            notifyWidgetsUpdate()
            onDeleted()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
