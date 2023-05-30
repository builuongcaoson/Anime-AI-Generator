package com.sola.anime.ai.generator.domain.model.config

import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.iap.IAP
import com.sola.anime.ai.generator.domain.model.config.style.Style

data class Data constructor(
    val explores: List<Explore> = listOf(),
    val iapList: List<IAP> = listOf(),
    val processes: List<Process> = listOf(),
    val styles: List<Style> = listOf()
) {

}