package com.sola.anime.ai.generator.common.extension

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.feature.art.ArtActivity
import com.sola.anime.ai.generator.feature.credit.CreditActivity
import com.sola.anime.ai.generator.feature.crop.CropActivity
import com.sola.anime.ai.generator.feature.detailExplore.DetailExploreActivity
import com.sola.anime.ai.generator.feature.detailModelOrLoRA.DetailModelOrLoRAActivity
import com.sola.anime.ai.generator.feature.first.FirstActivity
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.openad.OpenAdActivity
import com.sola.anime.ai.generator.feature.pickAvatar.PickAvatarActivity
import com.sola.anime.ai.generator.feature.preview.PreviewActivity
import com.sola.anime.ai.generator.feature.processing.art.ArtProcessingActivity
import com.sola.anime.ai.generator.feature.processing.avatar.AvatarProcessingActivity
import com.sola.anime.ai.generator.feature.processing.batch.BatchProcessingActivity
import com.sola.anime.ai.generator.feature.result.art.ArtResultActivity
import com.sola.anime.ai.generator.feature.search.SearchActivity
import com.sola.anime.ai.generator.feature.setting.SettingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

fun AppCompatActivity.showFullChanges(viewLoadingAds: View, task: () -> Unit) {
    lifecycleScope.launch(Dispatchers.Main) {
        viewLoadingAds.isVisible = true
        delay(1000L)
        App.app.fullScreenChanges.showAdIfAvailable(this@showFullChanges, getString(R.string.key_full_screen_changes)) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewLoadingAds.isVisible = false
                task()
            }
        }
    }
}

