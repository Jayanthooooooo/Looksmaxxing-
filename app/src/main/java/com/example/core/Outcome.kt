package com.example.core

sealed interface Outcome<out T> {
    data class Success<out T>(val data: T) : Outcome<T>
    data class Error(val message: String, val cause: Throwable? = null) : Outcome<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Error -> this
}
