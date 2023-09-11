package com.sola.anime.ai.generator.common.widget.quantitizer

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.HorizontalQuantitizerBinding

@SuppressLint("CustomViewStyleable")
class HorizontalQuantitizer @JvmOverloads constructor(context: Context,
                                                      attributeSet: AttributeSet? = null,
                                                      defStyle: Int = 0):
    ConstraintLayout(context, attributeSet, defStyle){

    private var listener :QuantitizerListener? = null
    private val binding = HorizontalQuantitizerBinding.inflate(LayoutInflater.from(context), this, true)
    private var currentValue: Int = 0

    private var _animationDuration = 300L
    private var _minValue:Int = 0
    private var _maxValue:Int = Int.MAX_VALUE
    private var _step:Int = 1
    private var _animateButtons: Boolean = true
    private var _animationStyle: AnimationStyle = AnimationStyle.SWING
    private var _isReadOnly: Boolean = false

    var minValue: Int
        get() = _minValue
        set(value) {
            if (value >= currentValue) {
                binding.quantityTv.text = Editable.Factory.getInstance().newEditable(value.toString())
                currentValue = value
                _minValue = value
            } else {
                _minValue = value
                currentValue = value
            }
        }

    var maxValue: Int
        get() = _maxValue
        set(value) {
            _maxValue = value
        }

    var step: Int
        get() = _step
        set(value) {
            _step = value
        }

    var value: Int
        get() = currentValue
        set(value) {
            currentValue = value
            binding.quantityTv.text = Editable.Factory.getInstance().newEditable(value.toString())
        }

    var buttonAnimationEnabled: Boolean
        get() = _animateButtons
        set(value) {
            _animateButtons = value
        }

    var textAnimationStyle: AnimationStyle
        get() = _animationStyle
        set(value) {
            _animationStyle = value
        }

    var animationDuration: Long
        get() = _animationDuration
        set(value) {
            _animationDuration = value
        }

    var isReadOnly: Boolean
        get() = _isReadOnly
        set(value) {
            isReadOnly(value)
        }

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.Quantitizer, defStyle, 0)

        minValue = a.getInteger(R.styleable.Quantitizer_minValue, 0)
        maxValue = a.getInteger(R.styleable.Quantitizer_maxValue, Int.MAX_VALUE)
        value = a.getInteger(R.styleable.Quantitizer_value, 0)
        step = a.getInteger(R.styleable.Quantitizer_step, _step)

        /*decrease*/
        binding.decreaseIb.clicks(debounce = 500L, withAnim = false) {
            hideKeyboard()

            when {
                minValue >= currentValue -> {}
                minValue == 0 && currentValue == 1 -> {}
                else -> {
                    doDec()

                    //listener
                    listener?.activateOnDecrease(_animationDuration)
                }
            }
        }

        /*increase*/
        binding.increaseIb.clicks(debounce = 500L, withAnim = false) {
            hideKeyboard()
            when {
                maxValue <= currentValue -> {}
                else -> {
                    doInc()

                    //listener
                    listener?.activateOnIncrease(_animationDuration)
                }
            }
        }

        /*make edit text cursor visible when clicked*/
