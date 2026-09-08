package com.example.docucheckai.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // अत्यंत महत्वाचे:
    // जर तू Emulator वापरत असशील, तर IP "http://10.0.2.2:8000" वापर.
    // जर तू खरा मोबाईल (USB केबलने) वापरत असशील, तर तुझ्या लॅपटॉपचा खरा Wi-Fi IP टाक (उदा. "http://192.168.1.x:8000")
    // इथे तुझा खरा IPv4 Address टाक. (लक्षात ठेव, शेवटी :8000 असणे गरजेचे आहे)
    private const val BASE_URL = "http://10.104.160.191:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // API मध्ये काय डेटा चाललाय ते Logcat मध्ये दिसेल
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS) // AI ला वेळ लागू शकतो म्हणून 60 सेकंद Timeout
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: DocuCheckApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DocuCheckApi::class.java)
    }
}