package com.user.smartledgerai.data

import androidx.room.TypeConverter

class ClassConverter {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value:String): TransactionType{
        return enumValueOf<TransactionType>(value)
    }
}