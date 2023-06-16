package com.sola.anime.ai.generator.common

class Constraint {
    class Server {
        companion object {
            const val SERVER_URL = "https://sonbui.000webhostapp.com/anime/"
        }
    }

    class Info {
        companion object {
            const val MAIL_SUPPORT = "sola.company.help@gmail.com"
            const val PRIVACY_URL = "https://sites.google.com/view/anime-art-privacy-policy"
            const val TERMS_URL = "https://sites.google.com/view/anime-art-terms-of-service"

            const val REVENUECAT_KEY = "goog_fmHtyzfMVxWqZjzwrVeWaMexRIq"

            const val DATA_VERSION = 4
        }
    }

    class Dezgo {
        companion object {
            const val DEFAULT_NEGATIVE = "(character out of frame)1.4, (worst quality)1.2, (low quality)1.6, (normal quality)1.6, lowres, (monochrome)1.1, (grayscale)1.3, acnes, skin blemishes, bad anatomy, DeepNegative,(fat)1.1, bad hands, text, error, missing fingers, extra limbs, missing limbs, extra digits, fewer digits, cropped, jpeg artifacts, signature, watermark, furry, elf ears"

            const val DEZGO_URL = "https://api.dezgo.com/"
            const val DEZGO_HEADER_KEY = "X-Dezgo-Key"
            const val DEZGO_KEY = "sdt0vcv+Iz4D/V/7HER47txE+5HyViNlMFiJr6hDDnI9OlqCRY0BczetLn79wmy7OCieVADKSa1qlhTOnM1JCUQB2zWROss816XQq12TCas="

            const val DEZGO_RAPID_URL = "https://dezgo.p.rapidapi.com/"
            const val DEZGO_HEADER_RAPID_KEY = "X-RapidAPI-Key"
            const val DEZGO_RAPID_KEY = "yC52kE9ygotYegfeFSY1gW/Sm2UR5Cfh75TZY+Rj+9Woa1aELndKpW8zMbLG2NJega2aRsbXhlmT15BazsMdGg=="
            const val DEZGO_HEADER_RAPID_HOST = "X-RapidAPI-Host"
            const val DEZGO_RAPID_HOST = "dezgo.p.rapidapi.com"
        }
    }

    class Iap {
        companion object  {
            const val SKU_LIFE_TIME = "buy_lifetime"
            const val SKU_WEEK = "buy_week"
            const val SKU_WEEK_3D_TRIAl = "buy_week_3d_trial"
            const val SKU_MONTH = "buy_month"
            const val SKU_YEAR = "buy_year"

            const val SKU_CREDIT_1000 = "buy_credit_1000"
            const val SKU_CREDIT_3000 = "buy_credit_3000"
            const val SKU_CREDIT_5000 = "buy_credit_5000"
            const val SKU_CREDIT_10000 = "buy_credit_10000"
        }

    }

    class Promo {
        companion object {
            const val USER_PURCHASED_LIFE_TIME = "USER_PURCHASED_LIFE_TIME"
            const val TRY_USER_PURCHASED = "TRY_USER_PURCHASED"
        }
    }

}