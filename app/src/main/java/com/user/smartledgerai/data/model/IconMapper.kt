package com.user.smartledgerai.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            "Fastfood" -> Icons.Default.Fastfood
            "LocalCafe" -> Icons.Default.LocalCafe
            "Coffee" -> Icons.Default.Coffee
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "AccountBalance" -> Icons.Default.AccountBalance
            "LocalGroceryStore" -> Icons.Default.LocalGroceryStore
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "Work" -> Icons.Default.Work
            else -> Icons.Default.ReceiptLong
        }
    }

    fun getName(icon: ImageVector): String {
        return icon.name.substringAfterLast(".")
    }
}
