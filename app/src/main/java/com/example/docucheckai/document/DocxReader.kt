package com.example.docucheckai.document

import android.content.Context
import android.net.Uri
import android.util.Log
import org.w3c.dom.Element
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

// Advanced Model with 'isBold' support
data class ParsedElement(
    val text: String,
    val fontSize: Int?,
    val alignment: String?,
    val isBold: Boolean = false
)

class DocxReader(private val context: Context) {

    fun readDocxFormatting(uri: Uri): List<ParsedElement> {
        val elements = mutableListOf<ParsedElement>()

        try {
            // 'use' ब्लॉक वापरल्यामुळे Memory Leak होत नाही, फाईल आपोआप close होते.
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry: ZipEntry? = zipInputStream.nextEntry

                    // DOCX च्या आतली मुख्य document.xml फाईल शोधणे
                    while (entry != null) {
                        if (entry.name == "word/document.xml") {
                            parseXml(zipInputStream, elements)
                            break
                        }
                        entry = zipInputStream.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DocxReader", "Error reading DOCX file: ${e.message}")
        }

        return elements
    }

    private fun parseXml(inputStream: InputStream, elements: MutableList<ParsedElement>) {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            // XML Injection हल्ले रोखण्यासाठी सुरक्षेची काळजी
            factory.isNamespaceAware = false
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(inputStream)

            // DOCX मध्ये <w:p> म्हणजे paragraph
            val paragraphs = doc.getElementsByTagName("w:p")

            for (i in 0 until paragraphs.length) {
                val pNode = paragraphs.item(i)

                // Smart Default Values
                var alignment: String = "left"
                var fontSize: Int = 12 // Default college body font size
                var isBold = false
                val textBuilder = java.lang.StringBuilder()

                val children = pNode.childNodes
                for (j in 0 until children.length) {
                    val child = children.item(j)

                    // <w:pPr> म्हणजे paragraph properties (उदा. Alignment)
                    if (child.nodeName == "w:pPr") {
                        val element = child as Element
                        val jcNodes = element.getElementsByTagName("w:jc")
                        if (jcNodes.length > 0) {
                            val alignVal = (jcNodes.item(0) as Element).getAttribute("w:val")
                            if (alignVal.isNotBlank()) alignment = alignVal
                        }
                    }

                    // <w:r> म्हणजे run (Text आणि त्याचे Formatting)
                    if (child.nodeName == "w:r") {
                        val rChildren = child.childNodes
                        for (k in 0 until rChildren.length) {
                            val rChild = rChildren.item(k)

                            // <w:rPr> म्हणजे run properties (उदा. Font Size आणि Bold)
                            if (rChild.nodeName == "w:rPr") {
                                val rElement = rChild as Element

                                // Font Size Check (<w:sz>)
                                val szNodes = rElement.getElementsByTagName("w:sz")
                                if (szNodes.length > 0) {
                                    val sizeVal = (szNodes.item(0) as Element).getAttribute("w:val")
                                    val parsedSize = sizeVal.toIntOrNull()
                                    if (parsedSize != null) {
                                        fontSize = parsedSize / 2 // DOCX मध्ये 24 = 12pt असते
                                    }
                                }

                                // Bold Text Check (<w:b>)
                                val bNodes = rElement.getElementsByTagName("w:b")
                                if (bNodes.length > 0) {
                                    isBold = true
                                }
                            }

                            // <w:t> म्हणजे खरा Text
                            if (rChild.nodeName == "w:t") {
                                textBuilder.append(rChild.textContent)
                            }
                        }
                    }
                }

                val finalString = textBuilder.toString()
                if (finalString.isNotBlank()) {
                    elements.add(ParsedElement(text = finalString, fontSize = fontSize, alignment = alignment, isBold = isBold))
                }
            }
        } catch (e: Exception) {
            Log.e("DocxReader", "Error parsing XML: ${e.message}")
        }
    }
}