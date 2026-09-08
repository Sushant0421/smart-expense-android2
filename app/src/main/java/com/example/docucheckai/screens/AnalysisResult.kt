package com.example.docucheckai.model

import com.google.gson.annotations.SerializedName

data class AnalysisResult(
    @SerializedName("id") val id: String = "",
    @SerializedName("fileName") val fileName: String = "Document",
    @SerializedName("score") val score: Int = 0,
    @SerializedName("totalIssues") val totalIssues: Int = 0,
    @SerializedName("issues") val issues: List<DocumentIssue> = emptyList()
)

data class DocumentIssue(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String = "Formatting Issue",
    @SerializedName("description") val description: String = "No description available",
    @SerializedName("severity") val severity: String = "INFO",
    @SerializedName("currentValue") val currentValue: String = "",
    @SerializedName("expectedValue") val expectedValue: String = ""
)