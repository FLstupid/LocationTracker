package com.example.locationtracker.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    protected val _error = MutableSharedFlow<UiError>()
    val error = _error

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            _loading.value = true
            try {
                block()
            } finally {
                _loading.value = false
            }
        }
    }

    protected open fun handleError(throwable: Throwable) {
        viewModelScope.launch {
            _error.emit(
                UiError(
                    message = throwable.message ?: "Unknown error",
                    cause = throwable
                )
            )
        }
    }
}
