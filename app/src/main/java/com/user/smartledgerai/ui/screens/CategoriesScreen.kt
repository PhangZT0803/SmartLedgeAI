package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoriesScreen() {
    // 使用 Column 将元素垂直排列
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // 给整个屏幕添加一些外边距
    ) {
        // Spending 文本
        Text(
            text = "Spending",
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // ----------------(横线)
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp
        )

        // Income 文本
        Text(
            text = "Income",
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // ----------------(横线)
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp
        )
    }
}

@Preview
@Composable
fun CategoriesScreenPreview() {
    CategoriesScreen()
}