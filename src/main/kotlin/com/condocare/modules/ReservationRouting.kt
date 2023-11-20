package com.condocare.modules

import com.condocare.models.Reservation
import com.condocare.services.ReservationDB
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureReservationRouting(reservationDB: ReservationDB) {
    routing {
        get("/reservations") {
            val reservations = reservationDB.findAll()

            if(reservations.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, reservations)
            } else {
                call.respond(HttpStatusCode.NotFound, "error")
            }
        }
        get("/reservations/{id}") {
            val id = call.parameters["id"]

            if(!id.isNullOrEmpty()) {
                reservationDB.findById(id)?.let { reservation ->
                    call.respond(HttpStatusCode.OK, reservation)
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        post("/reservations") {
            val reservation = call.receive<Reservation>()
            reservation.id = UUID.randomUUID().toString()

            call.respond(HttpStatusCode.Created, reservationDB.create(reservation))
        }
        put("/reservations/{id}") {
            val id = call.parameters["id"]

            if(!id.isNullOrEmpty()) {
                val reservation = call.receive<Reservation>()
                reservation.id = id
                val updated = reservationDB.update(id, reservation)

                if (updated != null) {
                    call.respond(HttpStatusCode.OK, updated)
                }
            }
            call.respond(HttpStatusCode.NotFound)
        }
        delete("/reservations/{id}") {
            val id = call.parameters["id"]
            if(!id.isNullOrEmpty()) {
                reservationDB.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}