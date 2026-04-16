package com.scaffold.app.domain.repositories

import com.scaffold.app.domain.models.SampleItem
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de itens de exemplo.
 * A camada de domínio define contratos; a camada de dados implementa.
 */
interface SampleRepository {
    fun getItems(): Flow<List<SampleItem>>
    suspend fun getItemById(id: String): SampleItem?
    suspend fun saveItem(item: SampleItem)
    suspend fun deleteItem(id: String)
}
