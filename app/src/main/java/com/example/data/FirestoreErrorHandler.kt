package com.example.data

import android.util.Log
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val retryable: Boolean = true) : UiState<Nothing>
}

class BazmoApiException(
    val statusCode: Int,
    override val message: String
) : Exception(message)

fun <T> handleApiError(
    exception: Throwable,
    operationType: OperationType,
    path: String?,
    userId: String? = null
): Result<T> {
    if (exception is CancellationException) {
        throw exception
    }

    val statusCode = (exception as? BazmoApiException)?.statusCode
    val errorJson = JSONObject().apply {
        put("operationType", operationType.name)
        put("path", path ?: "")
        put("userId", userId ?: "anonymous")
        put("statusCode", statusCode ?: 0)
        put("message", exception.message ?: "Unknown API error")
    }.toString()

    Log.e("BazmoApiError", errorJson, exception)

    val friendlyMessage = when {
        exception is BazmoApiException && exception.message.isNotBlank() -> exception.message
        statusCode == 403 -> "دسترسی به این بخش مجاز نیست."
        statusCode == 401 -> "لطفاً ابتدا وارد حساب کاربری خود شوید."
        statusCode == 404 -> "اطلاعات مورد نظر در سرور یافت نشد."
        statusCode != null && statusCode >= 500 -> "سرور بازمو موقتاً با خطا مواجه شد. لطفاً چند لحظه دیگر تلاش کنید."
        exception is UnknownHostException ||
            exception is ConnectException ||
            exception is SocketTimeoutException ->
            "خطا در ارتباط با سرور بازمو. لطفاً اتصال اینترنت را بررسی کنید."
        else ->
            "خطای ارتباط با سرور: ${exception.localizedMessage ?: "لطفاً دوباره تلاش کنید"}"
    }

    return Result.failure(Exception(friendlyMessage, exception))
}

fun <T> handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?,
    userId: String? = null
): Result<T> = handleApiError(exception, operationType, path, userId)
