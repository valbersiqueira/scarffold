package com.scaffold.app.domain.usecases

import javax.inject.Inject

/**
 * Caso de uso para validação de e-mail.
 */
class ValidateEmailUseCase @Inject constructor() {
    operator fun invoke(email: String): Boolean =
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
