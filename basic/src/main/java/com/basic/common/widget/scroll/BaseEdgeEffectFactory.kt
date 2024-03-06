package com.basic.common.widget.scroll

import android.graphics.Color
import android.widget.EdgeEffect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.forEachVisibleHolder

class BaseEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

    companion object {
        /** The magnitude of rotation while the list is over-scrolled. */
        private const val OVERSCROLL_ROTATION_MAGNITUDE = -10

        /** The magnitude of translation distance while the list is over-scrolled. */
        private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 0.2f

        /** The magnitude of translation distance when the list reaches the edge on fling. */
        private const val FLING_TRANSLATION_MAGNITUDE = 0.5f
    }

    var bouncyRotateEnabled = false
    var bouncyVerticalEnabled = false
    var bouncyHorizontalEnabled = false

    override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
        var isVertical = false

        when (val layoutManager = recyclerView.layoutManager) {
            is GridLayoutManager -> isVertical = layoutManager.orientation == GridLayoutManager.VERTICAL
            is LinearLayoutManager -> isVertical = layoutManager.orientation == LinearLayoutManager.VERTICAL
            is StaggeredGridLayoutManager -> isVertical = layoutManager.orientation == StaggeredGridLayoutManager.VERTICAL
        }

        val edgeEffect = object : EdgeEffect(recyclerView.context) {

            override fun onPull(deltaDistance: Float) {
                super.onPull(deltaDistance)
                handlePull(deltaDistance)
            }

            override fun onPull(deltaDistance: Float, displacement: Float) {
                super.onPull(deltaDistance, displacement)
                handlePull(deltaDistance)
            }

            private fun handlePull(deltaDistance: Float) {
                // This is called on every touch event while the list is scrolled with a finger.
                // We simply update the view properties without animation.
                val sign = if (direction == DIRECTION_BOTTOM || direction == DIRECTION_RIGHT) -1 else 1
                val rotationDelta = sign * deltaDistance * OVERSCROLL_ROTATION_MAGNITUDE
                val translationXorYDelta = sign * recyclerView.width * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
                recyclerView.forEachVisibleHolder { holder: LsViewHolder<ViewBinding> ->
                    if (bouncyRotateEnabled){
                        holder.rotation.cancel()
                        holder.itemView.rotation += rotationDelta
                    }

                    when {
                        isVertical && bouncyVerticalEnabled -> {
                            holder.translationY.cancel()
                            holder.itemView.translationY += translationXorYDelta
                        }
                        !isVertical && bouncyHorizontalEnabled -> {
                            holder.translationX.cancel()
                            holder.itemView.translationX += translationXorYDelta
                        }
                    }
                }
            }

            override fun onRelease() {
                super.onRelease()
                // The finger is lifted. This is when we should start the animations to bring
                // the view property values back to their resting states.
                recyclerView.forEachVisibleHolder { holder: LsViewHolder<ViewBinding> ->
                    if (bouncyRotateEnabled){
                        holder.rotation.start()
                    }

                    when {
                        isVertical && bouncyVerticalEnabled -> holder.translationY.start()
                        !isVertical && bouncyHorizontalEnabled -> holder.translationX.start()
                    }
                }
            }

            override fun onAbsorb(velocity: Int) {
                super.onAbsorb(velocity)
                val sign = if (direction == DIRECTION_BOTTOM || direction == DIRECTION_RIGHT) -1 else 1

                // The list has reached the edge on fling.
                val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
                recyclerView.forEachVisibleHolder { holder: LsViewHolder<ViewBinding> ->
                    when {
                        isVertical && bouncyVerticalEnabled -> holder.translationY
                            .setStartVelocity(translationVelocity)
                            .start()
                        !isVertical && bouncyHorizontalEnabled -> holder.translationX
                            .setStartVelocity(translationVelocity)
                            .start()
                    }
                }
            }
        }
        edgeEffect.color = Color.TRANSPARENT
        return edgeEffect
    }

}