package com.sola.anime.ai.generator.common.ui.sheet.photo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.extension.startCrop
import com.sola.anime.ai.generator.data.db.query.PhotoStorageDao
import com.sola.anime.ai.generator.databinding.ItemPhotoBinding
import com.sola.anime.ai.generator.databinding.SheetPhotoBinding
import com.sola.anime.ai.generator.domain.model.PhotoStorage
import com.sola.anime.ai.generator.domain.model.Ratio
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class SheetPhoto: LsBottomSheet<SheetPhotoBinding>(SheetPhotoBinding::inflate) {

    companion object {
        private const val CROP_RESULT = 1
    }

    @Inject lateinit var photoAdapter: PhotoAdapter
    @Inject lateinit var photoStorageDao: PhotoStorageDao

    private lateinit var pickPhoto: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePhoto: ActivityResultLauncher<Uri>
    private var uriTakePhoto: Uri? = null
    val clicks: Subject<PhotoType.Photo> = PublishSubject.create()
    var strength: Float = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                activity?.startCrop(fragment = this, uri = uri, requestCode = CROP_RESULT)
            }
        }
        takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            uriTakePhoto?.takeIf { success }?.let { uriTakePhoto ->
                activity?.startCrop(fragment = this, uri = uriTakePhoto, requestCode = CROP_RESULT)
            }
        }
    }

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun initData() {
        photoStorageDao.getAllLive().observe(viewLifecycleOwner){
            binding.confirm.isVisible = false
            binding.cancel.isVisible = false

             val data = arrayListOf(
                PhotoType.ChoosePhoto,
                PhotoType.ChooseCamera,
                PhotoType.Photo(R.drawable.preview_photo_1),
                PhotoType.Photo(R.drawable.preview_photo_2),
                PhotoType.Photo(R.drawable.preview_photo_3),
                PhotoType.Photo(R.drawable.preview_photo_4),
                PhotoType.Photo(R.drawable.preview_photo_5),
                PhotoType.Photo(R.drawable.preview_photo_6),
                PhotoType.Photo(R.drawable.preview_photo_7)
            ).apply {
                addAll(it.map { PhotoType.Photo(photoStorage = it) })
             }

            photoAdapter.canDelete = false
            photoAdapter.data = data
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == CROP_RESULT && resultCode == Activity.RESULT_OK -> {
                data?.data?.let { uri ->
                    val ratioDisplay = tryOrNull { Ratio.values().find { it.ratio == data.getStringExtra("ratioDisplay") } } ?: Ratio.Ratio1x1

                    lifecycleScope.launch(Dispatchers.Main){
                        delay(500)

                        photoStorageDao.inserts(PhotoStorage(uriString = uri.toString(), ratio = ratioDisplay))

                        delay(500)

                        tryOrNull {
                            binding.recyclerView.post {
                                binding.recyclerView.smoothScrollToPosition(photoAdapter.data.lastIndex)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun listenerView() {
        binding.slider.setListener { _, currentValue ->
            strength = currentValue
        }
        binding.delete.clicks {
            binding.cancel.isVisible = true
            binding.delete.isVisible = false

            photoAdapter.canDelete = true
        }
        binding.cancel.clicks {
            binding.cancel.isVisible = false
            binding.delete.isVisible = true
            binding.confirm.isVisible = false

            photoAdapter.canDelete = false
        }
        binding.confirm.clicks {
           val imageStorage = photoAdapter.itemsChoiceDelete.map { photoAdapter.data[it] }
               .filterIsInstance<PhotoType.Photo>()
               .mapNotNull { it.photoStorage }

            photoStorageDao.deletes(*imageStorage.toTypedArray())
        }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        photoAdapter
            .subjectDeleteChanges
            .autoDispose(scope())
            .subscribe {
                binding.confirm.isVisible = photoAdapter.itemsChoiceDelete.isNotEmpty()
            }

        photoAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { photoType ->
                when (photoType) {
                    is PhotoType.Photo -> {
                        photoAdapter.photo = photoType

                        clicks.onNext(photoType)
                    }
                    is PhotoType.ChooseCamera -> {
                        launchTakePhoto()
                    }
                    is PhotoType.ChoosePhoto -> {
                        launchPickPhoto()
                    }
                }
            }
    }

    private fun launchPickPhoto() {
        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun launchTakePhoto() {
        activity?.let { activity ->
            val file = File(activity.filesDir, "${System.currentTimeMillis()}.png")
            uriTakePhoto = FileProvider.getUriForFile(activity,  "${activity.packageName}.provider", file)
            takePhoto.launch(uriTakePhoto)
        }
    }

    private fun initView(){
        binding.recyclerView.apply {
            this.layoutManager = GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
            this.adapter = photoAdapter
        }
        binding.slider.currentValue = strength
    }

    class PhotoAdapter @Inject constructor(): LsAdapter<PhotoType, ItemPhotoBinding>(ItemPhotoBinding::inflate) {

        val clicks: Subject<PhotoType> = PublishSubject.create()
        val subjectDeleteChanges: Subject<Unit> = PublishSubject.create()
        var canDelete: Boolean = false
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value

                itemsChoiceDelete.clear()

                notifyDataSetChanged()
            }
        val itemsChoiceDelete = arrayListOf<Int>()
        var photo: PhotoType? = null
            set(value) {
                if (field == value){
                    return
                }

                if (value == null){
                    val oldIndex = data.indexOf(field)

                    field = null

                    notifyItemChanged(oldIndex)
                    return
                }

                data.indexOf(field).takeIf { it != -1 }?.let { notifyItemChanged(it) }
                data.indexOf(value).takeIf { it != -1 }?.let { notifyItemChanged(it) }

                field = value
            }

        override fun bindItem(item: PhotoType, binding: ItemPhotoBinding, position: Int) {
            binding.photo.isVisible = item == PhotoType.ChoosePhoto
            binding.camera.isVisible = item == PhotoType.ChooseCamera
            binding.preview.isVisible = item is PhotoType.Photo
            binding.imageCheck.isVisible = canDelete && item is PhotoType.Photo && item.photoStorage != null
            binding.viewSelected.isVisible = when {
                canDelete -> false
                else -> photo == item
            }
            binding.viewRatio.isVisible = item is PhotoType.Photo

            val isChecked = itemsChoiceDelete.contains(position)
            binding.imageCheck.setImageResource(if (isChecked) R.drawable.ic_check_2 else R.drawable.ic_uncheck)

            when (item){
                is PhotoType.Photo -> {
                    when {
                        item.preview != null -> {
                            binding.preview.setImageResource(item.preview)
                            binding.ratio.text = "1:1"
                        }
                        item.photoStorage != null -> {
                            binding.ratio.text = item.photoStorage.ratio.display

                            Glide.with(binding.root.context)
                                .load(item.photoStorage.uriString)
                                .error(R.drawable.place_holder_image)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.preview)
                        }
                    }
                }
                else -> {}
            }

            binding.viewClicks.clicks {
                when {
                    canDelete && item is PhotoType.Photo && item.photoStorage != null -> {
                        when {
                            itemsChoiceDelete.contains(position) -> itemsChoiceDelete.remove(position)
                            else -> itemsChoiceDelete.add(position)
                        }

                        val newChecked = itemsChoiceDelete.contains(position)
                        binding.imageCheck.setImageResource(if (newChecked) R.drawable.ic_check_2 else R.drawable.ic_uncheck)

                        subjectDeleteChanges.onNext(Unit)
                    }
                    !canDelete -> clicks.onNext(item)
                }
            }
        }
    }

    sealed class PhotoType {
        object ChoosePhoto: PhotoType()
        object ChooseCamera: PhotoType()
        data class Photo(val preview: Int? = null, val photoStorage: PhotoStorage? = null): PhotoType()
    }

}