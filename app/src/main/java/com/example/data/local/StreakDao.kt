package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsDirect(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: String): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitByIdDirect(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)

    // Squad members
    @Query("SELECT * FROM squad_members WHERE habitId = :habitId")
    fun getSquadMembersForHabit(habitId: String): Flow<List<SquadMemberEntity>>

    @Query("SELECT * FROM squad_members WHERE habitId = :habitId")
    suspend fun getSquadMembersForHabitDirect(habitId: String): List<SquadMemberEntity>

    @Query("SELECT * FROM squad_members")
    fun getAllSquadMembers(): Flow<List<SquadMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSquadMembers(members: List<SquadMemberEntity>)

    @Update
    suspend fun updateSquadMember(member: SquadMemberEntity)

    @Query("DELETE FROM squad_members WHERE id = :memberId")
    suspend fun deleteSquadMember(memberId: String)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)

    // Weekly History
    @Query("SELECT * FROM weekly_history ORDER BY habitId, dayIndex ASC")
    fun getAllWeeklyHistory(): Flow<List<WeeklyHistoryEntity>>

    @Query("SELECT * FROM weekly_history WHERE habitId = :habitId ORDER BY dayIndex ASC")
    fun getWeeklyHistoryForHabit(habitId: String): Flow<List<WeeklyHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyHistory(entries: List<WeeklyHistoryEntity>)

    @Update
    suspend fun updateWeeklyHistory(entry: WeeklyHistoryEntity)
}
