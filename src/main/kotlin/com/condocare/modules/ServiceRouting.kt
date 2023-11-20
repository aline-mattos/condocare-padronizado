package com.condocare.modules

import com.condocare.models.Service
import com.condocare.services.ServiceDB
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureServiceRouting(serviceDB: ServiceDB) {
    routing {
        get("/services") {
            val services = serviceDB.findAll()

            if(services.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, services)
            } else {
                call.respond(HttpStatusCode.NotFound, "error")
            }
        }
        get("/services/{id}") {
            val id = call.parameters["id"]

            if(!id.isNullOrEmpty()) {
                serviceDB.findById(id)?.let { service ->
                    call.respond(HttpStatusCode.OK, service)
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        post("/services") {
            val service = call.receive<Service>()
            service.id = UUID.randomUUID().toString()

            call.respond(HttpStatusCode.Created, serviceDB.create(service))
        }
        get("/services_type/{type}"){
            val type = call.parameters["type"]

            if(!type.isNullOrEmpty()){
                val services = serviceDB.findByType(type)
                call.respond(HttpStatusCode.OK, services)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/services/{id}") {
            val id = call.parameters["id"]

            if(!id.isNullOrEmpty()) {
                val service = call.receive<Service>()
                service.id = id
                val updated = serviceDB.update(id, service)

                if (updated != null) {
                    call.respond(HttpStatusCode.OK, updated)
                }
            }
            call.respond(HttpStatusCode.NotFound)
        }
        delete("/services/{id}") {
            val id = call.parameters["id"]
            if(!id.isNullOrEmpty()) {
                serviceDB.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}