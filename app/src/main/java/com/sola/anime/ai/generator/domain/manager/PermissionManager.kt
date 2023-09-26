package com.sola.anime.ai.generator.domain.manager

import android.app.Activity
import androidx.fragment.app.Fragment

interface PermissionManager {

    fun hasOverlay(): Boolean

    fun hasStorage(): Boolean

    fun requestStorage(activity: Activity, resultCode: Int)

    fun requestStorage(fragment: Fragment, resultCode: Int)

    fun hasNotification(): Boolean

    fun requestNotification(activity: Activity, requestCode: Int)


}