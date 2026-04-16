package com.scaffold.app.data.mappers

import com.scaffold.app.data.models.LoginResponse
import com.scaffold.app.domain.models.AuthResult
import com.scaffold.app.domain.models.User
import javax.inject.Inject

/**
 * Mapper de autenticação: converte respostas da API para modelos de domínio.
 */
class AuthMapper @Inject constructor() {

    fun toDomain(response: LoginResponse): AuthResult = AuthResult(
        user = User(
            id = response.user.id,
            name = response.user.name,
            email = response.user.email
        ),
        token = response.token
    )
}
