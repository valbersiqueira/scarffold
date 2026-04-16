package com.scaffold.app.data.api

/**
 * Sealed class para representar resultados de chamadas de API.
 * Use sempre safeApiCall para encapsular chamadas Retrofit.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String?) : ApiResult<Nothing>()
    data class Exception(val e: Throwable) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(code = response.code(), message = "Response body is null")
            }
        } else {
            ApiResult.Error(code = response.code(), message = response.errorBody()?.string())
        }
    } catch (e: Throwable) {
        ApiResult.Exception(e)
    }
}
