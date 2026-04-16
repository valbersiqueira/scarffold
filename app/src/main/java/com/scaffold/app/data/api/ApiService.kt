package com.scaffold.app.data.api

import com.scaffold.app.data.models.LoginRequest
import com.scaffold.app.data.models.LoginResponse
import com.scaffold.app.data.models.SampleItemRequest
import com.scaffold.app.data.models.SampleItemResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface do serviço de API.
 * Adicione seus endpoints aqui.
 */
interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // --- Sample Items ---
    @GET("items")
    suspend fun getItems(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<List<SampleItemResponse>>

    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<SampleItemResponse>

    @POST("items")
    suspend fun createItem(@Body request: SampleItemRequest): Response<SampleItemResponse>

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<Unit>
}
