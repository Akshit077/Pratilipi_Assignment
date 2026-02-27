package com.example.pratilipiassignment.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val contentHtml: String,
    val createdAt: Long,
    val updatedAt: Long
)
