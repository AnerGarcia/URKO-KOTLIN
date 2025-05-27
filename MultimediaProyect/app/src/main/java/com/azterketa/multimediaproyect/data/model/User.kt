package com.azterketa.multimediaproyect.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String = ""
)