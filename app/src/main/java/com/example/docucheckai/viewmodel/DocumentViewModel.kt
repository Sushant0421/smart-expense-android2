package com.example.docucheckai.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docucheckai.model.AnalysisResult
import com.example.docucheckai.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class DocumentViewModel : ViewModel() {

    private val _currentAnalysis = mutableStateOf<AnalysisResult?>(null)
    val currentAnalysis: State<AnalysisResult?> = _currentAnalysis

    // 1. फाईल सर्व्हरला पाठवून Analyze करणे
    fun uploadAndAnalyzeDocument(
        context: Context,
        uri: Uri,
        fileName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = getFileFromUri(context, uri, fileName)
                if (file == null) {
                    withContext(Dispatchers.Main) { onError("Failed to read file") }
                    return@launch
                }

                val requestFile = file.asRequestBody("application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RetrofitClient.api.analyzeDocument(body)

                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        _currentAnalysis.value = response.body()
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) { onError("Server error: ${response.code()}") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Network error: ${e.localizedMessage}") }
            }
        }
    }

    // 2. फाईल फिक्स करणे आणि फोनच्या Downloads मध्ये सेव्ह करणे
    fun fixDocument(
        context: Context,
        uri: Uri,
        fileName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = getFileFromUri(context, uri, fileName)
                if (file == null) {
                    withContext(Dispatchers.Main) { onError("Failed to read file") }
                    return@launch
                }

                val requestFile = file.asRequestBody("application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RetrofitClient.api.fixDocument(body)

                if (response.isSuccessful && response.body() != null) {
                    // 🚀 नवीन MediaStore पद्धत (No Crash)
                    val savedFileName = saveFixedFileToPhone(context, response.body()!!, fileName)
                    withContext(Dispatchers.Main) {
                        if (savedFileName.isNotEmpty()) {
                            onSuccess("Success! File saved as $savedFileName in Downloads folder.")
                        } else {
                            onError("Failed to save file on phone.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { onError("Server error: ${response.code()}") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Network error: ${e.localizedMessage}") }
            }
        }
    }

    // फाईलच्या Uri मधून Temporary फाईल बनवणे
    private fun getFileFromUri(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // फोनच्या 'Downloads' फोल्डरमध्ये फाईल सुरक्षितपणे सेव्ह करणे (Android 10+ Support)
    private fun saveFixedFileToPhone(context: Context, body: ResponseBody, originalFileName: String): String {
        return try {
            val newFileName = "Fixed_$originalFileName"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    body.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                newFileName
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}