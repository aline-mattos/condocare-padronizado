package com.condocare

import UserDB
import com.condocare.modules.configureDeliveryRouting
import com.condocare.modules.configureReservationRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.condocare.modules.configureServiceRouting
import com.condocare.modules.configureUserRouting
import com.condocare.plugins.JwtConfig
import com.condocare.services.DeliveryDB
import com.condocare.services.ReservationDB
import com.condocare.services.ServiceDB
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import org.jetbrains.exposed.sql.Database

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("Authorization-Token")
        anyHost()
    }
    install(Authentication) {
        jwt("jwt") {
            verifier(JwtConfig.verifier) // Configuração do verificador JWT
            realm = JwtConfig.realm
            validate { credential ->
                val payload = credential.payload
                val subject = payload.getClaim("sub").asString()
                val roles = payload.getClaim("roles").asList(String::class.java)
                if (subject != null && roles.contains("resident")) {
                    JWTPrincipal(payload)
                } else {
                    null
                }
            }
        }
    }

    val database = Database.connect(
        url = "jdbc:mysql://localhost:3306/condocare",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "1234"
    )

    configureUserRouting(UserDB(database))
    configureServiceRouting(ServiceDB(database))
    configureDeliveryRouting(DeliveryDB(database))
    configureReservationRouting(ReservationDB(database))
}