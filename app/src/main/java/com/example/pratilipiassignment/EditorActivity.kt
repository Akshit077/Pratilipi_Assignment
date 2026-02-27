package com.example.pratilipiassignment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.example.pratilipiassignment.databinding.ActivityEditorBinding
import com.example.pratilipiassignment.ui.editor.EditorViewModel
import com.example.pratilipiassignment.util.RichTextConverter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DOCUMENT_ID = "documentId"
    }

    private val viewModel: EditorViewModel by viewModels()
    private lateinit var binding: ActivityEditorBinding

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { insertImageAtCursor(it) }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pickImage.launch("image/*") else Toast.makeText(this, "Permission needed to insert images", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        viewModel.initWithDocumentId(intent.getStringExtra(EXTRA_DOCUMENT_ID))

        viewModel.document.observe(this, Observer { doc ->
            doc ?: return@Observer
            if (binding.titleInput.tag != doc.id) {
                binding.titleInput.tag = doc.id
                binding.titleInput.setText(doc.title)
            }
            if (binding.contentInput.tag != doc.id) {
                binding.contentInput.tag = doc.id
                binding.contentInput.setText(RichTextConverter.fromHtml(doc.contentHtml))
            }
        })
        viewModel.isSaving.observe(this, Observer { saving ->
            if (saving) Toast.makeText(this, "Saving…", Toast.LENGTH_SHORT).show()
        })

        binding.titleInput.addTextChangedListener { binding.titleInput.tag?.let { } }

        binding.btnBold.setOnClickListener { applySpan { StyleSpan(android.graphics.Typeface.BOLD) } }
        binding.btnItalic.setOnClickListener { applySpan { StyleSpan(android.graphics.Typeface.ITALIC) } }
        binding.btnUnderline.setOnClickListener { applySpan { UnderlineSpan() } }
        binding.btnStrikethrough.setOnClickListener { applySpan { StrikethroughSpan() } }
        binding.btnTextColor.setOnClickListener { showColorPicker(foreground = true) }
        binding.btnHighlight.setOnClickListener { showColorPicker(foreground = false) }
        binding.btnImage.setOnClickListener { launchImagePicker() }
    }

    override fun onSupportNavigateUp(): Boolean {
        save()
        return super.onSupportNavigateUp()
    }

    override fun onPause() {
        save()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            save()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun save() {
        val doc = viewModel.document.value ?: return
        val title = binding.titleInput.text?.toString() ?: ""
        val content = binding.contentInput.text
        val html = RichTextConverter.toHtml(content)
        viewModel.save(title, html)
    }

    private fun applySpan(spanFactory: () -> Any) {
        val edit = binding.contentInput.editableText
        val start = binding.contentInput.selectionStart
        val end = binding.contentInput.selectionEnd
        if (start < 0 || end < 0 || start == end) return
        edit.setSpan(spanFactory(), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun showColorPicker(foreground: Boolean) {
        val colors = intArrayOf(
            0xFF000000.toInt(), 0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(), 0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(), 0xFF009688.toInt(), 0xFF4CAF50.toInt(), 0xFF8BC34A.toInt(),
            0xFFCDDC39.toInt(), 0xFFFFEB3B.toInt(), 0xFFFFC107.toInt(), 0xFFFF9800.toInt()
        )
        val labels = colors.map { "#${Integer.toHexString(it).takeLast(6).uppercase()}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(if (foreground) getString(R.string.text_color) else getString(R.string.highlight))
            .setItems(labels) { _, which ->
                val start = binding.contentInput.selectionStart
                val end = binding.contentInput.selectionEnd
                if (start < 0 || end <= start) return@setItems
                val span = if (foreground) ForegroundColorSpan(colors[which]) else BackgroundColorSpan(colors[which])
                binding.contentInput.editableText.setSpan(span, start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            .show()
    }

    private fun launchImagePicker() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
                    pickImage.launch("image/*")
                else
                    requestPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> pickImage.launch("image/*")
            else -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    pickImage.launch("image/*")
                else
                    requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                return
            }
        }
    }

    private fun insertImageAtCursor(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream) ?: return
            val drawable = BitmapDrawable(resources, bitmap).apply {
                setBounds(0, 0, intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1))
            }
            val span = ImageSpan(drawable)
            val edit = binding.contentInput.editableText
            val pos = binding.contentInput.selectionStart.coerceIn(0, edit.length)
            val sb = SpannableStringBuilder(edit).append("\uFFFC")
            sb.setSpan(span, pos, pos + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.contentInput.setText(sb)
            binding.contentInput.setSelection(pos + 1)
        }
    }
}
