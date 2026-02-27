package com.example.pratilipiassignment.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratilipiassignment.data.model.Document
import com.example.pratilipiassignment.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _document = MutableLiveData<Document?>()
    val document: LiveData<Document?> = _document

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    fun initWithDocumentId(documentId: String?) {
        if (_document.value != null) return
        documentId?.let { loadDocument(it) } ?: createNew()
    }

    private fun loadDocument(id: String) {
        viewModelScope.launch {
            val doc = repository.getDocument(id)
            _document.postValue(doc)
        }
    }

    private fun createNew() {
        viewModelScope.launch {
            val doc = repository.createDocument("Untitled")
            _document.postValue(doc)
        }
    }

    fun save(title: String, contentHtml: String) {
        val doc = _document.value ?: return
        viewModelScope.launch {
            _isSaving.postValue(true)
            val updated = doc.copy(
                title = title.ifBlank { "Untitled" },
                contentHtml = contentHtml,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDocument(updated)
            _document.postValue(updated)
            _isSaving.postValue(false)
        }
    }
}
