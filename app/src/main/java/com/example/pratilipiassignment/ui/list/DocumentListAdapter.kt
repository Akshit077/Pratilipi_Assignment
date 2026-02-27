package com.example.pratilipiassignment.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pratilipiassignment.data.model.Document
import com.example.pratilipiassignment.databinding.ItemDocumentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentListAdapter(
    private val onItemClick: (Document) -> Unit,
    private val onItemLongClick: (Document) -> Unit
) : ListAdapter<Document, DocumentListAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onItemClick(getItem(bindingAdapterPosition)) }
            binding.root.setOnLongClickListener {
                onItemLongClick(getItem(bindingAdapterPosition))
                true
            }
        }

        fun bind(doc: Document) {
            binding.documentTitle.text = doc.title.ifBlank { binding.root.context.getString(com.example.pratilipiassignment.R.string.untitled) }
            binding.documentDate.text = dateFormat.format(Date(doc.updatedAt))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(old: Document, new: Document) = old.id == new.id
        override fun areContentsTheSame(old: Document, new: Document) = old == new
    }
}
