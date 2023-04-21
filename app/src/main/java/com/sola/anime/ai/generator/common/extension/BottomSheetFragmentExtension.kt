package com.sola.anime.ai.generator.common.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun BottomSheetDialogFragment.show(activity: AppCompatActivity){
    show(activity.supportFragmentManager, tag)
}

fun BottomSheetDialogFragment.show(fragment: Fragment){
    show(fragment.childFragmentManager, tag)
}