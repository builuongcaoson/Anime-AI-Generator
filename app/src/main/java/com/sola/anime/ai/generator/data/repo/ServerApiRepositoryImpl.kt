package com.sola.anime.ai.generator.data.repo

import android.content.Context
import androidx.core.text.isDigitsOnly
import com.basic.common.extension.tryOrNull
import com.google.gson.Gson
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.server.Message
import com.sola.anime.ai.generator.domain.model.server.UserPremium
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.inject.server.ServerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerApiRepositoryImpl @Inject constructor(
    private val context: Context,
    private val serverApi: ServerApi,
    private val prefs: Preferences,
    private val analyticManager: AnalyticManager
): ServerApiRepository {

    override suspend fun syncUser(appUserId: String, success: (UserPremium?) -> Unit) {
        Timber.tag("Main11111").e("##### SYNC USER #####")

        val json = withContext(Dispatchers.IO) {
            try {
                serverApi.syncUser(appUserId.toRequestBody())
            } catch (e: Exception){
                e.printStackTrace()
                null
            }
        } ?: run {
            prefs.userPremium.delete()
            prefs.isSyncUserPurchased.delete()
            prefs.isSyncUserPurchasedFailed.delete()

            analyticManager.logEvent(AnalyticManager.TYPE.SYNC_USER_PREMIUM_FAILED)

            success(null)
            return
        }
        val gson = Gson()
        val userPremium = tryOrNull { gson.fromJson(json, UserPremium::class.java) }
        when {
            userPremium?.id != null -> {
                Timber.tag("Main11111").e("Id: ${userPremium.id}")
                Timber.tag("Main11111").e("AppUserId: ${userPremium.appUserId}")
                Timber.tag("Main11111").e("isUpgraded: ${userPremium.isUpgraded}")
                Timber.tag("Main11111").e("timePurchased: ${userPremium.timePurchased}")
                Timber.tag("Main11111").e("timeExpired: ${userPremium.timeExpired}")
                Timber.tag("Main11111").e("numberCreatedArtworkInDay: ${userPremium.numberCreatedArtworkInDay}")
                Timber.tag("Main11111").e("totalNumberCreatedArtwork: ${userPremium.totalNumberCreatedArtwork}")
                Timber.tag("Main11111").e("latestTimeCreatedArtwork: ${userPremium.latestTimeCreatedArtwork}")

                prefs.numberCreatedArtwork.set(userPremium.numberCreatedArtworkInDay.takeIf { it.isNotEmpty() && it.isDigitsOnly() }?.toLong() ?: 0)
                prefs.totalNumberCreatedArtwork.set(userPremium.totalNumberCreatedArtwork.takeIf { it.isNotEmpty() && it.isDigitsOnly() }?.toLong() ?: 0)
                prefs.latestTimeCreatedArtwork.set(userPremium.latestTimeCreatedArtwork.takeIf { it.isNotEmpty() && it.isDigitsOnly() }?.toLong() ?: -1)

                prefs.userPremium.set(gson.toJson(userPremium))
                prefs.isSyncUserPurchased.set(true)
                prefs.isSyncUserPurchasedFailed.set(false)

                success(userPremium)
            }
            else -> {
                prefs.userPremium.delete()
                prefs.isSyncUserPurchased.delete()
                prefs.isSyncUserPurchasedFailed.delete()

                analyticManager.logEvent(AnalyticManager.TYPE.SYNC_USER_PREMIUM_FAILED)

                success(null)
            }
        }
        Timber.tag("Main11111").e("AppUserId: $appUserId")
        Timber.tag("Main11111").e("User premium: ${userPremium?.appUserId}")
    }

    override suspend fun insertUserPremium(
        appUserId: String,
        timePurchased: String,
        timeExpired: String,
        success: () -> Unit
    ) {
        val json = withContext(Dispatchers.IO) {
            try {
                serverApi.insertUserPremium(appUserId = appUserId.toRequestBody(), timePurchased = timePurchased.toRequestBody(), timeExpired = timeExpired.toRequestBody())
            } catch (e: Exception){
                e.printStackTrace()
                null
            }
        } ?: run {
            prefs.userPremium.delete()
            prefs.isSyncUserPurchased.delete()
            prefs.isSyncUserPurchasedFailed.delete()

            success()
            return
        }
        val message = tryOrNull { Gson().fromJson(json, Message::class.java) }
        when {
            message?.message != null && message.message == "Insert success" -> {
                val userPremium = UserPremium().apply {
                    this.id = "-1"
                    this.appUserId = appUserId
                    this.timePurchased = timePurchased
                    this.timeExpired = timeExpired
                    this.numberCreatedArtworkInDay = "0"
                    this.totalNumberCreatedArtwork = "0"
                    this.latestTimeCreatedArtwork = "0"
                }

                prefs.numberCreatedArtwork.delete()

                prefs.userPremium.set(Gson().toJson(userPremium))
                prefs.isSyncUserPurchased.set(true)
                prefs.isSyncUserPurchasedFailed.set(false)

                success()
            }
            else -> {
                prefs.userPremium.delete()
                prefs.isSyncUserPurchased.delete()
                prefs.isSyncUserPurchasedFailed.delete()

                success()
            }
        }
        Timber.tag("Main11111").e("##### INSERT USER PREMIUM #####")
        Timber.tag("Main11111").e("Message: $message")
    }

    override suspend fun updateCreatedArtworkInDay() {
        val userPremium = when {
            prefs.userPremium.get().isNotEmpty() -> tryOrNull { Gson().fromJson(prefs.userPremium.get(), UserPremium::class.java) }
            else -> null
        }
        Timber.tag("Main12345").e("AppUserId: ${userPremium?.appUserId}")
        userPremium?.let {
            val json = withContext(Dispatchers.IO) {
                try {
                    val numberCreated = prefs.numberCreatedArtworkInDayFailed.get().toString()
                    serverApi.updateCreatedArtworkInDay(appUserId = userPremium.appUserId.toRequestBody(), numberCreated = numberCreated.toRequestBody())
                } catch (e: Exception){
                    e.printStackTrace()
                    null
                }
            } ?: run {
                prefs.numberCreatedArtworkInDayFailed.set(prefs.numberCreatedArtworkInDayFailed.get() + 1)
                return
            }
            val message = tryOrNull { Gson().fromJson(json, Message::class.java) }
            when {
                message?.message != null && message.message == "Update success" -> {
                    prefs.numberCreatedArtworkInDayFailed.delete()
                }
                else -> {
                    prefs.numberCreatedArtworkInDayFailed.set(prefs.numberCreatedArtworkInDayFailed.get() + 1)
                }
            }

            Timber.tag("Main11111").e("##### UPDATE USER PREMIUM #####")
            Timber.tag("Main11111").e("Message: $message")
        }
    }


}