package com.sola.anime.ai.generator.common.widget.quantitizer

interface QuantitizerListener {
    fun onIncrease()
    fun onDecrease()
    fun onValueChanged(value: Int)
}