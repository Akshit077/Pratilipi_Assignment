package com.example.pratilipiassignment

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pratilipiassignment.databinding.ActivityMainBinding
import com.example.pratilipiassignment.ui.list.DocumentListAdapter
import com.example.pratilipiassignment.ui.list.DocumentListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: DocumentListViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val adapter = DocumentListAdapter(
            onItemClick = { doc -> startActivity(Intent(this, EditorActivity::class.java).putExtra(EditorActivity.EXTRA_DOCUMENT_ID, doc.id)) },
            onItemLongClick = { doc ->
                AlertDialog.Builder(this)
                    .setTitle(doc.title.ifBlank { getString(R.string.untitled) })
                    .setMessage("Delete this document?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteDocument(doc) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        viewModel.documents.observe(this) { list -> adapter.submitList(list ?: emptyList()) }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, EditorActivity::class.java))
        }
    }
}
