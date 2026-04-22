package com.user.smartledgerai.data.model

import androidx.room.TypeConverter
import androidx.compose.ui.graphics.vector.ImageVector

class Converters {
    @TypeConverter
    fun fromImageVector(vector: ImageVector): String {
        return IconMapper.getName(vector)
    }

    @TypeConverter
    fun toImageVector(name: String): ImageVector {
        return IconMapper.getIcon(name)
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(name: String): TransactionType {
        return TransactionType.valueOf(name)
    }
}
