package com.condocare.models

import kotlinx.serialization.Serializable

@Serializable
class Reservation (
    var id: String?,
    var type: String?,
    var name: String?,
    var date: String?
)