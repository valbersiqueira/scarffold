package com.scaffold.app.domain.repositories

import com.scaffold.app.domain.models.AuthResult
import com.scaffold.app.domain.models.Credentials
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de autenticação.
 */
interface AuthRepository {
    suspend fun login(credentials: Credentials): Flow<AuthResult>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    fun getSavedCredentials(): Credentials?
    suspend fun saveCredentials(credentials: Credentials)
}
