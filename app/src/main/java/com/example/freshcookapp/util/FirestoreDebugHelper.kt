package com.example.freshcookapp.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * 🔥 FIRESTORE DEBUG HELPER
 * Giúp kiểm tra kết nối và permissions của Firestore
 */
object FirestoreDebugHelper {

    private const val TAG = "FirestoreDebug"

    data class DebugResult(
        val isSuccess: Boolean,
        val message: String,
        val errorType: String? = null,
        val suggestion: String? = null
    )

    /**
     * Kiểm tra xem user đã đăng nhập chưa
     */
    fun checkAuthentication(): DebugResult {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) {
            DebugResult(
                isSuccess = true,
                message = "✅ Đã đăng nhập: ${user.email ?: user.uid}"
            )
        } else {
            DebugResult(
                isSuccess = false,
                message = "❌ Chưa đăng nhập",
                errorType = "UNAUTHENTICATED",
                suggestion = "Vui lòng đăng nhập lại"
            )
        }
    }

    /**
     * Test đọc collection recipes từ Firestore
     */
    suspend fun testRecipesAccess(): DebugResult {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("recipes")
                .limit(1)
                .get()
                .await()

            DebugResult(
                isSuccess = true,
                message = "✅ Đọc recipes thành công (${snapshot.size()} documents)"
            )
        } catch (e: Exception) {
            handleFirestoreError(e, "recipes")
        }
    }

    /**
     * Test đọc collection categories từ Firestore
     */
    suspend fun testCategoriesAccess(): DebugResult {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("categories")
                .limit(1)
                .get()
                .await()

            DebugResult(
                isSuccess = true,
                message = "✅ Đọc categories thành công (${snapshot.size()} documents)"
            )
        } catch (e: Exception) {
            handleFirestoreError(e, "categories")
        }
    }

    /**
     * Test đọc collection users từ Firestore
     */
    suspend fun testUsersAccess(): DebugResult {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: return DebugResult(
                    isSuccess = false,
                    message = "❌ Không có user ID",
                    errorType = "NO_USER",
                    suggestion = "Đăng nhập lại"
                )

            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            DebugResult(
                isSuccess = true,
                message = "✅ Đọc user profile thành công: ${snapshot.getString("name")}"
            )
        } catch (e: Exception) {
            handleFirestoreError(e, "users")
        }
    }

    /**
     * Chạy tất cả các test
     */
    suspend fun runAllTests(): List<Pair<String, DebugResult>> {
        val results = mutableListOf<Pair<String, DebugResult>>()

        // 1. Check Authentication
        results.add("Authentication" to checkAuthentication())

        // 2. Test Recipes
        results.add("Recipes Collection" to testRecipesAccess())

        // 3. Test Categories
        results.add("Categories Collection" to testCategoriesAccess())

        // 4. Test Users
        results.add("Users Collection" to testUsersAccess())

        return results
    }

    /**
     * Xử lý lỗi Firestore và đưa ra gợi ý
     */
    private fun handleFirestoreError(e: Exception, collectionName: String): DebugResult {
        Log.e(TAG, "Lỗi khi truy cập $collectionName", e)

        val errorMessage = e.message ?: "Unknown error"

        return when {
            errorMessage.contains("PERMISSION_DENIED", ignoreCase = true) -> {
                DebugResult(
                    isSuccess = false,
                    message = "❌ PERMISSION_DENIED: Không có quyền đọc '$collectionName'",
                    errorType = "PERMISSION_DENIED",
                    suggestion = """
                        🔧 Cách sửa:
                        1. Vào Firebase Console: https://console.firebase.google.com/
                        2. Chọn project: freshcookapp-b376c
                        3. Vào Firestore Database → Rules
                        4. Thêm rules: allow read: if request.auth != null;
                        5. Click Publish
                        6. Chờ 5 giây và thử lại
                        
                        📖 Xem chi tiết: FIRESTORE_RULES_FIX.md
                    """.trimIndent()
                )
            }
            errorMessage.contains("UNAVAILABLE", ignoreCase = true) -> {
                DebugResult(
                    isSuccess = false,
                    message = "❌ UNAVAILABLE: Không thể kết nối Firestore",
                    errorType = "UNAVAILABLE",
                    suggestion = "Kiểm tra kết nối internet và thử lại"
                )
            }
            errorMessage.contains("NOT_FOUND", ignoreCase = true) -> {
                DebugResult(
                    isSuccess = false,
                    message = "❌ NOT_FOUND: Collection '$collectionName' không tồn tại",
                    errorType = "NOT_FOUND",
                    suggestion = "Kiểm tra xem collection đã được tạo trên Firestore chưa"
                )
            }
            errorMessage.contains("UNAUTHENTICATED", ignoreCase = true) -> {
                DebugResult(
                    isSuccess = false,
                    message = "❌ UNAUTHENTICATED: Chưa đăng nhập",
                    errorType = "UNAUTHENTICATED",
                    suggestion = "Đăng xuất và đăng nhập lại"
                )
            }
            else -> {
                DebugResult(
                    isSuccess = false,
                    message = "❌ Lỗi không xác định: ${errorMessage.take(100)}",
                    errorType = "UNKNOWN",
                    suggestion = "Kiểm tra Logcat để biết thêm chi tiết"
                )
            }
        }
    }

    /**
     * Log tất cả thông tin debug
     */
    suspend fun logDebugInfo() {
        Log.d(TAG, "========== FIRESTORE DEBUG INFO ==========")

        val results = runAllTests()
        results.forEach { (testName, result) ->
            Log.d(TAG, "[$testName] ${result.message}")
            if (!result.isSuccess) {
                Log.e(TAG, "  Error Type: ${result.errorType}")
                Log.e(TAG, "  Suggestion: ${result.suggestion}")
            }
        }

        Log.d(TAG, "==========================================")
    }
}

