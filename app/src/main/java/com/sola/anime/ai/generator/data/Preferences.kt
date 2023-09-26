package com.sola.anime.ai.generator.data

import android.net.Uri
import com.basic.common.extension.tryOrNull
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.util.AESEncyption
import com.sola.anime.ai.generator.domain.model.config.userPurchased.UserPurchased
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(
    private val rxPrefs: RxSharedPreferences
) {

    companion object {
        const val STEP_DEFAULT = "45"
        const val STEP_PREMIUM = "50"
        const val MAX_NUMBER_CREATE_ARTWORK = 0L
        const val MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY = 20L
        const val MAX_SECOND_GENERATE_ART = 60
    }

    // Config
    val isUpgraded = rxPrefs.getBoolean("isUpgraded", false)
    val timeExpiredPremium = rxPrefs.getLong("timeExpiredPremium", -1) // Milliseconds
    private val userPurchased = rxPrefs.getString("userPurchased_v2", "")
    val userPurchasedChanges = rxPrefs.getLong("userPurchased_v2_changes", -1)

    // For App
    val versionExplore = rxPrefs.getLong("versionExplore", 0)
    val versionLoRA = rxPrefs.getLong("versionLoRA", 0)
    val versionIap = rxPrefs.getLong("versionIap", 0)
    val versionProcess = rxPrefs.getLong("versionProcess", 0)
    val versionStyle = rxPrefs.getLong("versionStyle", 0)
    val versionModel = rxPrefs.getLong("versionModel", 0)
    val isShowedWaringPremiumDialog = rxPrefs.getBoolean("isShowedWaringPremiumDialog", false)
    val isFirstTime = rxPrefs.getBoolean("isFirstTime", true)
    val numberDownloadedOriginal = rxPrefs.getLong("numberDownloadedOriginal", 0)
    val numberCreatedArtwork = rxPrefs.getLong("numberCreatedArtwork", 0)
    val totalNumberCreatedArtwork = rxPrefs.getLong("totalNumberCreatedArtwork", 0)
    val latestTimeCreatedArtwork = rxPrefs.getLong("latestTimeCreatedArt", -1)
    val isPurchasedCredit = rxPrefs.getBoolean("isPurchasedCredit", false)
    private val credits = rxPrefs.getString("credits", "")
    private val credits2 = rxPrefs.getFloat("credits2", 0f)
    val creditsChanges = rxPrefs.getLong("creditsChanges", -1)
    val isFirstPurchaseCredits10000 = rxPrefs.getBoolean("isFirstPurchaseCredits10000", true)
    val purchasedOrderLastedId = rxPrefs.getString("purchasedOrderLastedId", "null")

    fun isShowFeatureDialog(version: Long): Preference<Boolean> {
        return rxPrefs.getBoolean("isShowFeatureDialog_$version", false)
    }

    fun getUserPurchased(): UserPurchased? {
        val valueUserPurchased = userPurchased.get()
        return when {
            valueUserPurchased.isNotEmpty() -> tryOrNull { Gson().fromJson(AESEncyption.decrypt(valueUserPurchased), UserPurchased::class.java) }
            else -> null
        }
    }

    fun setUserPurchased(userPurchased: UserPurchased?) {
        when (userPurchased) {
            null -> this.userPurchased.delete()
            else -> {
                AESEncyption.encrypt(Gson().toJson(userPurchased).toString())?.let { encryptUserPurchased ->
                    this.userPurchased.set(encryptUserPurchased)
                } ?: run {
                    this.userPurchased.set(Gson().toJson(userPurchased))
                }
            }
        }

        userPurchasedChanges.set(userPurchasedChanges.get() + 1)
    }

    fun setCredits(newCredits: Float) {
        AESEncyption.encrypt(newCredits.toString())?.let { encryptCredits ->
            credits.set(encryptCredits)
        } ?: run {
            credits2.set(newCredits)
        }
        creditsChanges.set(creditsChanges.get() + 1)
    }

    fun getCredits(): Float {
        val valueCredits = credits.get()
        return when {
            valueCredits.isNotEmpty() -> {
                tryOrNull { AESEncyption.decrypt(valueCredits)?.toFloat() } ?: credits2.get()
            }
            else -> 0f
        }
    }

    fun hadFaceWithUri(uri: Uri): Boolean {
        val urisHadFacePrefs = rxPrefs.getString("urisHadFace", "")
        val urisHadFace = when {
            urisHadFacePrefs.get().isEmpty() -> arrayListOf()
            else -> tryOrNull { Gson().fromJson(urisHadFacePrefs.get(), object : TypeToken<List<String>>() {}.type) } ?: arrayListOf<String>()
        }
        return urisHadFace.contains(uri.toString())
    }

    fun saveFaceWithUri(uri: Uri){
        val urisHadFacePrefs = rxPrefs.getString("urisHadFace", "")
        val urisHadFace = when {
            urisHadFacePrefs.get().isEmpty() -> arrayListOf()
            else -> tryOrNull { Gson().fromJson(urisHadFacePrefs.get(), object : TypeToken<List<String>>() {}.type) } ?: arrayListOf<String>()
        }
        if (!urisHadFace.contains(uri.toString())){
            urisHadFace.add(uri.toString())
        }
        urisHadFacePrefs.set(Gson().toJson(urisHadFace))
    }

}