//        binding.quantityTv.setOnClickListener {
//            if (_isReadOnly.not()) {
//                binding.quantityTv.isCursorVisible = true
//            }
//        }

        binding.quantityTv.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentValue = if (s.toString().isNotEmpty() || s.toString() != "") {
                    val value = Integer.parseInt(s.toString())
                    listener?.onValueChanged(value)
                    value
                }else{
                    0
                }
            }

            override fun afterTextChanged(s: Editable?) {
//                val value = s.toString().toIntOrNull()
//                if (s.toString().isEmpty()) {
//                    //do nothing
//                }else if (value!! < minValue && s.toString().isBlank().not()) {
//                    binding.quantityTv.text = Editable.Factory.getInstance().newEditable(minValue.toString())
//                    currentValue = minValue
//                    Toast.makeText(context, "Min value is $minValue", Toast.LENGTH_SHORT).show()
//                }else if (value > maxValue && s.toString().isBlank().not()) {
//                    binding.quantityTv.text = Editable.Factory.getInstance().newEditable(minValue.toString())
//                    currentValue = minValue
//                    Toast.makeText(context, "Max value is $maxValue", Toast.LENGTH_SHORT).show()
//
//                }
            }

        })

        /*TypedArrays are heavyweight objects that should be recycled immediately
         after all the attributes you need have been extracted.*/
        a.recycle()
    }

    private fun wobble(view: View): View {
        val anim: Animation = TranslateAnimation(-20F, 20F, 0f, 0f)
        anim.duration = 50L
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 3
        view.startAnimation(anim)
        return view
    }

    private fun doInc() {
        if (_animateButtons) {
            animatePlusButton()

        }

        binding.quantityTv.isCursorVisible = false // hide cursor if it's visible
        val increasedValue: Int = when {
            currentValue == 1 -> step
            else -> currentValue + step
        }
        currentValue = increasedValue
        animateInc()
    }

    private fun doDec() {
        if (_animateButtons) {
            animateMinusButton()
        }

        binding.quantityTv.isCursorVisible = false  // hide cursor if it's visible
        val decreasedValue: Int = when {
            currentValue - step <= 0 -> 1
            else -> currentValue - step
        }
        currentValue = decreasedValue
        animateDec()
    }

    private fun animateInc() {
        //animate and set current value for edit text
        when (_animationStyle) {
            AnimationStyle.SLIDE_IN_REVERSE -> {
                binding.quantityTv.textAnimSlideInRTL(
                    translation_X,
                    -200f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                ) // text
            }
            AnimationStyle.SLIDE_IN -> {
                binding.quantityTv.textAnimSlideInLTR(
                    translation_X,
                    200f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                )
            }
            AnimationStyle.FALL_IN -> {
                binding.quantityTv.textAnimFallIn(
                    translation_Y,
                    60f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                )
            }
            else -> {
                binding.quantityTv.textAnimSwing(
                    translation_X,
                    200f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                )
            }
        }

    }

    private fun animateDec() {
        //animate and set current value for edit text
        when (_animationStyle) {
            AnimationStyle.SLIDE_IN_REVERSE -> {
                binding.quantityTv.textAnimSlideInRTL(
                    translation_X,
                    200f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                ) // text
            }
            AnimationStyle.SLIDE_IN -> {
                binding.quantityTv.textAnimSlideInLTR(
                    translation_X,
                    -200f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                )
            }
            AnimationStyle.FALL_IN -> {
                binding.quantityTv.textAnimFallIn(
                    translation_Y,
                    -60f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                )
            }
            else -> {
                binding.quantityTv.textAnimSwing(
                    translation_X,
                    -200f,
                    0f,
                    currentValue.toString(),
                    _animationDuration
                )
            }
        }
    }

    private fun animatePlusButton() {
        //enter animation
        binding.increaseIb.enterAnimationSwing( translation_X, 0f, 20f , _animationDuration) // view

        //exit animation
        binding.increaseIb.exitAnimationSwing( translation_X, 20f, 0f , _animationDuration) // view
    }

    private fun animateMinusButton() {
        //enter animation
        binding.decreaseIb.enterAnimationSwing( translation_X, 0f, -20f , _animationDuration) // view

        //exit animation
        binding.decreaseIb.exitAnimationSwing( translation_X, -20f, 0f , _animationDuration) // view
    }

    fun setQuantitizerListener(listener : QuantitizerListener) {
        this.listener = listener
    }

    private fun isReadOnly(isReadOnly: Boolean): Boolean {
        return if (isReadOnly) {//if user wants read only, then set edittext enabled to false
            binding.quantityTv.apply {
                isFocusableInTouchMode = false
                isCursorVisible = false
                inputType = InputType.TYPE_NULL
            }
            true
        } else {//else set enabled to true
            binding.quantityTv.apply {
                isFocusableInTouchMode = true
                isCursorVisible = true
            }
            false
        }
    }
}