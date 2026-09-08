package com.example.docucheckai.network

import com.example.docucheckai.model.AnalysisResult
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DocuCheckApi {

    // १. Analysis साठी फाईल पाठवणे
    @Multipart
    @POST("/api/v1/analyze")
    suspend fun analyzeDocument(
        @Part file: MultipartBody.Part
    ): Response<AnalysisResult>

    // २. Fix करण्यासाठी फाईल पाठवणे आणि नवीन फाईल (ResponseBody) डाउनलोड करणे
    @Multipart
    @POST("/api/v1/fix")
    suspend fun fixDocument(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>
}