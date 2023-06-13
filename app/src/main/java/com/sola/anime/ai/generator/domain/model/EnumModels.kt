package com.sola.anime.ai.generator.domain.model

enum class Ratio(val display: String, val ratio: String, val width: String, val height: String) {
    Ratio1x1(display = "1x1", ratio = "1:1", width = "512", height = "512"),
    Ratio9x16(display = "9x16", ratio = "9:16", width = "513", height = "912"),
    Ratio16x9(display = "16x9", ratio = "16:9", width = "912", height = "513"),
    Ratio3x4(display = "3x4", ratio = "3:4", width = "384", height = "512"),
    Ratio4x3(display = "4x3", ratio = "4:3", width = "512", height = "384"),
    Ratio2x3(display = "2x3", ratio = "2:3", width = "340", height = "510"),
    Ratio3x2(display = "3x2", ratio = "3:2", width = "510", height = "340")
}

enum class NumberOfImages(var display: String){
    NumberOfImages1(display = "1"),
    NumberOfImages2(display = "2"),
    NumberOfImages3(display = "3"),
    NumberOfImages4(display = "4"),
    NumberOfImages5(display = "5"),
    NumberOfImages6(display = "6"),
    NumberOfImages7(display = "7"),
    NumberOfImagesEdit(display = "8")
}

//enum class ImageDimensions(val display: String){
//    ImageDimensions512x512(display = "512x512"),
//    ImageDimensions768x768(display = "768x768"),
//    ImageDimensions512x1024(display = "512x1024"),
//    ImageDimensions768x1024(display = "768x1024"),
//    ImageDimensions1024x768(display = "1024x768"),
//    ImageDimensions1024x1024(display = "1024x1024")
//}