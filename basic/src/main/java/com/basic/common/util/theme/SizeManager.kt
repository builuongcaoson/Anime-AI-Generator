package com.basic.common.util.theme

import android.content.Context
import com.intuit.ssp.R
import com.basic.common.extension.getDimens
import com.basic.data.LsPrefs

object SizeManager {

    private const val SIZE_TOOLBAR = 0
    private const val SIZE_TITLE = 1
    private const val SIZE_PRIMARY = 2
    private const val SIZE_SECONDARY = 3
    private const val SIZE_TERTIARY = 4

    fun sizeById(context: Context, textSize: Int, id: Int) = when (textSize) {
        LsPrefs.TEXT_SIZE_SMALL -> when (id) {
            SIZE_TOOLBAR -> context.getDimens(R.dimen._19ssp)
            SIZE_TITLE -> context.getDimens(R.dimen._14ssp)
            SIZE_PRIMARY -> context.getDimens(R.dimen._11ssp)
            SIZE_SECONDARY -> context.getDimens(R.dimen._9ssp)
            SIZE_TERTIARY -> context.getDimens(R.dimen._8ssp)
            else -> context.getDimens(R.dimen._11ssp)
        }
        LsPrefs.TEXT_SIZE_NORMAL -> when (id) {
            SIZE_TOOLBAR -> context.getDimens(R.dimen._20ssp)
            SIZE_TITLE -> context.getDimens(R.dimen._15ssp)
            SIZE_PRIMARY -> context.getDimens(R.dimen._12ssp)
            SIZE_SECONDARY -> context.getDimens(R.dimen._10ssp)
            SIZE_TERTIARY -> context.getDimens(R.dimen._9ssp)
            else -> context.getDimens(R.dimen._12ssp)
        }
        LsPrefs.TEXT_SIZE_LARGE -> when (id) {
            SIZE_TOOLBAR -> context.getDimens(R.dimen._21ssp)
            SIZE_TITLE -> context.getDimens(R.dimen._16ssp)
            SIZE_PRIMARY -> context.getDimens(R.dimen._13ssp)
            SIZE_SECONDARY -> context.getDimens(R.dimen._11ssp)
            SIZE_TERTIARY -> context.getDimens(R.dimen._10ssp)
            else -> context.getDimens(R.dimen._13ssp)
        }
        LsPrefs.TEXT_SIZE_LARGER -> when (id) {
            SIZE_TOOLBAR -> context.getDimens(R.dimen._24ssp)
            SIZE_TITLE -> context.getDimens(R.dimen._17ssp)
            SIZE_PRIMARY -> context.getDimens(R.dimen._14ssp)
            SIZE_SECONDARY -> context.getDimens(R.dimen._12ssp)
            SIZE_TERTIARY -> context.getDimens(R.dimen._11ssp)
            else -> context.getDimens(R.dimen._14ssp)
        }
        else -> null
    }

}