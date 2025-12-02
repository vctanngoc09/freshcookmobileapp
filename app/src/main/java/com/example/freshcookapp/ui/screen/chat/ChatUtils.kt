package com.example.freshcookapp.ui.screen.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Định dạng thời gian tương đối (ví dụ: "2 giờ trước", "3 ngày trước")
 * Sử dụng trong danh sách chat để hiển thị thời gian tin nhắn cuối cùng
 */
fun formatRelativeTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Vừa xong"
        diff < 3_600_000 -> "${diff / 60_000} phút trước"
        diff < 86_400_000 -> "${diff / 3_600_000} giờ trước"
        diff < 172_800_000 -> "Hôm qua"
        diff < 604_800_000 -> "${diff / 86_400_000} ngày trước"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * Định dạng thời gian đầy đủ
 * Sử dụng trong màn hình chi tiết chat để hiển thị thời gian chính xác
 */
fun formatFullTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale("vi", "VN"))
    return formatter.format(date)
}
