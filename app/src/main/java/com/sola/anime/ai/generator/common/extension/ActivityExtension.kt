package com.sola.anime.ai.generator.common.extension

import android.app.Activity
import android.content.Intent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.processing.art.ArtProcessingActivity
import com.sola.anime.ai.generator.feature.result.art.ArtResultActivity

fun Activity.startMain(){
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) }
}

fun Activity.startIap(){
    val intent = Intent(this, IapActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) }
}

fun Activity.startArtProcessing(){
    val intent = Intent(this, ArtProcessingActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) }
}

fun Activity.startArtResult(){
    val intent = Intent(this, ArtResultActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) }
}

fun Activity.back(){
    finish()
    tryOrNull { overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) }
}