package com.basic.common.extension

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.WindowManager
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager

fun Activity.transparent(isLightStatusBar: Boolean = false, isLightNavigationBar: Boolean = false) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    if (isLightStatusBar){
        lightStatusBar()
    } else darkStatusBar()

    if (isLightNavigationBar){
        lightNavigationBar()
    } else darkNavigationBar()
}

fun Activity.transparent() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT
}

fun Activity.lightStatusBar() {
    var flags: Int = window.decorView.systemUiVisibility // get current flag
    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // add LIGHT_STATUS_BAR to flag
    window.decorView.systemUiVisibility = flags
}

fun Activity.lightNavigationBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        var flags: Int = window.decorView.systemUiVisibility // get current flag
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR // add LIGHT_STATUS_BAR to flag
        window.decorView.systemUiVisibility = flags
    }
}

fun Activity.darkStatusBar() {
    var flags = window.decorView.systemUiVisibility // get current flag
    flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // use XOR here for remove LIGHT_STATUS_BAR from flags
    window.decorView.systemUiVisibility = flags
}

fun Activity.darkNavigationBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        var flags = window.decorView.systemUiVisibility // get current flag
        flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR // use XOR here for remove LIGHT_STATUS_BAR from flags
        window.decorView.systemUiVisibility = flags
    }
}

private fun Activity.setWindowFlag(bits: Int, on: Boolean = false) {
    val win = window
    val winParams = win.attributes
    if (on) {
        winParams.flags = winParams.flags or bits
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    win.attributes = winParams
}

fun Activity.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(currentFocus, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}


