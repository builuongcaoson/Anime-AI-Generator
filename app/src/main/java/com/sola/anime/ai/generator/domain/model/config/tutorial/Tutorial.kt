package com.sola.anime.ai.generator.domain.model.config.tutorial

import androidx.annotation.DrawableRes
import com.sola.anime.ai.generator.R

enum class TutorialStep(val display: String, @DrawableRes val preview: Int, val childs: List<TutorialStep2>){
    Princess(
        display = "Princess",
        preview = R.drawable.preview_tutorial_princess_1,
        childs = listOf(
            TutorialStep2(
                display = "Red hair",
                preview = R.drawable.preview_tutorial_princess_red_hair_1,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_princess_red_hair_short_hair_1
                    )
                )
            ),
            TutorialStep2(
                display = "Blue hair",
                preview = R.drawable.preview_tutorial_princess_blue_hair_1,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_princess_blue_hair_short_hair_1
                    )
                )
            ),
            TutorialStep2(
                display = "White hair",
                preview = R.drawable.preview_tutorial_princess_white_hair_1,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_princess_blue_hair_short_hair_1
                    )
                )
            )
        )
    ),
    CoolBoy(
        display = "Cool Boy",
        preview = R.drawable.preview_tutorial_cool_boy_1,
        childs = listOf(
            TutorialStep2(
                display = "Chinese clothes",
                preview = R.drawable.preview_tutorial_cool_boy_chinese_clothes_1,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_princess_red_hair_short_hair_1
                    )
                )
            ),
            TutorialStep2(
                display = "Green hair",
                preview = R.drawable.preview_tutorial_cool_boy_green_hair_1,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_princess_blue_hair_short_hair_1
                    )
                )
            ),
            TutorialStep2(
                display = "Red eyes",
                preview = R.drawable.preview_tutorial_cool_boy_red_eyes_1,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_princess_blue_hair_short_hair_1
                    )
                )
            )
        )
    )
}

data class TutorialStep2(val display: String, @DrawableRes val preview: Int, val childs: List<TutorialStep3>)

data class TutorialStep3(val display: String, @DrawableRes val preview: Int)