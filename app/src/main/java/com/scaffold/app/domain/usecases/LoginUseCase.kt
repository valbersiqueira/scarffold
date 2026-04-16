package com.scaffold.app.domain.usecases

import com.scaffold.app.domain.models.AuthResult
import com.scaffold.app.domain.models.Credentials
import com.scaffold.app.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso de autenticação por email/senha.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(credentials: Credentials): Flow<AuthResult> =
        repository.login(credentials)
}
