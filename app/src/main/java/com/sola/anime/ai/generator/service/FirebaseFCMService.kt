package com.sola.anime.ai.generator.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sola.anime.ai.generator.domain.manager.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseFCMService : FirebaseMessagingService(){

    @Inject lateinit var notificationManager: NotificationManager

    override fun handleIntent(intent: Intent?) {
        super.handleIntent(intent)
        Timber.e("Handle Intent")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.e("onMessageReceived")

        val intent = message.toIntent()
        intent?.let {
            val data = intent.extras

            data?.let {
                when {
                    it.containsKey("gcm.notification.title") && it.containsKey("gcm.notification.body") -> {
                        val title = it.getString("gcm.notification.title") ?: return
                        val body = it.getString("gcm.notification.body") ?: return

                        notificationManager.notify(title, body)
                    }
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

}