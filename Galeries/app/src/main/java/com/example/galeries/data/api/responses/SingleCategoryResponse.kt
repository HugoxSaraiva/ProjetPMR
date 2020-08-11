package com.example.galeries.data.api.responses

data class SingleCategoryResponse(
    val categorie : CategoryAdded
)

data class CategoryAdded(
    val id : Int,
    var titre : String,
    var coupdecoeur : Int
)