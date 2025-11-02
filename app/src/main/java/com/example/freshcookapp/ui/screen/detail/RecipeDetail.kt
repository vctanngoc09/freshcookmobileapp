package com.example.freshcookapp.ui.screen.detail

// --- IMPORT CÁC THƯ VIỆN CẦN THIẾT ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freshcookapp.R
import com.example.freshcookapp.domain.model.* // Import tất cả model
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.ui.theme.FreshCookAppTheme

/**
 * HÀM "CỔNG VÀO" (ENTRY POINT)
 * (Giữ nguyên, không sửa)
 */
@Composable
fun RecipeDetail(
    recipeId: String?,
    navController: NavHostController
) {
    val recipeToShow = DemoData.findRecipeById(recipeId) ?: DemoData.allRecipes.first()

    // Gọi giao diện
    RecipeDetailView(
        recipe = recipeToShow,
        onBackClick = { navController.navigateUp() },
        onAuthorClick = { authorId ->
            navController.navigate("user_profile/$authorId")
        }
    )
}

/**
 * HÀM GIAO DIỆN (UI)
 * Dùng 'Box' để xếp chồng TopBar lên trên LazyColumn
 */
@Composable
private fun RecipeDetailView(
    recipe: Recipe,
    onBackClick: () -> Unit,
    onAuthorClick: (String) -> Unit
) {
    // Dùng Box làm gốc
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Nền trắng cho toàn màn hình
    ) {
        // 1. NỘI DUNG CUỘN (Nằm dưới cùng)
        RecipeDetailContent(
            recipe = recipe,
            modifier = Modifier.fillMaxSize(),
            onAuthorClick = onAuthorClick
        )

        // 2. THANH TOP BAR (Nổi ở trên)
        // Thêm 1 lớp gradient mờ ở trên cùng để TopBar dễ đọc hơn
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Chiều cao của lớp mờ
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )
        // Đặt TopBar vào
        RecipeDetailTopBar(
            onBackClick = onBackClick,
            onFavoriteClick = { /* TODO */ },
            onNotifyClick = { /* TODO */ },
            onMoreClick = { /* TODO */ }
        )

        // 3. KHÔNG CÓ BOTTOM BAR TÙY CHỈNH
        // Thanh Bottom Navigation chính (Trang chủ, Thêm...)
        // sẽ tự động hiển thị (do file MyAppNavigation quản lý).
    }
}

/**
 * Thanh TopBar (Nổi)
 * SỬA: Dùng icon màu trắng và nền mờ
 */
@Composable
private fun RecipeDetailTopBar(
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onNotifyClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // Quan trọng: né thanh status bar (pin, giờ)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nền mờ cho icon
        val iconBackgroundColor = Color.Black.copy(alpha = 0.3f)
        val iconColor = Color.White // Icon màu trắng

        // Nút quay lại
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.background(iconBackgroundColor, CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = iconColor)
        }

        // Cụm 3 nút bên phải
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.background(iconBackgroundColor, CircleShape)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Yêu thích", tint = iconColor)
            }
            IconButton(
                onClick = onNotifyClick,
                modifier = Modifier.background(iconBackgroundColor, CircleShape)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Thông báo", tint = iconColor)
            }
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.background(iconBackgroundColor, CircleShape)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Thêm", tint = iconColor)
            }
        }
    }
}

/**
 * Nội dung cuộn
 */
@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onAuthorClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        // 1. KHỐI OVERLAY (Ảnh + Thông tin)
        item {
            RecipeHeader(recipe = recipe)
        }

        // 2. NGUYÊN LIỆU (Giờ là item riêng, nền trắng)
        item {
            RecipeIngredients(ingredients = recipe.ingredients)
        }

        // 3. CÁCH LÀM
        item {
            RecipeInstructions(steps = recipe.instructions)
        }

        // 4. BÌNH LUẬN & TÁC GIẢ
        item {
            CommentsSection(
                author = recipe.author,
                onAuthorClick = onAuthorClick
            )
        }

        // 5. MÓN ĂN TƯƠNG TỰ
        item {
            RelatedRecipes(recipes = recipe.relatedRecipes)
        }

        // Thêm khoảng trống ở dưới cùng (RẤT QUAN TRỌNG)
        item {
            // Thêm 80.dp để nội dung cuộn không bị che bởi
            // thanh Bottom Navigation chính (Trang chủ, Thêm...)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * SỬA: Header (Ảnh + Thông tin Overlay)
 * Đây là thiết kế GỐC của bạn
 */
@Composable
private fun RecipeHeader(recipe: Recipe) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp) // Đặt chiều cao cố định cho ảnh
    ) {
        // Ảnh nền
        Image(
            painter = painterResource(id = recipe.imageRes),
            contentDescription = "Ảnh món ăn",
            modifier = Modifier.fillMaxSize(), // Ảnh tràn đầy
            contentScale = ContentScale.Crop
        )
        // Lớp phủ màu đen mờ (Gradient) ở dưới
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 500f // Gradient bắt đầu từ đâu
                    )
                )
        )

        // Cột chứa thông tin (nằm ở góc dưới)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tên món ăn
            Text(recipe.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            // Tác giả
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.avatar1), // Tạm
                    contentDescription = recipe.author.name,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(recipe.author.name, color = Color.White, fontSize = 14.sp)
            }

            // Hashtags
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                recipe.hashtags.forEach { tag ->
                    Text(
                        text = tag,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Thời gian nấu
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = "Thời gian nấu", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(recipe.time, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

// (Các hàm RecipeIngredients, RecipeInstructions, CommentsSection, RelatedRecipes
//  giữ nguyên như code "thân thiện" vì chúng nằm BÊN DƯỚI ảnh overlay)

@Composable
private fun RecipeIngredients(ingredients: List<String>) {
    // Thêm padding Top để nó tách khỏi ảnh
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp)) {
        Text("Nguyên Liệu", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ingredients.forEach { ingredient ->
            Text("• $ingredient", modifier = Modifier.padding(bottom = 8.dp), fontSize = 16.sp)
        }
    }
}

@Composable
private fun RecipeInstructions(steps: List<InstructionStep>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        Text("Cách làm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            steps.forEach { step ->
                InstructionStepItem(step = step)
            }
        }
    }
}

@Composable
private fun InstructionStepItem(step: InstructionStep) {
    Column {
        Text(
            "Bước ${step.stepNumber}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(step.description, fontSize = 16.sp, lineHeight = 24.sp)
        if (step.imageUrl != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = step.imageUrl),
                contentDescription = "Ảnh minh họa Bước ${step.stepNumber}",
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun CommentsSection(
    author: Author,
    onAuthorClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        // Card Tác giả (Giống Panel 1 figma)
        Surface(
            modifier = Modifier.clickable { onAuthorClick(author.id) },
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar1),
                        contentDescription = author.name,
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(author.name.replace("Bởi ", ""), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Tác giả", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Button(onClick = { /* TODO: Xử lý Follow */ }) {
                    Text("Follow")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Phần Bình luận (Giống Panel 3 figma)
        Text("Bình Luận", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chưa có bình luận nào.", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun RelatedRecipes(recipes: List<RecipePreview>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Các món tương tự",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recipes) { recipe ->
                RelatedRecipeItem(recipe = recipe)
            }
        }
    }
}

@Composable
private fun RelatedRecipeItem(recipe: RecipePreview) {
    Card(modifier = Modifier.width(180.dp)) {
        Column {
            Image(
                painter = painterResource(id = recipe.imageUrl),
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Text(
                text = recipe.title,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}