package com.example.vfsgm.core

sealed interface SealedResult<out T> {
    data class Success<out T>(val data: T) : SealedResult<T>
    data class Error(val exception: Throwable) : SealedResult<Nothing>
}