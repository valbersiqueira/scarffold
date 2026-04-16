package com.scaffold.app.data.mappers

import com.scaffold.app.data.models.SampleItemResponse
import com.scaffold.app.domain.models.SampleItem
import javax.inject.Inject

/**
 * Mapper responsável por converter modelos da API para modelos de domínio.
 */
class SampleItemMapper @Inject constructor() {

    fun toDomain(response: SampleItemResponse): SampleItem = SampleItem(
        id = response.id,
        title = response.title,
        description = response.description
    )

    fun toDomainList(responses: List<SampleItemResponse>): List<SampleItem> =
        responses.map { toDomain(it) }
}
