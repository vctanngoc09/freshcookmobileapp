package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNotificationClick: () -> Unit = {},
    onMyDishesClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onFollowerClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ScreenContainer {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Cinnabar500
                    )
                }

                Text(
                    text = "Tài khoản",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = WorkSans,
                    color = Cinnabar500
                )

                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Cinnabar500
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Profile Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .clickable { onEditProfileClick() }
                        ) {
                            // Placeholder - replace with actual image
                            Image(
                                painter = painterResource(id = R.drawable.avatar1),
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Vo Cao Tan Ngoc",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WorkSans,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "HCM, Viet Nam",
                            fontSize = 14.sp,
                            fontFamily = WorkSans,
                            color = Color.Gray
                        )
                    }
                }

                // Stats Bar with Red Background
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Cinnabar500)
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(count = "24", label = "Follower", onClick = onFollowerClick)

                        Divider(
                            modifier = Modifier
                                .width(2.dp)
                                .height(50.dp),
                            color = Color.White
                        )

                        StatItem(count = "18", label = "Món", onClick = {})

                        Divider(
                            modifier = Modifier
                                .width(2.dp)
                                .height(50.dp),
                            color = Color.White
                        )

                        StatItem(count = "121", label = "Thích", onClick = onFollowingClick)
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Xem gần đây button (Outlined)
                        OutlinedButton(
                            onClick = onRecentlyViewedClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Cinnabar500
                            ),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Cinnabar500),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "Xem gần đây",
                                fontSize = 15.sp,
                                fontFamily = WorkSans,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Món món tui button (Filled)
                        Button(
                            onClick = onMyDishesClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cinnabar500
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "Món món tui",
                                fontSize = 15.sp,
                                fontFamily = WorkSans,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // "Xem tất cả" link
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Xem tất cả",
                            fontSize = 14.sp,
                            fontFamily = WorkSans,
                            color = Cinnabar500,
                            fontWeight = FontWeight.Medium,
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            ),
                            modifier = Modifier.clickable { onMyDishesClick() }
                        )
                    }
                }

                // Recipe Grid
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(700.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(4) { index ->
                            RecipeCard()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = count,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = WorkSans,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = WorkSans,
            color = Color.White
        )
    }
}

@Composable
fun RecipeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // Recipe Image
            Image(
                painter = painterResource(id = R.drawable.img_food1),
                contentDescription = "Recipe",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Favorite Icon
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = Cinnabar500,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
            )

            // Recipe Info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Honey pancakes with...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = WorkSans,
                    color = Color.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "30 min",
                            fontSize = 12.sp,
                            fontFamily = WorkSans,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "Easy",
                        fontSize = 12.sp,
                        fontFamily = WorkSans,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
