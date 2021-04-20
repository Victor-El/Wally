package me.codeenzyme.wally.commons.models

sealed class ErrorResponse {
    object NetworkError: ErrorResponse()
    object ServerError: ErrorResponse()
}
