package com.vesti.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.vesti.app.AppConfig

object VestiColors {
    val Primary = Color(0xFF7986CB)       // Classic Soft Indigo
    val Accent = Color(0xFFFF6F61)        // Vivid Coral
    val SuccessMood = Color(0xFFE8F5E9)   // Mint Green
    
    // Dynamic getters that adapt reactively to AppConfig.isDarkMode globally
    val TextMain: Color
        get() = if (AppConfig.isDarkMode) Color(0xFFECEFF1) else Color(0xFF37474F)      // Charcoal -> Off-White
        
    val DarkIndigo: Color
        get() = if (AppConfig.isDarkMode) Color(0xFF13141C) else Color(0xFF29294D)    // Dark Navy -> Charcoal
        
    val Background: Color
        get() = if (AppConfig.isDarkMode) Color(0xFF0F1017) else Color(0xFFF8F9FA)    // Off-White -> Charcoal Black
        
    val LightPurple: Color
        get() = if (AppConfig.isDarkMode) Color(0xFF282B3D) else Color(0xFFEDE7F6)   // Soft Purple -> Dark Slate Indigo
}
