package com.sola.anime.ai.generator.domain.model

enum class Sampler(val display: String, val sampler: String) {
    Random(display = "Random", sampler = ""),
    Ddim(display = "ddim", sampler = "ddim"),
    Dpm(display = "dpm", sampler = "dpm"),
    Euler(display = "euler", sampler = "euler"),
    EulerA(display = "euler_a", sampler = "euler_a")
}

enum class Ratio(val display: String, val ratio: String, val width: String, val height: String, val aspectRatio: Float) {
    Ratio1x1(display = "1x1", ratio = "1:1", width = "512", height = "512", aspectRatio = 1f / 1f),
    Ratio9x16(display = "9x16", ratio = "9:16", width = "324", height = "576", aspectRatio = 9f / 16f),
    Ratio16x9(display = "16x9", ratio = "16:9", width = "576", height = "324", aspectRatio = 16f / 9f),
    Ratio2x3(display = "2x3", ratio = "2:3", width = "340", height = "510", aspectRatio = 2f / 3f),
    Ratio3x2(display = "3x2", ratio = "3:2", width = "510", height = "340", aspectRatio = 3f / 2f),
    Ratio3x4(display = "3x4", ratio = "3:4", width = "384", height = "512", aspectRatio = 3f / 4f),
    Ratio4x3(display = "4x3", ratio = "4:3", width = "512", height = "384", aspectRatio = 4f / 3f),
}

enum class NumberOfImages(var display: String, val number: Int){
    NumberOfImages1(display = "10", number = 10),
    NumberOfImages2(display = "20", number = 20),
    NumberOfImages3(display = "30", number = 30),
    NumberOfImages4(display = "40", number = 40),
    NumberOfImages5(display = "50", number = 50),
    NumberOfImages6(display = "60", number = 60),
    NumberOfImages7(display = "70", number = 70),
    NumberOfImages8(display = "80", number = 80)
}

enum class TabExplore {
    Recommendations,
    ExploreRelated
}

enum class TabModelOrLoRA {
    Artworks,
    Others
}

//enum class ImageDimensions(val display: String){
//    ImageDimensions512x512(display = "512x512"),
//    ImageDimensions768x768(display = "768x768"),
//    ImageDimensions512x1024(display = "512x1024"),
//    ImageDimensions768x1024(display = "768x1024"),
//    ImageDimensions1024x768(display = "1024x768"),
//    ImageDimensions1024x1024(display = "1024x1024")
//}