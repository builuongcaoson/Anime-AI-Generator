package com.sola.anime.ai.generator.common

class Constraint {
    class Api {
        companion object {
            const val DEZGO_API_KEY = "6bacdccfc8mshcf72a2013553932p1897eajsn65b89307e1cd"
            const val DEZGO_API_HOST = "dezgo.p.rapidapi.com"
        }
    }

    class Info {
        companion object {
            const val MAIL_SUPPORT = "sola.luthimylanh@gmail.com"
            const val PRIVACY_URL = "https://sites.google.com/view/anime-art-privacy-policy"
            const val TERMS_URL = "https://sites.google.com/view/anime-art-terms-of-service"
        }
    }

    class Dezgo {
        companion object {
            const val DEFAULT_NEGATIVE = "(character out of frame)1.4, (worst quality)1.2, (low quality)1.6, (normal quality)1.6, lowres, (monochrome)1.1, (grayscale)1.3, acnes, skin blemishes, bad anatomy, DeepNegative,(fat)1.1, bad hands, text, error, missing fingers, extra limbs, missing limbs, extra digits, fewer digits, cropped, jpeg artifacts,signature, watermark, furry, elf ears"
        }
    }

    class Iap {
        companion object  {
//            const val SKU_LIFE_TIME = "buy_lifetime"
//            const val SKU_WEEK = "buy_week"
//            const val SKU_YEAR = "buy_year"

            const val SKU_LIFE_TIME = "lifetime"
            const val SKU_WEEK = "buy_week"
            const val SKU_YEAR = "buy_month"
        }
    }
}