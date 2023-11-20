package com.condocare.models

import kotlinx.serialization.Serializable

@Serializable
class Delivery(
    var id: String?,
    var type: String?,
    var apartment: String?,
    var block: String?,
    var receivedDate: String?
)