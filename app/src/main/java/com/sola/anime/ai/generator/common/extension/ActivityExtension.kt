package com.sola.anime.ai.generator.common.extension

import android.app.Activity
import android.content.Intent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.feature.explore.ExploreActivity
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.preview.PreviewActivity
import com.sola.anime.ai.generator.feature.processing.art.ArtProcessingActivity
import com.sola.anime.ai.generator.feature.result.art.ArtResultActivity
import com.sola.anime.ai.generator.feature.setting.SettingActivity
import com.sola.anime.ai.generator.feature.style.StyleActivity

fun Activity.startMain(){
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startSetting(){
    val intent = Intent(this, SettingActivity::class.java)
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

fun Activity.startArtResult(historyId: Long, childHistoryIndex: Int = -1){
    val intent = Intent(this, ArtResultActivity::class.java)
    intent.putExtra(ArtResultActivity.HISTORY_ID_EXTRA, historyId)
    intent.putExtra(ArtResultActivity.CHILD_HISTORY_INDEX_EXTRA, childHistoryIndex)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startPreview(historyId: Long, childHistoryIndex: Int = -1){
    val intent = Intent(this, PreviewActivity::class.java)
    intent.putExtra(ArtResultActivity.HISTORY_ID_EXTRA, historyId)
    intent.putExtra(ArtResultActivity.CHILD_HISTORY_INDEX_EXTRA, childHistoryIndex)
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