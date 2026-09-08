package com.example.docucheckai.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable