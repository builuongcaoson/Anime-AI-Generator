package com.sola.anime.ai.generator.common

class Constraint {
    class Api {
        companion object {
            const val DEZGO_URL = "https://api.dezgo.com/"
            const val DEZGO_API_KEY = "DEZGO-D52F2465008D43E746840EF0A88000892E435F3EF219B29C7D263BB8F11F9E43DE560228"
        }
    }

    class Info {
        companion object {
            const val MAIL_SUPPORT = "sola.company.help@gmail.com"
            const val PRIVACY_URL = "https://sites.google.com/view/anime-art-privacy-policy"
            const val TERMS_URL = "https://sites.google.com/view/anime-art-terms-of-service"

            const val REVENUECAT_KEY = "goog_fmHtyzfMVxWqZjzwrVeWaMexRIq"
        }
    }

    class Dezgo {
        companion object {
            const val DEFAULT_NEGATIVE = "(character out of frame)1.4, (worst quality)1.2, (low quality)1.6, (normal quality)1.6, lowres, (monochrome)1.1, (grayscale)1.3, acnes, skin blemishes, bad anatomy, DeepNegative,(fat)1.1, bad hands, text, error, missing fingers, extra limbs, missing limbs, extra digits, fewer digits, cropped, jpeg artifacts,signature, watermark, furry, elf ears"
        }
    }

    class Iap {
        companion object  {
            const val SKU_LIFE_TIME = "buy_lifetime"
            const val SKU_WEEK = "buy_week"
            const val SKU_WEEK_3D_TRIAl = "buy_week_3d_trial"
            const val SKU_MONTH = "buy_month"
            const val SKU_YEAR = "buy_year"
        }
    }
}