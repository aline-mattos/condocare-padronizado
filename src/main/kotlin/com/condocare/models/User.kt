package com.condocare.models

import kotlinx.serialization.Serializable

@Serializable
class User (
    var id: String?,
    var token: String?,
    var type: String?,
    var name: String?,
    var apartment: String?,
    var block: String?,
    var email: String?,
    var password: String?,
)