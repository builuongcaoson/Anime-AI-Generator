package com.sola.anime.ai.generator.common.extension

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.basic.common.extension.tryOrNull
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.feature.explore.ExploreActivity
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.processing.art.ArtProcessingActivity
import com.sola.anime.ai.generator.feature.result.art.ArtResultActivity
import com.sola.anime.ai.generator.feature.style.StyleActivity

fun Activity.startMain(){
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startIap(){
    val intent = Intent(this, IapActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
}

fun Activity.startArtProcessing(){
    val intent = Intent(this, ArtProcessingActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startArtResult(){
    val intent = Intent(this, ArtResultActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startExplore(){
    val intent = Intent(this, ExploreActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startStyle(){
    val intent = Intent(this, StyleActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.back(){
    finish()
    tryOrNull { overridePendingTransition(R.anim.nothing, R.anim.slide_out_left) }
}

fun Activity.backTopToBottom(){
    finish()
    tryOrNull { overridePendingTransition(R.anim.nothing, R.anim.slide_down) }
}