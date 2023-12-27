package com.samsia.checkme.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuestionDao {
    @Insert
    suspend fun insert(question: Question)

    @Delete
    suspend fun remove(question: Question)

    @Query("SELECT * FROM question_table")
    suspend fun getAllQuestions(): List<Question>
}