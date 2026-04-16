package com.scaffold.app.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de resposta da API para o item de exemplo.
 * Mantenha modelos de API separados dos modelos de domínio.
 */
data class SampleItemResponse(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = ""
)

/**
 * Modelo de requisição genérico.
 */
data class SampleItemRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
)
