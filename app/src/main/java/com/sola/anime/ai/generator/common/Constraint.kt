package com.sola.anime.ai.generator.common

class Constraint {
    class Api {
        companion object {

            const val DEZGO_URL = "https://api.dezgo.com/"
            const val DEZGO_HEADER_KEY = "X-Dezgo-Key"
            const val DEZGO_KEY = "LVVGJuOraUCqa9uD3oMyalY9vX0vb3KrVpUP16PWElXAStwwPBYPmbF2pjMYqQJNmV5kK6q9y+I4q+ts+rszV23j0SmrExlkQHRNZmOtSX4="

            const val DEZGO_RAPID_URL = "https://dezgo.p.rapidapi.com/"
            const val DEZGO_HEADER_RAPID_KEY = "X-RapidAPI-Key"
            const val DEZGO_RAPID_KEY = "yC52kE9ygotYegfeFSY1gW/Sm2UR5Cfh75TZY+Rj+9Woa1aELndKpW8zMbLG2NJega2aRsbXhlmT15BazsMdGg=="
            const val DEZGO_HEADER_RAPID_HOST = "X-RapidAPI-Host"
            const val DEZGO_RAPID_HOST = "dezgo.p.rapidapi.com"
        }
    }

    class Info {
        companion object {
            const val MAIL_SUPPORT = "sola.company.help@gmail.com"
            const val PRIVACY_URL = "https://sites.google.com/view/anime-art-privacy-policy"
            const val TERMS_URL = "https://sites.google.com/view/anime-art-terms-of-service"

            const val REVENUECAT_KEY = "goog_fmHtyzfMVxWqZjzwrVeWaMexRIq"

            const val DATA_VERSION = 2
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