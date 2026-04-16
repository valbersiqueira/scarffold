package com.scaffold.app.data.repositories

import com.scaffold.app.data.api.ApiService
import com.scaffold.app.data.api.safeApiCall
import com.scaffold.app.data.api.ApiResult
import com.scaffold.app.data.mappers.SampleItemMapper
import com.scaffold.app.domain.models.SampleItem
import com.scaffold.app.domain.repositories.SampleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Implementação concreta do SampleRepository.
 * Busca dados da API e converte para o domínio.
 */
class SampleRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: SampleItemMapper
) : SampleRepository {

    override fun getItems(): Flow<List<SampleItem>> = flow {
        val result = safeApiCall { apiService.getItems() }
        when (result) {
            is ApiResult.Success -> emit(mapper.toDomainList(result.data))
            is ApiResult.Error -> throw Exception("Erro ${result.code}: ${result.message}")
            is ApiResult.Exception -> throw result.e
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getItemById(id: String): SampleItem? {
        val result = safeApiCall { apiService.getItemById(id) }
        return when (result) {
            is ApiResult.Success -> mapper.toDomain(result.data)
            else -> null
        }
    }

    override suspend fun saveItem(item: SampleItem) {
        // Implementar persistência local ou chamada API conforme necessidade
    }

    override suspend fun deleteItem(id: String) {
        safeApiCall { apiService.deleteItem(id) }
    }
}
