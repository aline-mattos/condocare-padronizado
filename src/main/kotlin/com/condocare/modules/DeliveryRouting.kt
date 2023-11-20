package com.condocare.modules

import com.condocare.models.Delivery
import com.condocare.services.DeliveryDB
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureDeliveryRouting(deliveryBD: DeliveryDB) {
    routing {
        get("/deliveries") {
            val deliveries = deliveryBD.findAll()

            if(deliveries.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, deliveries)
            } else {
                call.respond(HttpStatusCode.NotFound, "error")
            }
        }
        get("/deliveries/{id}") {
            val id = call.parameters["id"]

            if(!id.isNullOrEmpty()) {
                deliveryBD.findById(id)?.let { delivery ->
                    call.respond(HttpStatusCode.OK, delivery)
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/deliveries/{block}/{apartment}"){
            val block = call.parameters["block"]
            val apartment = call.parameters["apartment"]

            if(!block.isNullOrEmpty() && !apartment.isNullOrEmpty()){
                val deliveries = deliveryBD.findFromResident(block, apartment)
                call.respond(HttpStatusCode.OK, deliveries)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        post("/deliveries") {
            val delivery = call.receive<Delivery>()
            delivery.id = UUID.randomUUID().toString()

            call.respond(HttpStatusCode.Created, deliveryBD.create(delivery))
        }
        put("/deliveries/{id}") {
            val id = call.parameters["id"]

            if(!id.isNullOrEmpty()) {
                val delivery = call.receive<Delivery>()
                delivery.id = id
                val updated = deliveryBD.update(id, delivery)

                if (updated != null) {
                    call.respond(HttpStatusCode.OK, updated)
                }
            }
            call.respond(HttpStatusCode.NotFound)
        }
        delete("/deliveries/{id}") {
            val id = call.parameters["id"]
            if(!id.isNullOrEmpty()) {
                deliveryBD.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}