package com.sola.anime.ai.generator.domain.model.config.tutorial

import androidx.annotation.DrawableRes
import com.sola.anime.ai.generator.R

enum class TutorialStep(val display: String, @DrawableRes val preview: Int, val childs: List<TutorialStep2>){
    Princess(
        display = "Angel",
        preview = R.drawable.preview_tutorial_angel,
        childs = listOf(
            TutorialStep2(
                display = "Water magic",
                preview = R.drawable.preview_tutorial_angel_water_magic,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_angel_water_magic_short_hair
                    ),
                    TutorialStep3(
                        display = "Blue hair",
                        preview = R.drawable.preview_tutorial_angel_water_magic_blue_hair
                    )
                )
            ),
            TutorialStep2(
                display = "Wings white blood",
                preview = R.drawable.preview_tutorial_angel_wings_white_blood,
                childs = listOf(
                    TutorialStep3(
                        display = "Golden hair",
                        preview = R.drawable.preview_tutorial_angel_wings_white_blood_golden_hair
                    ),
                    TutorialStep3(
                        display = "Red hair",
                        preview = R.drawable.preview_tutorial_angel_wings_white_blood_red_hair
                    ),
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_angel_wings_white_blood_short_hair
                    )
                )
            ),
            TutorialStep2(
                display = "Red hair",
                preview = R.drawable.preview_tutorial_angel_red_hair,
                childs = listOf(
                    TutorialStep3(
                        display = "Short hair",
                        preview = R.drawable.preview_tutorial_angel_red_hair_short_hair
                    ),
                    TutorialStep3(
                        display = "White dress",
                        preview = R.drawable.preview_tutorial_angel_red_hair_white_dress
                    )
                )
            )
        )
    ),
    YoungBoy(
        display = "Young Boy",
        preview = R.drawable.preview_tutorial_young_boy,
        childs = listOf(
            TutorialStep2(
                display = "Wear a hat",
                preview = R.drawable.preview_tutorial_young_boy_wear_hat,
                childs = listOf(
                    TutorialStep3(
                        display = "Yellow magic",
                        preview = R.drawable.preview_tutorial_young_boy_wear_hat_yellow_magic
                    ),
                    TutorialStep3(
                        display = "Fire magic",
                        preview = R.drawable.preview_tutorial_young_boy_wear_hat_red_magic
                    ),
                    TutorialStep3(
                        display = "Holding the scepter",
                        preview = R.drawable.preview_tutorial_young_boy_wear_hat_holding_scepter
                    )
                )
            ),
            TutorialStep2(
                display = "Red wand",
                preview = R.drawable.preview_tutorial_young_boy_red_wand,
                childs = listOf(
                    TutorialStep3(
                        display = "Red hair",
                        preview = R.drawable.preview_tutorial_young_boy_red_wand_red_hair
                    ),
                    TutorialStep3(
                        display = "Wear aristocratic clothes",
                        preview = R.drawable.preview_tutorial_young_boy_red_wand_aristocratic_clothes
                    )
                )
            ),
            TutorialStep2(
                display = "White robe",
                preview = R.drawable.preview_tutorial_young_boy_white_robe,
                childs = listOf(
                    TutorialStep3(
                        display = "Wings white blood",
                        preview = R.drawable.preview_tutorial_young_boy_white_hair_wings_blood
                    ),
                    TutorialStep3(
                        display = "Wings black blood",
                        preview = R.drawable.preview_tutorial_young_boy_white_hair_wings_black_blood
                    )
                )
            )
        )
    )
}

data class TutorialStep2(val display: String, @DrawableRes val preview: Int, val childs: List<TutorialStep3>)

data class TutorialStep3(val display: String, @DrawableRes val preview: Int)