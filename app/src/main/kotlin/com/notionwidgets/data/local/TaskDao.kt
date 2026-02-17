package com.notionwidgets.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE databaseId = :databaseId ORDER BY createdAt DESC")
    suspend fun getTasksByDatabase(databaseId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Query("DELETE FROM tasks WHERE databaseId = :databaseId")
    suspend fun deleteByDatabase(databaseId: String)
}
