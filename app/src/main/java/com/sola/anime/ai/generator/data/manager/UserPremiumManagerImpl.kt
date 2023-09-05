package com.sola.anime.ai.generator.data.manager

import android.content.Context
import com.basic.common.extension.makeToast
import com.basic.common.extension.tryOrNull
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.getDeviceId
import com.sola.anime.ai.generator.common.extension.getDeviceModel
import com.sola.anime.ai.generator.common.extension.getTimeFormatted
import com.sola.anime.ai.generator.common.extension.toDate
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.manager.UserPremiumManager
import com.sola.anime.ai.generator.domain.model.config.userPurchased.ProductPurchased
import com.sola.anime.ai.generator.domain.model.config.userPurchased.UserPurchased
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPremiumManagerImpl @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
): UserPremiumManager {

    override suspend fun addOrUpdatePurchasedToDatabase(packagePurchased: String, timePurchased: Long, timeExpired: Long): UserPurchased? = withContext(Dispatchers.IO){
        val reference = Firebase.database.reference.child("usersPurchased").child(context.getDeviceId())
        val snapshot = reference.get().await()
        val userPurchased = tryOrNull { snapshot.getValue(UserPurchased::class.java) } ?: UserPurchased()

        userPurchased.deviceId = context.getDeviceId()
        userPurchased.deviceModel = getDeviceModel()
        userPurchased.credits = prefs.getCredits()
        userPurchased.numberCreatedArtwork = when {
            packagePurchased.contains(Constraint.Iap.SKU_LIFE_TIME) -> 0
            packagePurchased.contains(Constraint.Iap.SKU_WEEK) -> 0
            packagePurchased.contains(Constraint.Iap.SKU_WEEK_3D_TRIAl) -> 0
            packagePurchased.contains(Constraint.Iap.SKU_MONTH) -> 0
            packagePurchased.contains(Constraint.Iap.SKU_YEAR) -> 0
            else -> prefs.numberCreatedArtwork.get()
        }
        userPurchased.latestTimeCreatedArtwork = if (prefs.latestTimeCreatedArtwork.isSet) prefs.latestTimeCreatedArtwork.get().getTimeFormatted() else ""
        userPurchased.productsPurchased.add(
            ProductPurchased().apply {
                this.packagePurchased = packagePurchased
                this.timePurchased = timePurchased.getTimeFormatted()
                this.timeExpired = when {
                    packagePurchased.contains(Constraint.Iap.SKU_LIFE_TIME) -> "-2"
                    packagePurchased.contains(Constraint.Iap.SKU_WEEK) -> timeExpired.getTimeFormatted()
                    packagePurchased.contains(Constraint.Iap.SKU_WEEK_3D_TRIAl) -> timeExpired.getTimeFormatted()
                    packagePurchased.contains(Constraint.Iap.SKU_MONTH) -> timeExpired.getTimeFormatted()
                    packagePurchased.contains(Constraint.Iap.SKU_YEAR) -> timeExpired.getTimeFormatted()
                    else -> "-3"
                }
            }
        )

        reference.setValue(userPurchased).await()

        userPurchased
    }

    override suspend fun syncUserPurchasedFromDatabase() = withContext(Dispatchers.IO) {
        val reference = Firebase.database.reference.child("usersPurchased").child(context.getDeviceId())
        val snapshot = reference.get().await()
        val userPurchased = tryOrNull { snapshot.getValue(UserPurchased::class.java) }

        prefs.setUserPurchased(userPurchased)

        userPurchased?.let {
            Timber.e("User purchased: ${Gson().toJson(userPurchased)}")

            prefs.setCredits(userPurchased.credits)
            prefs.numberCreatedArtwork.set(userPurchased.numberCreatedArtwork)
            prefs.latestTimeCreatedArtwork.set(userPurchased.latestTimeCreatedArtwork.toDate()?.time ?: -1)

            when {
                userPurchased.productsPurchased.filterNotNull().any { it.packagePurchased.contains(Constraint.Iap.SKU_LIFE_TIME) } -> {
                    prefs.isUpgraded.set(true)
                    prefs.timeExpiredPremium.set(-2L)
                }
                userPurchased.productsPurchased.filterNotNull().any { it.packagePurchased.contains(Constraint.Iap.SKU_WEEK) || it.packagePurchased.contains(Constraint.Iap.SKU_YEAR) } -> {
                    val dateExpired = userPurchased.productsPurchased.filterNotNull().maxBy { it.timeExpired.toDate()?.time ?: 0 }.timeExpired.toDate()
                    when {
                        dateExpired != null && dateExpired.time - System.currentTimeMillis() >= 0 -> {
                            prefs.isUpgraded.set(true)
                            prefs.timeExpiredPremium.set(dateExpired.time)
                        }
                        else -> {
                            prefs.isUpgraded.delete()
                            prefs.timeExpiredPremium.delete()
                        }
                    }
                }
                else -> {
                    prefs.isUpgraded.delete()
                    prefs.timeExpiredPremium.delete()
                }
            }
        }

        Unit
    }

    override suspend fun createdArtwork() {

    }
}