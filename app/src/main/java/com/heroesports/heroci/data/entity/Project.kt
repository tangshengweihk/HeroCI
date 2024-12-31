package com.heroesports.heroci.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val password: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) 