package com.sola.anime.ai.generator.common.extension

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.feature.art.ArtActivity
import com.sola.anime.ai.generator.feature.credit.CreditActivity
import com.sola.anime.ai.generator.feature.crop.CropActivity
import com.sola.anime.ai.generator.feature.detailExplore.DetailExploreActivity
import com.sola.anime.ai.generator.feature.detailModelOrLoRA.DetailModelOrLoRAActivity
import com.sola.anime.ai.generator.feature.first.FirstActivity
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.pickAvatar.PickAvatarActivity
import com.sola.anime.ai.generator.feature.preview.PreviewActivity
import com.sola.anime.ai.generator.feature.processing.art.ArtProcessingActivity
import com.sola.anime.ai.generator.feature.processing.avatar.AvatarProcessingActivity
import com.sola.anime.ai.generator.feature.processing.batch.BatchProcessingActivity
import com.sola.anime.ai.generator.feature.result.art.ArtResultActivity
import com.sola.anime.ai.generator.feature.search.SearchActivity
import com.sola.anime.ai.generator.feature.setting.SettingActivity

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

fun Activity.startSearch(){
    val intent = Intent(this, SearchActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startDetailModelOrLoRA(modelId: Long = -1, loRAGroupId: Long = -1, loRAId: Long = -1, loRAPReviewIndex: Int = 0){
    val intent = Intent(this, DetailModelOrLoRAActivity::class.java)
    intent.putExtra(DetailModelOrLoRAActivity.MODEL_ID_EXTRA, modelId)
    intent.putExtra(DetailModelOrLoRAActivity.LORA_GROUP_ID_EXTRA, loRAGroupId)
    intent.putExtra(DetailModelOrLoRAActivity.LORA_ID_EXTRA, loRAId)
    intent.putExtra(DetailModelOrLoRAActivity.LORA_PREVIEW_INDEX_EXTRA, loRAPReviewIndex)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startDetailExplore(exploreId: Long, previewIndex: Int = 0){
    val intent = Intent(this, DetailExploreActivity::class.java)
    intent.putExtra(DetailExploreActivity.EXPLORE_ID_EXTRA, exploreId)
    intent.putExtra(DetailExploreActivity.EXPLORE_PREVIEW_INDEX_EXTRA, previewIndex)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startPickAvatar(){
    val intent = Intent(this, PickAvatarActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startCrop(fragment: Fragment, uri: Uri, requestCode: Int){
    val intent = Intent(this, CropActivity::class.java)
    intent.data = uri
    fragment.startActivityForResult(intent, requestCode)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startCredit(isKill: Boolean = true){
    val intent = Intent(this, CreditActivity::class.java)
    intent.putExtra(CreditActivity.IS_KILL_EXTRA, isKill)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
}

fun Activity.startIap(isKill: Boolean = true){
    val intent = Intent(this, IapActivity::class.java)
    intent.putExtra(IapActivity.IS_KILL_EXTRA, isKill)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
}

fun Activity.startArt(modelId: Long = -1, loRAGroupId: Long = -1, loRAId: Long = -1, exploreId: Long? = null){
    val intent = Intent(this, ArtActivity::class.java)
    intent.putExtra(ArtActivity.MODEL_ID_EXTRA, modelId)
    intent.putExtra(ArtActivity.LORA_GROUP_ID_EXTRA, loRAGroupId)
    intent.putExtra(ArtActivity.LORA_ID_EXTRA, loRAId)
    intent.putExtra(ArtActivity.EXPLORE_ID_EXTRA, exploreId)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
}

fun Activity.startArtProcessing(totalCreditsDeducted: Float, creditsPerImage: Float){
    val intent = Intent(this, ArtProcessingActivity::class.java)
    intent.putExtra("totalCreditsDeducted", totalCreditsDeducted)
    intent.putExtra("creditsPerImage", creditsPerImage)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startBatchProcessing(creditsPerImage: Float){
    val intent = Intent(this, BatchProcessingActivity::class.java)
    intent.putExtra("creditsPerImage", creditsPerImage)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startAvatarProcessing(creditsPerImage: Float){
    val intent = Intent(this, AvatarProcessingActivity::class.java)
    intent.putExtra("creditsPerImage", creditsPerImage)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startArtResult(historyId: Long, childHistoryIndex: Int = -1, isGallery: Boolean){
    val intent = Intent(this, ArtResultActivity::class.java)
    intent.putExtra(ArtResultActivity.HISTORY_ID_EXTRA, historyId)
    intent.putExtra(ArtResultActivity.CHILD_HISTORY_INDEX_EXTRA, childHistoryIndex)
    intent.putExtra(ArtResultActivity.IS_GALLERY_EXTRA, isGallery)
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

fun Activity.startFirst(){
    val intent = Intent(this, FirstActivity::class.java)
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