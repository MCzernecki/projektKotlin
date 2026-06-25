package com.example.projektandroid.data.validation

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    val isValid: Boolean
        get() = this is Success
}
