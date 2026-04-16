package com.scaffold.app.domain.usecases

import com.scaffold.app.domain.models.SampleItem
import com.scaffold.app.domain.repositories.SampleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para buscar lista de itens de exemplo.
 * Cada use case representa uma única ação de negócio.
 */
class GetSampleItemsUseCase @Inject constructor(
    private val repository: SampleRepository
) {
    operator fun invoke(): Flow<List<SampleItem>> = repository.getItems()
}
