package com.user.smartledgerai.ai

import com.user.smartledgerai.data.model.PendingTransactionEntity
import com.user.smartledgerai.data.model.TransactionType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIParsingEngine @Inject constructor() {

    /**
     * Simulates parsing a raw string from a notification or receipt.
     */
    fun parseRawData(raw: String): PendingTransactionEntity {
        val amount = extractAmount(raw)
        val (title, category, iconName) = analyzeSource(raw)
        
        return PendingTransactionEntity(
            id = UUID.randomUUID().toString(),
            rawSource = raw,
            title = title,
            amount = amount,
            category = category,
            date = "Oct 21, 10:59 PM",
            timestamp = System.currentTimeMillis(),
            type = TransactionType.EXPENSE,
            iconName = iconName,
            matchPercentage = (85..99).random()
        )
    }

    private fun extractAmount(raw: String): Double {
        val regex = Regex("""(?:RM|MYR)\s?(\d+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        val match = regex.find(raw)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 25.00
    }

    private fun analyzeSource(raw: String): Triple<String, String, String> {
        val upper = raw.uppercase()
        
        return when {
            upper.contains("GRAB") -> Triple("Grab", "Transport", "DirectionsCar")
            upper.contains("STARBUCKS") -> Triple("Starbucks", "Drinks", "LocalCafe")
            upper.contains("MCD") || upper.contains("MCDONALDS") -> Triple("McDonald's", "Food", "Fastfood")
            upper.contains("SHELL") || upper.contains("PETRON") -> Triple("Petrol", "Transport", "LocalGasStation")
            upper.contains("UNQLO") -> Triple("Uniqlo", "Shopping", "ShoppingBag")
            upper.contains("ZARA") -> Triple("Zara", "Shopping", "ShoppingBag")
            upper.contains("AEON") -> Triple("Aeon Mall", "Food", "LocalGroceryStore")
            upper.contains("COCO") -> Triple("CoCo Tea", "Drinks", "LocalCafe")
            else -> Triple("Unknown Merchant", "Other", "Receipt")
        }
    }
}
