package com.condocare.models

import kotlinx.serialization.Serializable

@Serializable
class Service (
    var id: String?,
    var type: String?,
    var name: String?
)