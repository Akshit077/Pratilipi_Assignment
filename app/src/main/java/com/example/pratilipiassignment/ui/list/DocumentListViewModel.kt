package com.example.pratilipiassignment.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pratilipiassignment.data.model.Document
import com.example.pratilipiassignment.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    val documents: LiveData<List<Document>> = repository.getAllDocuments()

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            repository.deleteDocument(document.id)
        }
    }
}
