package com.sola.anime.ai.generator.domain.model

enum class Ratio(val display: String, val ratio: String) {
    Ratio1x1(display = "1x1", ratio = "1:1"),
    Ratio9x16(display = "9x16", ratio = "9:16"),
    Ratio16x9(display = "16x9", ratio = "16:9"),
    Ratio4x3(display = "4x3", ratio = "4:3"),
    Ratio3x4(display = "3x4", ratio = "3:4"),
    Ratio2x3(display = "2x3", ratio = "2:3"),
    Ratio3x2(display = "3x2", ratio = "3:2")
}