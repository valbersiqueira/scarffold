package com.scaffold.app.domain.usecases

import javax.inject.Inject

/**
 * Caso de uso para validação de senha.
 * Regra padrão: mínimo 8 caracteres.
 */
class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String): Boolean = password.length >= 8
}
