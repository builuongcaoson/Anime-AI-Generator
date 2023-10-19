package com.sola.anime.ai.generator.common

class Constraint {
    class Upscale {
        companion object {
            const val URL = "https://super-image1.p.rapidapi.com/"
            const val HEADER_RAPID_KEY = "X-RapidAPI-Key"
            const val RAPID_KEY = "6dc18db382mshf55f8c8958b8188p1dff71jsnb059d3d58467"
            const val HEADER_RAPID_HOST = "X-RapidAPI-Host"
            const val RAPID_HOST = "super-image1.p.rapidapi.com"
        }
    }

    class Server {
        companion object {
            const val URL = "https://sonbui.000webhostapp.com/anime/"
        }
    }

    class Dezgo {
        companion object {
            const val DEFAULT_NEGATIVE = "(character out of frame)1.4, (worst quality)1.2, (low quality)1.6, (normal quality)1.6, lowres, (monochrome)1.1, (grayscale)1.3, acnes, skin blemishes, bad anatomy, DeepNegative,(fat)1.1, bad hands, text, error, missing fingers, extra limbs, missing limbs, extra digits, fewer digits, cropped, jpeg artifacts, signature, watermark, furry, elf ears"
            const val DEFAULT_MODEL = "anything_4_0"
            const val DEFAULT_STRENGTH_IMG_TO_IMG = "0.5"

            const val URL = "https://api.dezgo.com/"
            const val HEADER_KEY = "X-Dezgo-Key"
            const val KEY = "Gt8zA+OMwHWNPgBZotF452X6qJSpVIzY9FgesTlf1xfgfJUnOuuirw1j6JUpWxsTV0+hCTYqUW6F1R5jTX6IrC3nm3vPo+Gc14Hm52kc6us="
            const val KEY_PREMIUM = "1Nx2znPpNX0KvzDWDdqJ8SK9j1YhZlR4R1/Kbe3+FVilNwT8MbMlJS0vDumE/yynGICLFDUVHPCEaLI8u66IlU1HauAo/PlBMp9Zu57qABM="
        }
    }

    class Ls {
        companion object {
            const val URL = "https://overhanging-mixes.000webhostapp.com/"
        }
    }

    class Info {
        companion object {
            const val MAIL_SUPPORT = "sola.company.help@gmail.com"
            const val PRIVACY_URL = "https://sites.google.com/view/anime-art-privacy-policy"
            const val TERMS_URL = "https://sites.google.com/view/anime-art-terms-of-service"

            const val REVENUECAT_KEY = "goog_fmHtyzfMVxWqZjzwrVeWaMexRIq"

            const val DATA_VERSION = 19
        }
    }

    class Iap {
        companion object  {
            const val SKU_LIFE_TIME = "buy_lifetime"
            const val SKU_WEEK = "buy_week"
            const val SKU_YEAR = "buy_year"

            const val SKU_CREDIT_1000 = "buy_credit_1000"
            const val SKU_CREDIT_3000 = "buy_credit_3000"
            const val SKU_CREDIT_5000 = "buy_credit_5000"
            const val SKU_CREDIT_10000 = "buy_credit_10000"
        }

    }

    class Promo {
        companion object {
            const val REWARD_CREDITS = "REWARD_CREDITS"
        }
    }

}