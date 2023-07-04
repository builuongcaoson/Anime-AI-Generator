package com.sola.anime.ai.generator.common.ui.sheet.photo

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.data.db.query.PhotoStorageDao
import com.sola.anime.ai.generator.databinding.ItemPhotoBinding
import com.sola.anime.ai.generator.databinding.SheetPhotoBinding
import com.sola.anime.ai.generator.domain.model.PhotoStorage
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

@AndroidEntryPoint
class SheetPhoto: LsBottomSheet<SheetPhotoBinding>(SheetPhotoBinding::inflate) {

    @Inject lateinit var photoAdapter: PhotoAdapter
    @Inject lateinit var photoStorageDao: PhotoStorageDao

    val clicks: Subject<PhotoType.Photo> = PublishSubject.create()

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

    private fun listenerView() {
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
                when {
                    photoType is PhotoType.Photo -> {
                        photoAdapter.photo = photoType

                        clicks.onNext(photoType)
                    }
                }
            }
    }

    private fun initView(){
        binding.recyclerView.apply {
            this.layoutManager = object: GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = photoAdapter
        }
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
            binding.viewSelected.isVisible = photo == item
            binding.viewRatio.isVisible = item is PhotoType.Photo

            val isChecked = itemsChoiceDelete.contains(position)
            binding.imageCheck.setImageResource(if (isChecked) R.drawable.ic_check else R.drawable.ic_uncheck)

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
                                .sizeMultiplier(0.5f)
                                .error(R.drawable.place_holder_image)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.preview)

                            binding.imageCheck.setOnClickListener {
                                val newChecked = !isChecked

                                binding.imageCheck.setImageResource(if (newChecked) R.drawable.ic_check else R.drawable.ic_uncheck)

                                when {
                                    itemsChoiceDelete.contains(position) -> itemsChoiceDelete.remove(position)
                                    else -> itemsChoiceDelete.add(position)
                                }

                                subjectDeleteChanges.onNext(Unit)
                            }
                        }
                    }
                }
                else -> {}
            }

            binding.viewClicks.clicks{ clicks.onNext(item) }
        }
    }

    sealed class PhotoType {
        object ChoosePhoto: PhotoType()
        object ChooseCamera: PhotoType()
        data class Photo(val preview: Int? = null, val photoStorage: PhotoStorage? = null): PhotoType()
    }

}