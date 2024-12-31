package com.heroesports.heroci.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "members",
    primaryKeys = ["projectId", "name"],
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
data class Member(
    val projectId: Long,
    val name: String
) 