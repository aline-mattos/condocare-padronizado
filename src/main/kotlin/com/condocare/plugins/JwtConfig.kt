package com.condocare.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier

object JwtConfig {
    private const val secret = "seu-segredo-aqui" // Substitua pelo seu próprio segredo

    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer("seu-issuer") // Substitua pelo emissor do seu JWT
        .build()

    val audience = "sua-audiencia" // Substitua pela audiência do seu JWT
    val realm = "ktor-app"
}
