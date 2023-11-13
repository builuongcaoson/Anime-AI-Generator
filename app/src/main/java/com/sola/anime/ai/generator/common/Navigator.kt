package com.sola.anime.ai.generator.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.basic.common.extension.makeToast
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Navigator @Inject constructor(
    private val context: Context,
    private val analyticManager: AnalyticManager,
    private val prefs: Preferences,
){

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun startActivityExternal(intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
    }

    fun showPrivacy() {
        try {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                    .apply {
                        data = Uri.parse(Constraint.Info.PRIVACY_URL)
                    }
            startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constraint.Info.PRIVACY_URL))
                startActivity(intent)
            } catch (_: Exception){
                context.makeToast("Device not support, please check again!")
            }
        }
    }

    fun showTerms() {
        try {
            val intent =
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                    .apply {
                        data = Uri.parse(Constraint.Info.TERMS_URL)
                    }
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constraint.Info.TERMS_URL))
                startActivity(intent)
            } catch (_: Exception){
                context.makeToast("Device not support, please check again!")
            }
        }
    }

    fun showSupport() {
        analyticManager.logEvent(AnalyticManager.TYPE.CLICKED_SUPPORT)

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constraint.Info.MAIL_SUPPORT))
        intent.putExtra(Intent.EXTRA_SUBJECT, "${context.getString(R.string.app_name)} Support")
        intent.putExtra(
            Intent.EXTRA_TEXT, StringBuilder()
                .append("--- Please write your message above this line ---\n\n")
                .append("Package: ${context.packageName}\n")
                .append("Version: ${BuildConfig.VERSION_NAME}\n")
                .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
                .append("SDK: ${Build.VERSION.SDK_INT}\n")
                .append("Upgraded: ${prefs.isUpgraded()}")
                .toString()
        )
        startActivityExternal(intent)
    }

    fun showReportModel(modelId: Long) {
        analyticManager.logEvent(AnalyticManager.TYPE.REPORT_MODEL)

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constraint.Info.MAIL_SUPPORT))
        intent.putExtra(Intent.EXTRA_SUBJECT, "${context.getString(R.string.app_name)} Support")
        intent.putExtra(
            Intent.EXTRA_TEXT, StringBuilder()
                .append("--- Report model ---\n\n")
                .append("Id: $modelId\n")
                .append("Reason: ")
                .toString()
        )
        startActivityExternal(intent)
    }

    fun showReportLoRA(loRAGroupId: Long, loRAId: Long) {
        analyticManager.logEvent(AnalyticManager.TYPE.REPORT_LORA)

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constraint.Info.MAIL_SUPPORT))
        intent.putExtra(Intent.EXTRA_SUBJECT, "${context.getString(R.string.app_name)} Support")
        intent.putExtra(
            Intent.EXTRA_TEXT, StringBuilder()
                .append("--- Report lora ---\n\n")
                .append("Group Id: $loRAGroupId\n")
                .append("Id: $loRAId\n")
                .append("Reason: ")
                .toString()
        )
        startActivityExternal(intent)
    }

    fun showReportExplore(exploreId: Long) {
        analyticManager.logEvent(AnalyticManager.TYPE.REPORT_EXPLORE)

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constraint.Info.MAIL_SUPPORT))
        intent.putExtra(Intent.EXTRA_SUBJECT, "${context.getString(R.string.app_name)} Support")
        intent.putExtra(
            Intent.EXTRA_TEXT, StringBuilder()
                .append("--- Report explore ---\n\n")
                .append("Id: $exploreId\n")
                .append("Reason: ")
                .toString()
        )
        startActivityExternal(intent)
    }

    fun showInvite() {
        analyticManager.logEvent(AnalyticManager.TYPE.CLICKED_INVITE)

        Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(
                Intent.EXTRA_TEXT,
                "http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            )
            .let { Intent.createChooser(it, null) }
            .let(::startActivityExternal)
    }

    fun showRating() {
        analyticManager.logEvent(AnalyticManager.TYPE.CLICKED_RATING)

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        )
            .addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )

        try {
            startActivityExternal(intent)
        } catch (e: ActivityNotFoundException) {
            val url = "http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            startActivityExternal(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

}