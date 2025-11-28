package com.example.freshcookapp.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.White
import com.example.freshcookapp.ui.theme.WorkSans

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // THÊM THAM SỐ NÀY
) {
    Button(
        onClick = onClick,
        enabled = enabled, // TRUYỀN THAM SỐ VÀO ĐÂY
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Cinnabar500,
            contentColor = White,
            // Thêm màu cho trạng thái bị vô hiệu hóa để dễ nhận biết
            disabledContainerColor = Cinnabar500.copy(alpha = 0.5f),
            disabledContentColor = White.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(
            horizontal = 10.dp,
            vertical = 12.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            fontFamily = WorkSans,
            textAlign = TextAlign.Center
        )
    }
}
