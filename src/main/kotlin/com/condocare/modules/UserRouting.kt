package com.condocare.modules

import UserDB
import com.condocare.models.Authentication
import com.condocare.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureUserRouting(user: UserDB) {
    routing {
        get("/users") {
            call.respond(HttpStatusCode.OK, user.findAll())
        }

        get("/users/{id}") {
            val id = call.parameters["id"]

            if (!id.isNullOrEmpty()) {
                user.findById(id)?.let { user ->
                    call.respond(HttpStatusCode.OK, user)
                } ?: call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/users") {
            call.respond(HttpStatusCode.Created, user.create(call.receive<User>()))
        }

        post("/login"){
            val authentication = call.receive<Authentication>()

            if (!authentication.email.isNullOrEmpty() && !authentication.password.isNullOrEmpty()){
                val user = user.findByEmail(authentication.email)
                if (user != null) {
                    if (user.password == authentication.password) {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
                call.respond(HttpStatusCode.NotFound, user.toString())
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        put("/users/{id}") {
            val id = call.parameters["id"]

            if (!id.isNullOrEmpty()) {
                val user = user.update(id, call.receive())

                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/users/{id}") {
            val id = call.parameters["id"]
            if (!id.isNullOrEmpty()) {
                user.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}