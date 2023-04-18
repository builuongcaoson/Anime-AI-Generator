package com.sola.anime.ai.generator.common

import android.content.Context
import android.content.Intent
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.processing.art.ArtProcessingActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(
    private val context: Context
){

    private fun startActivity(intent: Intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun startMain(){
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
    }

    fun startIap(){
        val intent = Intent(context, IapActivity::class.java)
        startActivity(intent)
    }

    fun startArtProcessing(){
        val intent = Intent(context, ArtProcessingActivity::class.java)
        startActivity(intent)
    }

}