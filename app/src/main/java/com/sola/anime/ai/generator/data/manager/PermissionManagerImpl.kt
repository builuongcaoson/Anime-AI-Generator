package com.sola.anime.ai.generator.data.manager

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sola.anime.ai.generator.domain.manager.PermissionManager
import javax.inject.Inject

class PermissionManagerImpl @Inject constructor(
    private val context: Context
) : PermissionManager {

    private fun hasPermissions(vararg permissions: String): Boolean {
        var hasPermission = true
        permissions.forEach {
            val granted =
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                hasPermission = false
                return@forEach
            }
        }
        return hasPermission
    }

    override fun hasOverlay(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> Settings.canDrawOverlays(context)
            else -> true
        }
    }

    override fun hasStorage(): Boolean {
        return hasPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun requestStorage(activity: Activity, resultCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), resultCode)
    }

    override fun requestStorage(fragment: Fragment, resultCode: Int) {
        fragment.requestPermissions(arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), resultCode)
    }

}