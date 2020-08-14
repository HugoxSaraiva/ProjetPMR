package com.example.galeries.data.model

data class GalleryImage(
    val id: Int,
    val idUser: Int,
    var legende: String = "",
    val url: String,
    var isLiked : Boolean = false
)