package com.heroesports.heroci.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "check_in_records",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"])
    ]
)
data class CheckInRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val checkInTime: LocalDateTime = LocalDateTime.now(),
    val note: String = ""
) 