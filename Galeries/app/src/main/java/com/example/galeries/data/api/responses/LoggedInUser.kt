package com.example.galeries.data.api.responses

/**
 * Data class that captures user information for logged in users retrieved from Repository
 */
data class LoggedInUser(
    val idUser: Int,
    val hash: String
)