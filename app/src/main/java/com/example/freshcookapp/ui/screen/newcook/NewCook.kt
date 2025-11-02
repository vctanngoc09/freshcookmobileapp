package com.example.freshcookapp.ui.screen.newcook

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.component.UnderlineTextField
import com.example.freshcookapp.ui.theme.Cinnabar100
import com.example.freshcookapp.ui.theme.Cinnabar200
import com.example.freshcookapp.ui.theme.Cinnabar400
import com.example.freshcookapp.ui.theme.Cinnabar50
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.White

@Composable
fun NewCook(onBackClick: () -> Unit){
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf(mutableListOf("250g bột", "100ml nước")) }
    var steps by remember { mutableStateOf(mutableListOf("Trộn bột và nước đến khi đặc lại", "Đậy kín hỗn hợp lại và để 1 tiếng")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                Button(
                    onClick = { /* Lưu */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,   // nền nhạt như ảnh
                        contentColor = Color.Black            // chữ đen
                    ),
                    shape = RoundedCornerShape(10.dp), // bo tròn đều (hình viên thuốc)
                    border = BorderStroke(1.dp, Cinnabar500), // viền đỏ
                    contentPadding = PaddingValues(
                        horizontal = 16.dp, // giảm padding ngang (mặc định 24.dp)
                        vertical = 6.dp     // giảm padding dọc (mặc định 12.dp)
                    )
                ) {
                    Text(
                        text = "Lưu",
                        style = MaterialTheme.typography.labelLarge
                    )
                }


                Button(
                    onClick = { /* Lưu */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cinnabar500,   // nền nhạt như ảnh
                        contentColor = White            // chữ đen
                    ),
                    shape = RoundedCornerShape(10.dp), // bo tròn đều (hình viên thuốc)
                    contentPadding = PaddingValues(
                        horizontal = 16.dp, // giảm padding ngang (mặc định 24.dp)
                        vertical = 6.dp     // giảm padding dọc (mặc định 12.dp)
                    )
                ) {
                    Text(
                        text = "Lên sóng",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }


        }

        Spacer(modifier = Modifier.height(10.dp))


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            /** ẢNH MÓN ĂN */
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth() // chiếm hết chiều ngang của vùng cha thực tế (bỏ qua padding)
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Cinnabar100),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera),
                            contentDescription = "Camera",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            "Đăng tải hình đại diện món ăn",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }


            /** TÊN MÓN + MÔ TẢ + THỜI GIAN */
            item {
                ScreenContainer {

                    // --- Ô “Tên món” to hơn ---
                    Spacer(Modifier.height(16.dp))
                    UnderlineTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        placeholder = "Tên món: Món canh bí ngon nhất",
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        placeholderStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    )

                    // --- Ô mô tả bình thường ---
                    Spacer(Modifier.height(16.dp))
                    UnderlineTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Hãy chia sẻ với mọi người về món này của bạn nhé. " +
                                "Ai hay điều gì đã truyền cảm hứng cho bạn nấu nó? " +
                                "Tại sao nó đặc biệt? Bạn thích thưởng thức nó theo cách nào?"
                    )

                    // --- Ô thời gian nấu bình thường ---
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Thời gian nấu",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        UnderlineTextField(
                            value = cookTime,
                            onValueChange = { cookTime = it },
                            placeholder = "1 tiếng 30 phút",
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
            }


            /** NGUYÊN LIỆU */
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Nguyên Liệu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            itemsIndexed(ingredients) { index, item ->
                OutlinedTextField(
                    value = item,
                    onValueChange = { new -> ingredients[index] = new },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    trailingIcon = {
                        IconButton(onClick = { ingredients.removeAt(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa")
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                TextButton(
                    onClick = { ingredients.add("") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Thêm nguyên liệu")
                }
            }

            /** CÁCH LÀM */
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Cách Làm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(steps) { index, step ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text("${index + 1}.", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = step,
                        onValueChange = { new -> steps[index] = new },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF6F6F6))
                            .clickable { /* chọn ảnh minh họa bước */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            item {
                TextButton(
                    onClick = { steps.add("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Thêm bước")
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}