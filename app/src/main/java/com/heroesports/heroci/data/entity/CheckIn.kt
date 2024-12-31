package com.heroesports.heroci.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId", "memberName", "checkInTime"])
    ]
)
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val memberName: String,
    val checkInTime: LocalDateTime,
    val location: String,
    val photoPath: String,
    val latitude: Double,
    val longitude: Double
) 