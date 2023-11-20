package com.condocare.models

import kotlinx.serialization.Serializable

@Serializable
data class Authentication(
    val email: String?,
    val password: String?
)