fun AppCompatActivity.startMain(viewLoadingAds: View, isFull: Boolean, afterDoTask: () -> Unit){
    val task = {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        afterDoTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
}

fun AppCompatActivity.startSetting(viewLoadingAds: View, isFull: Boolean, doAfterTask: () -> Unit){
    val task = {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        doAfterTask()
    }
    task()
}

fun AppCompatActivity.startSearch(viewLoadingAds: View, isFull: Boolean, doAfterTask: () -> Unit){
    val task = {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        doAfterTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
}

fun AppCompatActivity.startDetailModelOrLoRA(viewLoadingAds: View, modelId: Long = -1, loRAGroupId: Long = -1, loRAId: Long = -1, loRAPReviewIndex: Int = 0, isFull: Boolean, afterDoTask: () -> Unit){
    val task = {
        val intent = Intent(this, DetailModelOrLoRAActivity::class.java)
        intent.putExtra(DetailModelOrLoRAActivity.MODEL_ID_EXTRA, modelId)
        intent.putExtra(DetailModelOrLoRAActivity.LORA_GROUP_ID_EXTRA, loRAGroupId)
        intent.putExtra(DetailModelOrLoRAActivity.LORA_ID_EXTRA, loRAId)
        intent.putExtra(DetailModelOrLoRAActivity.LORA_PREVIEW_INDEX_EXTRA, loRAPReviewIndex)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        afterDoTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
}

fun AppCompatActivity.startDetailExplore(viewLoadingAds: View, exploreId: Long, previewIndex: Int = 0, isFull: Boolean, afterDoTask: () -> Unit){
    val task = {
        val intent = Intent(this, DetailExploreActivity::class.java)
        intent.putExtra(DetailExploreActivity.EXPLORE_ID_EXTRA, exploreId)
        intent.putExtra(DetailExploreActivity.EXPLORE_PREVIEW_INDEX_EXTRA, previewIndex)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        afterDoTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
}

fun AppCompatActivity.startPickAvatar(viewLoadingAds: View, isFull: Boolean, afterDoTask: () -> Unit){
    val task = {
        val intent = Intent(this, PickAvatarActivity::class.java)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        afterDoTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
}

fun Activity.startCrop(fragment: Fragment, uri: Uri, requestCode: Int){
    val intent = Intent(this, CropActivity::class.java)
    intent.data = uri
    fragment.startActivityForResult(intent, requestCode)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startCredit(){
    val intent = Intent(this, CreditActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
}

fun Activity.startOpenAd(){
    val intent = Intent(this, OpenAdActivity::class.java)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
}

fun Activity.startIap(isKill: Boolean = true){
    Timber.tag("MainXXXXXX").e("Start IAP")
    val intent = Intent(this, IapActivity::class.java)
    intent.putExtra(IapActivity.IS_KILL_EXTRA, isKill)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
}

fun AppCompatActivity.startArt(viewLoadingAds: View, modelId: Long = -1, loRAGroupId: Long = -1, loRAId: Long = -1, exploreId: Long? = null, isFull: Boolean, afterDoTask: () -> Unit){
    val task = {
        val intent = Intent(this, ArtActivity::class.java)
        intent.putExtra(ArtActivity.MODEL_ID_EXTRA, modelId)
        intent.putExtra(ArtActivity.LORA_GROUP_ID_EXTRA, loRAGroupId)
        intent.putExtra(ArtActivity.LORA_ID_EXTRA, loRAId)
        intent.putExtra(ArtActivity.EXPLORE_ID_EXTRA, exploreId)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_up, R.anim.nothing) }
        afterDoTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
}

fun Activity.startArtProcessing(totalCreditsDeducted: Float, creditsPerImage: Float, isRewarded: Boolean){
    val intent = Intent(this, ArtProcessingActivity::class.java)
    intent.putExtra("totalCreditsDeducted", totalCreditsDeducted)
    intent.putExtra("creditsPerImage", creditsPerImage)
    intent.putExtra("isRewarded", isRewarded)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startBatchProcessing(totalCreditsDeducted: Float, creditsPerImage: Float){
    val intent = Intent(this, BatchProcessingActivity::class.java)
    intent.putExtra("totalCreditsDeducted", totalCreditsDeducted)
    intent.putExtra("creditsPerImage", creditsPerImage)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun Activity.startAvatarProcessing(totalCreditsDeducted: Float, creditsPerImage: Float){
    val intent = Intent(this, AvatarProcessingActivity::class.java)
    intent.putExtra("totalCreditsDeducted", totalCreditsDeducted)
    intent.putExtra("creditsPerImage", creditsPerImage)
    startActivity(intent)
    tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
}

fun AppCompatActivity.startArtResult(viewLoadingAds: View, historyId: Long, childHistoryIndex: Int = -1, isGallery: Boolean, isFull: Boolean, afterDoTask: () -> Unit){
    val task = {
        val intent = Intent(this, ArtResultActivity::class.java)
        intent.putExtra(ArtResultActivity.HISTORY_ID_EXTRA, historyId)
        intent.putExtra(ArtResultActivity.CHILD_HISTORY_INDEX_EXTRA, childHistoryIndex)
        intent.putExtra(ArtResultActivity.IS_GALLERY_EXTRA, isGallery)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        afterDoTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
}

fun AppCompatActivity.startPreview(viewLoadingAds: View, historyId: Long, childHistoryIndex: Int = -1, isFull: Boolean, afterDoTask: () -> Unit){
    val task = {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra(ArtResultActivity.HISTORY_ID_EXTRA, historyId)
        intent.putExtra(ArtResultActivity.CHILD_HISTORY_INDEX_EXTRA, childHistoryIndex)
        startActivity(intent)
        tryOrNull { overridePendingTransition(R.anim.slide_in_left, R.anim.nothing) }
        afterDoTask()
    }
    when {
        !App.app.prefs.isUpgraded.get() && isNetworkAvailable() && App.app.configApp.isFullScreenChanges && App.app.fullScreenChanges.isAdAvailable() && isFull -> {
            showFullChanges(viewLoadingAds) { task() }
        }
        else -> task()
    }
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