package com.example.freshcookapp.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar400

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Tìm kiếm",
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // 👇 Box chứa cả placeholder và text nhập
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerTextField() // ✅ chỗ nhập text thật
                    }
                }
            }
        )


        // Icon filter tách riêng nằm bên phải
        if (onFilterClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter),
                    contentDescription = "Filter",
                    modifier = Modifier.size(22.dp),
                    tint = Color.Black
                )
            }
        }
    }
}