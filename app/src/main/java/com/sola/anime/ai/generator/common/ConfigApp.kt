package com.sola.anime.ai.generator.common

import android.content.Context
import android.net.Uri
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.util.AESEncyption
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigApp @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
) {

    var scriptIap = "0" // 0: (Nothing), 1: (LifeTime - 3 Day Trial Week - Year), 2: (Lifetime - Month - Year)
    var stepDefault = Preferences.STEP_DEFAULT
    var stepPremium = Preferences.STEP_PREMIUM
    var maxNumberGenerateFree = Preferences.MAX_NUMBER_CREATE_ARTWORK
    var maxNumberGeneratePremium = Preferences.MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY
    var feature = context.getString(R.string.default_feature)
    var version = BuildConfig.VERSION_CODE.toLong()
    var versionExplore = prefs.versionExplore.get()
    var versionIap = prefs.versionIap.get()
    var versionProcess = prefs.versionProcess.get()
    var versionStyle = prefs.versionStyle.get()
    val decryptKey by lazy {
        when {
            !BuildConfig.DEBUG || BuildConfig.SCRIPT -> AESEncyption.decrypt(Constraint.Dezgo.KEY) ?: ""
            else -> AESEncyption.decrypt(Constraint.Dezgo.RAPID_KEY) ?: ""
        }
    }

    val sensitiveKeywords = context.getStringArray(R.array.sensitives)
    var styleChoice: Style? = null

    // Art & Batch
    var resPhoto: Int? = null
        set(value) {
            field = value
            subjectUriPhotoChanges.onNext(Unit)
        }
    var uriPhoto: Uri? = null
        set(value) {
            field = value
            subjectUriPhotoChanges.onNext(Unit)
        }
    var dezgoBodiesTextsToImages: List<DezgoBodyTextToImage> = listOf()
    var discountCredit: Int = 10 // For tab batch

    // RxJava
    var subjectUriPhotoChanges: Subject<Unit> = BehaviorSubject.createDefault(Unit)
    var subjectRatioClicks: Subject<Ratio> = BehaviorSubject.createDefault(Ratio.Ratio1x1)
    var subjectExploreClicks: Subject<Long> = BehaviorSubject.createDefault(-1)

}