package com.vesti.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

object AppConfig {
    // Global reactive Compose states
    var isDarkMode by mutableStateOf(false)
    var language by mutableStateOf("tr") // "tr" or "en"

    /**
     * A highly optimized Composable translation helper.
     * Returns the English translation if global language is set to "en",
     * otherwise falls back to Turkish. Recomposes instantly on change.
     */
    @Composable
    fun t(tr: String, en: String): String {
        return if (language == "en") en else tr
    }

    /**
     * Utility to dynamically update the Android application display locale configuration.
     * This forces the system context to refresh so that native bottom bar XML strings
     * loaded via stringResource(id) update instantly without requiring an Activity restart.
     */
    fun updateLocale(context: Context, lang: String) {
        try {
            val locale = Locale(lang)
            Locale.setDefault(locale)
            
            val resources = context.resources
            val config = resources.configuration
            
            config.setLocale(locale)
            context.createConfigurationContext(config)
            
            // Apply deprecation-compatible resource configuration update
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun translateColor(color: String): String {
        val clean = color.lowercase().trim()
        val en = when (clean) {
            "siyah" -> "Black"
            "beyaz" -> "White"
            "mavi" -> "Blue"
            "kırmızı" -> "Red"
            "yeşil" -> "Green"
            "gri" -> "Gray"
            "krem" -> "Cream"
            "bej" -> "Beige"
            "sarı" -> "Yellow"
            "turuncu" -> "Orange"
            "mor" -> "Purple"
            "pembe" -> "Pink"
            "kahve", "kahverengi" -> "Brown"
            "lacivert" -> "Navy Blue"
            "gül kurusu" -> "Rose Dust"
            "vişne çürüğü" -> "Cherry Dregs"
            "petrol" -> "Teal"
            "haki" -> "Khaki"
            "saks" -> "Royal Blue"
            "pudra" -> "Powder Pink"
            "ekru" -> "Ecru"
            "taba" -> "Tan"
            "antrasit" -> "Anthracite"
            "hardal" -> "Mustard"
            "mint" -> "Mint"
            "lila" -> "Lilac"
            "somon" -> "Salmon"
            "gece mavisi" -> "Midnight Blue"
            "fıstık" -> "Pistachio"
            "kiremit" -> "Brick Red"
            "vizon" -> "Mink"
            "şeftali" -> "Peach"
            "bordo" -> "Burgundy"
            "indigo" -> "Indigo"
            "camel", "deve tüyü" -> "Camel"
            "altın", "dore" -> "Gold"
            "gümüş", "lame" -> "Silver"
            "fuşya" -> "Fuchsia"
            "turkuaz" -> "Turquoise"
            "mercan" -> "Coral"
            else -> color.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        return if (language == "en") en else color.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun translateCategory(category: String): String {
        val clean = category.lowercase().trim()
        val en = when (clean) {
            "tişört" -> "T-Shirt"
            "gömlek" -> "Shirt"
            "ceket" -> "Jacket"
            "deri ceket" -> "Leather Jacket"
            "blazer ceket" -> "Blazer Jacket"
            "vintage deri ceket" -> "Vintage Leather Jacket"
            "pantolon" -> "Pants"
            "elbise" -> "Dress"
            "ayakkabı" -> "Shoes"
            "aksesuar" -> "Accessory"
            "kazak" -> "Sweater"
            "kaban" -> "Coat"
            "takım" -> "Suit"
            "etek" -> "Skirt"
            "yok" -> "None"
            "uncategorized" -> "Uncategorized"
            else -> category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        return if (language == "en") en else category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
