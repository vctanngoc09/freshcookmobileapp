package com.example.freshcookapp.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Helper để lấy Facebook Key Hash cho Development
 * Sử dụng trong MainActivity để log ra Key Hash
 */
object FacebookKeyHashHelper {

    fun printHashKey(context: Context) {
        try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            // Lấy signatures từ API phù hợp
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }

            // Kiểm tra null trước khi dùng for loop
            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), Base64.DEFAULT))
                Log.d("FACEBOOK_KEY_HASH", "Key Hash: $hashKey")
                println("==============================================")
                println("FACEBOOK KEY HASH: $hashKey")
                println("==============================================")
            } ?: Log.e("FACEBOOK_KEY_HASH", "No signatures found")

        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("FACEBOOK_KEY_HASH", "Error getting package info", e)
        } catch (e: NoSuchAlgorithmException) {
            Log.e("FACEBOOK_KEY_HASH", "Error generating hash", e)
        }
    }
}
