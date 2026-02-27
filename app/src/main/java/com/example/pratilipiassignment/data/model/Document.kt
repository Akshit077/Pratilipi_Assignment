package com.example.pratilipiassignment.data.model

data class Document(
    val id: String,
    val title: String,
    val contentHtml: String,
    val createdAt: Long,
    val updatedAt: Long
)
