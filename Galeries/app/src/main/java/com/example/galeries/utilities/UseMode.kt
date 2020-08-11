package com.example.galeries.utilities

import com.example.galeries.R

enum class UseMode(val value: Int) {
    LIST(0),
    EDIT(1),
    SHOW(2)
}

fun getMarginColor(idUser: Int?, otherId: Int): Int {
    return idUser?.let {
        if (it == otherId) {
            R.drawable.owner_border
        } else {
            R.drawable.foreign_border
        }
    } ?: R.drawable.foreign_border
}