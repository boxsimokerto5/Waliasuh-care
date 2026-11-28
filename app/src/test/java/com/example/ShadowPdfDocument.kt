package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.OutputStream
import java.nio.charset.StandardCharsets

@Implements(PdfDocument::class)
class ShadowPdfDocument {
    private val finishedPages = mutableListOf<PdfDocument.PageInfo>()
    private val allocatedBitmaps = mutableListOf<Bitmap>()
    private var currentPage: PdfDocument.Page? = null
    private var closed = false

    @Implementation
    fun __constructor__() {
        closed = false
    }

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo?): PdfDocument.Page {
        check(!closed) { "document is closed!" }
        check(currentPage == null) { "current page not finished!" }
        val info = requireNotNull(pageInfo) { "pageInfo cannot be null" }
        val bitmap = Bitmap.createBitmap(
            info.pageWidth.coerceAtLeast(1),
            info.pageHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        allocatedBitmaps.add(bitmap)
        val canvas = Canvas(bitmap)
        val ctor = PdfDocument.Page::class.java.getDeclaredConstructor(
            Canvas::class.java,
            PdfDocument.PageInfo::class.java
        )
        ctor.isAccessible = true
        val page = ctor.newInstance(canvas, info)
        currentPage = page
        return page
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page?) {
        check(!closed) { "document is closed!" }
        val info = page?.info
        if (info != null) {
            finishedPages.add(info)
        }
        currentPage = null
    }

    @Implementation
    fun getPages(): List<PdfDocument.PageInfo> = finishedPages.toList()

    @Implementation
    fun writeTo(out: OutputStream?) {
        check(!closed) { "document is closed!" }
        check(currentPage == null) { "current page not finished!" }
        val stream = requireNotNull(out) { "OutputStream cannot be null" }
        val pageCount = finishedPages.size.coerceAtLeast(1)
        val payload = buildString {
            append("%PDF-1.4\n")
            append("% Robolectric ShadowPdfDocument ($pageCount pages)\n")
            append("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n")
            append("2 0 obj << /Type /Pages /Count $pageCount /Kids [3 0 R] >> endobj\n")
            append("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] >> endobj\n")
            append("%%EOF\n")
        }
        stream.write(payload.toByteArray(StandardCharsets.UTF_8))
        stream.flush()
    }

    @Implementation
    fun close() {
        closed = true
        currentPage = null
        allocatedBitmaps.forEach { bmp ->
            if (!bmp.isRecycled) bmp.recycle()
        }
        allocatedBitmaps.clear()
    }

    @Implementation
    fun finalize() {
        // No-op in shadow to avoid native teardown warnings on GC
    }
}
