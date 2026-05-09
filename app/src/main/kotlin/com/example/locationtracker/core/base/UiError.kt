package com.example.locationtracker.core.base

data class UiError(
    val message: String,
    val cause: Throwable? = null
) {
    companion object {
        fun from(throwable: Throwable): UiError {
            return UiError(
                message = throwable.message ?: "An error occurred",
                cause = throwable
            )
        }
    }
}
