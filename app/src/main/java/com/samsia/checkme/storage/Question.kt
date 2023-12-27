package com.samsia.checkme.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_table")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionText: String
)