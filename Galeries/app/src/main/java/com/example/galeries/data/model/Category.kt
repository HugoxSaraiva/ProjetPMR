package com.example.galeries.data.model

import com.example.galeries.utilities.UseMode

data class Category (
    val id: Int,
    var titre: String,
    val idUser: Int,
    var useMode: UseMode = UseMode.LIST,
    var estCoupsDeCoeur: Boolean = false
)