package com.scaffold.app.domain.models

/**
 * Modelos de domínio para autenticação.
 */
data class User(
    val id: String,
    val name: String,
    val email: String
)

data class Credentials(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

data class AuthResult(
    val user: User,
    val token: String
)
