package com.example.docucheckai.document

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.ImageRenderInfo
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.TextExtractionStrategy
import com.itextpdf.text.pdf.parser.TextRenderInfo
import java.io.InputStream

class PdfReader(private val context: Context) {

    fun readPdfFormatting(uri: Uri): List<ParsedElement> {
        val elements = mutableListOf<ParsedElement>()
        var inputStream: InputStream? = null
        var reader: PdfReader? = null

        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                reader = PdfReader(inputStream)
                val pages = reader.numberOfPages

                // 1. Advanced PDF Extraction Strategy
                // हा कस्टम क्लास PDF मधील फक्त शब्दच नाही, तर त्यांचा आकार (Size) आणि स्टाईल (Bold) ओळखतो.
                val strategy = object : TextExtractionStrategy {
                    override fun beginTextBlock() {}
                    override fun endTextBlock() {}
                    override fun getResultantText(): String = ""
                    override fun renderImage(renderInfo: ImageRenderInfo?) {}

                    override fun renderText(renderInfo: TextRenderInfo?) {
                        renderInfo?.let {
                            val text = it.text
                            if (text.isNotBlank()) {
                                // A. Extracting Real Font Size (Using Y-axis Coordinate Math)
                                // PDF मध्ये अक्षराच्या वरच्या (Ascent) आणि खालच्या (Descent) रेषेमधील अंतर म्हणजे Font Size
                                val ascent = it.ascentLine.startPoint.get(1)
                                val descent = it.descentLine.startPoint.get(1)
                                val calculatedSize = (ascent - descent).toInt()

                                // जर calculation मध्ये चूक झाली, तर Default 12pt वापरणे
                                val finalSize = if (calculatedSize > 0) calculatedSize else 12

                                // B. Extracting Boldness
                                // PDF च्या Font च्या नावात जर 'bold' किंवा 'black' असेल, तर तो ठळक मजकूर आहे
                                val fontName = it.font?.postscriptFontName?.lowercase() ?: ""
                                val isBold = fontName.contains("bold") || fontName.contains("black")

                                // PDF मध्ये Alignment काढणे तांत्रिकदृष्ट्या शक्य नसते, त्यामुळे ते Default 'left' ठेवले आहे
                                elements.add(
                                    ParsedElement(
                                        text = text.trim(),
                                        fontSize = finalSize,
                                        alignment = "left",
                                        isBold = isBold
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. PDF च्या प्रत्येक पानावरून खऱ्या Formatting सोबत Text वाचणे
                for (i in 1..pages) {
                    PdfTextExtractor.getTextFromPage(reader, i, strategy)
                }
            }
        } catch (e: Exception) {
            Log.e("PdfReader", "Error reading PDF file: ${e.message}")
        } finally {
            // Memory Leak टाळण्यासाठी Stream Close करणे अत्यंत महत्त्वाचे आहे
            reader?.close()
            inputStream?.close()
        }

        return elements
    }
}