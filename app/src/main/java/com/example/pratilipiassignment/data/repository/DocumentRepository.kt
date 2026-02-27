package com.example.pratilipiassignment.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.pratilipiassignment.data.local.DocumentDao
import com.example.pratilipiassignment.data.local.DocumentEntity
import com.example.pratilipiassignment.data.model.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DocumentRepository @Inject constructor(
    private val dao: DocumentDao
) {

    fun getAllDocuments(): LiveData<List<Document>> {
        val result = MediatorLiveData<List<Document>>()
        result.addSource(dao.getAllDocuments()) { entities ->
            result.value = entities?.map { entity -> entity.toDocument() } ?: emptyList()
        }
        return result
    }

    suspend fun getDocument(id: String): Document? = withContext(Dispatchers.IO) {
        dao.getDocument(id)?.toDocument()
    }

    suspend fun saveDocument(document: Document) = withContext(Dispatchers.IO) {
        dao.insert(document.toEntity())
    }

    suspend fun createDocument(title: String): Document = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val doc = Document(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            contentHtml = "",
            createdAt = now,
            updatedAt = now
        )
        dao.insert(doc.toEntity())
        doc
    }

    suspend fun deleteDocument(id: String) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    private fun DocumentEntity.toDocument() = Document(
        id = id,
        title = title,
        contentHtml = contentHtml,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Document.toEntity() = DocumentEntity(
        id = id,
        title = title,
        contentHtml = contentHtml,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
