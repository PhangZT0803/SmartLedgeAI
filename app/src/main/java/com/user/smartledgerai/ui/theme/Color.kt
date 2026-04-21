package com.user.smartledgerai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Light Theme Colors (Bright, Clean, Trustworthy)
// ==========================================
val PrimaryLight = Color(0xFF4F46E5) // Electric Indigo (Brand & Action)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEEF2FF) // Soft highlight for AI cards
val OnPrimaryContainerLight = Color(0xFF312E81)

val SecondaryLight = Color(0xFF14B8A6) // Mint Teal (Secondary actions/badges)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCCFBF1)
val OnSecondaryContainerLight = Color(0xFF134E4A)

val TertiaryLight = Color(0xFFF59E0B) // Amber/Gold (AI Warnings, Match Rates)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFEF3C7)
val OnTertiaryContainerLight = Color(0xFF78350F)

val BackgroundLight = Color(0xFFF8FAFC) // Slate 50 (Slightly cooler than pure white)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF) // Pure white for cards to pop
val OnSurfaceLight = Color(0xFF1E293B)
val SurfaceVariantLight = Color(0xFFF1F5F9) // Card outlines/dividers
val OnSurfaceVariantLight = Color(0xFF64748B)

val OutlineLight = Color(0xFFCBD5E1)

// ==========================================
// Dark Theme Colors (Deep, Tech, Immersive)
// ==========================================
val PrimaryDark = Color(0xFF818CF8) // Lighter Indigo for dark mode visibility
val OnPrimaryDark = Color(0xFFFFFFFF)
val PrimaryContainerDark = Color(0xFF3730A3)
val OnPrimaryContainerDark = Color(0xFFE0E7FF)

val SecondaryDark = Color(0xFF2DD4BF)
val OnSecondaryDark = Color(0xFF042F2E)
val SecondaryContainerDark = Color(0xFF115E59)
val OnSecondaryContainerDark = Color(0xFFCCFBF1)

val TertiaryDark = Color(0xFFFBBF24)
val OnTertiaryDark = Color(0xFF451A03)
val TertiaryContainerDark = Color(0xFF78350F)
val OnTertiaryContainerDark = Color(0xFFFEF3C7)

val BackgroundDark = Color(0xFF0F172A) // Deep Night Black
val OnBackgroundDark = Color(0xFFF8FAFC)
val SurfaceDark = Color(0xFF1E293B) // Slightly elevated for cards
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF334155)
val OnSurfaceVariantDark = Color(0xFF94A3B8)

val OutlineDark = Color(0xFF475569)

// ==========================================
// Semantic Colors (Essential for Ledger App)
// ==========================================
// Usage: Do not put these in MaterialTheme, call them directly for +/- amounts
val ExpenseColorLight = Color(0xFFEF4444) // Rose Red
val ExpenseColorDark = Color(0xFFF87171)
val IncomeColorLight = Color(0xFF10B981)  // Emerald Green
val IncomeColorDark = Color(0xFF34D399)

// --- 新增：AI 特色渐变 (Gemini Signature) ---
// 用于仪表盘背景或 AI 识别高光
val AiGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF14B8A6))
)

// --- 新增：毛玻璃/光感辅助色 ---
val GlassWhite = Color(0xCCFFFFFF)
val GlassBlack = Color(0x990F172A)

// --- 语义化颜色强化 ---
val ExpenseColor = Color(0xFFF43F5E) // 更鲜艳的玫瑰红
val IncomeColor = Color(0xFF10B981)  // 翠绿色