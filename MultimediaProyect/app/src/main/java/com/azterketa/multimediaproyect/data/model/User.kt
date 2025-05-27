package com.azterketa.multimediaproyect.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)