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

    var clicks: (PhotoType) -> Unit = {}

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun initData() {
        photoStorageDao.getAllLive().observe(viewLifecycleOwner){
            binding.tick.isVisible = false
            binding.cancel.isVisible = false

             val data = arrayListOf(
                PhotoType.ChoosePhoto,
                PhotoType.ChooseCamera,
//                PhotoType.Photo(R.drawable.preview_input_photo_1),
//                PhotoType.Photo(R.drawable.preview_input_photo_2),
//                PhotoType.Photo(R.drawable.preview_input_photo_3),
//                PhotoType.Photo(R.drawable.preview_input_photo_4),
//                PhotoType.Photo(R.drawable.preview_input_photo_5),
//                PhotoType.Photo(R.drawable.preview_input_photo_6),
//                PhotoType.Photo(R.drawable.preview_input_photo_7)
            ).apply {
                addAll(it.map { PhotoType.Photo(photoStorage = it) })
             }
            photoAdapter.isTypeDeleted = false
            photoAdapter.data = data
        }
    }

    private fun listenerView() {
        binding.delete.clicks(debounce = 250, withAnim = true) {
            binding.cancel.isVisible = true
            binding.delete.isVisible = false

            photoAdapter.isTypeDeleted = true
        }
        binding.cancel.clicks(debounce = 250, withAnim = true) {
            binding.cancel.isVisible = false
            binding.delete.isVisible = true
            binding.tick.isVisible = false

            photoAdapter.isTypeDeleted = false
        }
        binding.tick.clicks(debounce = 250, withAnim = true) {
           val imageStorage = photoAdapter.listChooseDelete.map { photoAdapter.data[it] }
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
                binding.tick.isVisible = photoAdapter.listChooseDelete.isNotEmpty()
            }
    }

    private fun initView(){
        binding.recyclerView.apply {
            this.layoutManager = object: GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = photoAdapter.apply {
                this.clicks = this@SheetPhoto.clicks
            }
        }
    }

    class PhotoAdapter @Inject constructor(): LsAdapter<PhotoType, ItemPhotoBinding>(ItemPhotoBinding::inflate) {

        var clicks: (PhotoType) -> Unit = {}
        var isTypeDeleted: Boolean = false
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value

                listChooseDelete.clear()

                notifyDataSetChanged()
            }
        val listChooseDelete = arrayListOf<Int>()
        val subjectDeleteChanges: Subject<Unit> = PublishSubject.create()

        override fun bindItem(item: PhotoType, binding: ItemPhotoBinding, position: Int) {
            when (item){
                PhotoType.ChoosePhoto -> {
                    binding.camera.isVisible = false
                    binding.photo.isVisible = true
                    binding.preview.isVisible = false

                    binding.imageCheck.isVisible = false

                    binding.viewClicks.clicks(debounce = 250, withAnim = true){
                        clicks(item)
                    }
                }
                PhotoType.ChooseCamera -> {
                    binding.camera.isVisible = true
                    binding.photo.isVisible = false
                    binding.preview.isVisible = false

                    binding.imageCheck.isVisible = false

                    binding.viewClicks.clicks(debounce = 250, withAnim = true){
                        clicks(item)
                    }
                }
                is PhotoType.Photo -> {
                    binding.camera.isVisible = false
                    binding.photo.isVisible = false
                    binding.preview.isVisible = true

                    when {
                        item.preview != null -> {
                            binding.imageCheck.isVisible = false

                            binding.preview.setImageResource(item.preview)
                        }
                        item.photoStorage != null -> {
                            Glide.with(binding.root.context)
                                .load(item.photoStorage.uriString)
                                .sizeMultiplier(0.5f)
                                .placeholder(R.drawable.place_holder_image)
                                .error(R.drawable.place_holder_image)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(binding.preview)

                            binding.imageCheck.isVisible = isTypeDeleted

                            val isChecked = listChooseDelete.contains(position)

                            when {
                                isChecked -> binding.imageCheck.setImageResource(R.drawable.ic_check)
                                else -> binding.imageCheck.setImageResource(R.drawable.ic_uncheck)
                            }

                            binding.imageCheck.setOnClickListener {
                                val newChecked = !isChecked

                                when {
                                    newChecked -> binding.imageCheck.setImageResource(R.drawable.ic_check)
                                    else -> binding.imageCheck.setImageResource(R.drawable.ic_uncheck)
                                }

                                when {
                                    listChooseDelete.contains(position) -> listChooseDelete.remove(position)
                                    else -> listChooseDelete.add(position)
                                }
                                subjectDeleteChanges.onNext(Unit)
                            }
                        }
                    }

                    binding.viewClicks.clicks(debounce = 250, withAnim = true){
                        clicks(item)
                    }
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