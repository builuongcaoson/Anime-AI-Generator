package com.sola.anime.ai.generator.common

import android.content.Context
import android.net.Uri
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigApp @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
) {

    var stepDefault = Preferences.STEP_DEFAULT
    var stepPremium = Preferences.STEP_PREMIUM
    var maxNumberGenerateFree = Preferences.MAX_NUMBER_CREATE_ARTWORK
    var maxNumberGeneratePremium = Preferences.MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY
    var feature = context.getString(R.string.default_feature)
    var version = BuildConfig.VERSION_CODE.toLong()
    var versionExplore = prefs.versionExplore.get()
    var versionLoRA = prefs.versionLoRA.get()
    var versionIap = prefs.versionIap.get()
    var versionProcess = prefs.versionProcess.get()
    var versionStyle = prefs.versionStyle.get()
    var versionModel = prefs.versionModel.get()
    var keyDezgo = Constraint.Dezgo.KEY
    var keyDezgoPremium = Constraint.Dezgo.KEY_PREMIUM
    var blockDeviceIds = listOf("")
    var blockedRoot = true

    // Art & Batch
    var resPhoto: Int? = null
        set(value) {
            field = value
            subjectUriPhotoChanges.onNext(Unit)
        }
    var pairUriPhoto: Pair<Uri, Ratio>? = null
        set(value) {
            field = value
            subjectUriPhotoChanges.onNext(Unit)
        }
    var dezgoBodiesTextsToImages: List<DezgoBodyTextToImage> = listOf()
    var dezgoBodiesImagesToImages: List<DezgoBodyImageToImage> = listOf()
    var discountCreditArt: Int = 0 // For tab art
    var discountCreditBatch: Int = 10 // For tab batch
    var discountCreditAvatar: Int = 0 // For avatar
    var creditsRemaining = prefs.getCredits()

    // RxJava
    var subjectUriPhotoChanges: Subject<Unit> = BehaviorSubject.createDefault(Unit)
    var subjectRatioClicks: Subject<Ratio> = BehaviorSubject.createDefault(Ratio.Ratio1x1)
    var subjectExploreClicks: Subject<Long> = BehaviorSubject.createDefault(-1)

}