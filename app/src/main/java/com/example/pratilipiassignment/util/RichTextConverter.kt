package com.example.pratilipiassignment.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.util.Log
import org.xml.sax.XMLReader
import java.io.ByteArrayOutputStream

object RichTextConverter {

    private const val IMG_PREFIX = "data:image/png;base64,"

    fun toHtml(source: Spanned): String {
        val sb = StringBuilder()
        var i = 0
        val len = source.length
        while (i < len) {
            val next = nextSpanTransition(source, i, len)
            val segment = source.subSequence(i, next).toString()
            val spans = source.getSpans(i, next, Any::class.java)
            sb.append(applySpans(segment, spans))
            i = next
        }
        return sb.toString()
    }

    private fun nextSpanTransition(source: Spanned, start: Int, limit: Int): Int {
        val spans = source.getSpans(start, limit, Any::class.java)
        var next = limit
        for (span in spans) {
            val spanStart = source.getSpanStart(span)
            val spanEnd = source.getSpanEnd(span)
            if (spanStart in (start + 1) until next) next = spanStart
            if (spanEnd in (start + 1) until next) next = spanEnd
        }
        return next
    }

    private fun applySpans(text: String, spans: Array<Any>): String {
        var out = escapeHtml(text).replace("\n", "<br>")
        for (span in spans) {
            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD -> out = "<b>$out</b>"
                        android.graphics.Typeface.ITALIC -> out = "<i>$out</i>"
                        android.graphics.Typeface.BOLD_ITALIC -> out = "<b><i>$out</i></b>"
                    }
                }
                is UnderlineSpan -> out = "<u>$out</u>"
                is StrikethroughSpan -> out = "<strike>$out</strike>"
                is ForegroundColorSpan -> out = "<font color=\"#${Integer.toHexString(span.foregroundColor).takeLast(6).uppercase()}\">$out</font>"
                is BackgroundColorSpan -> {
                    val hex = Integer.toHexString(span.backgroundColor).takeLast(6).uppercase()
                    out = "<bg$hex>$out</bg$hex>"
                }
                is ImageSpan -> {
                    val b64 = drawableToBase64(span.drawable)
                    if (b64 != null) out = "<img src=\"$IMG_PREFIX$b64\"/>"
                }
            }
        }
        return out
    }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private fun drawableToBase64(drawable: Drawable): String? {
        return try {
            val bitmap = when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    val w = drawable.intrinsicWidth.coerceAtLeast(1)
                    val h = drawable.intrinsicHeight.coerceAtLeast(1)
                    val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val c = Canvas(b)
                    drawable.setBounds(0, 0, c.width, c.height)
                    drawable.draw(c)
                    b
                }
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("RichTextConverter", "drawableToBase64", e)
            null
        }
    }

    fun fromHtml(html: String): Spanned {
        val unescaped = html
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("<br>", "\n")
        val result = Html.fromHtml(unescaped, Html.FROM_HTML_MODE_LEGACY, { source ->
            if (source.startsWith(IMG_PREFIX)) {
                val base64 = source.removePrefix(IMG_PREFIX)
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    BitmapDrawable(null, bitmap).apply {
                        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                    }
                } catch (e: Exception) {
                    Log.e("RichTextConverter", "fromHtml ImageGetter", e)
                    null
                }
            } else null
        }, ColorTagHandler())
        return result
    }

    private class ColorTagHandler : Html.TagHandler {
        private val startIndices = mutableListOf<Pair<Int, String>>()

        override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader?) {
            if (!tag.startsWith("bg") || tag.length != 8) return
            if (opening) {
                startIndices.add(output.length to tag)
            } else {
                val idx = startIndices.indexOfLast { it.second == tag }
                if (idx < 0) return
                val last = startIndices.removeAt(idx)
                try {
                    val hex = tag.removePrefix("bg")
                    val color = Color.parseColor("#$hex")
                    output.setSpan(
                        BackgroundColorSpan(color),
                        last.first,
                        output.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } catch (_: Exception) { }
            }
        }
    }
}
