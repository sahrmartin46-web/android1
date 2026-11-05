package c.app.sociamediabackend.ui.theme

import android.graphics.Color

object FugenTokens {
    // Brand Colors
    val Background = Color.parseColor("#000000")       // Pure OLED black
    val Surface = Color.parseColor("#121212")          // Elevated dark surface
    val SurfaceVariant = Color.parseColor("#1E1E1E")   // Card / Sheet background
    val TextPrimary = Color.parseColor("#FFFFFF")      // High contrast text
    val TextSecondary = Color.parseColor("#B3B3B3")    // Subdued text/icons
    
    // FUGEN Brand Accent Dual-Gradients
    val AccentPrimary = Color.parseColor("#FE2C55")    // Vibrant crimson
    val AccentSecondary = Color.parseColor("#25F4EE")  // Cyan accent
    
    // Glassmorphism & Overlays
    val OverlayDark = Color.parseColor("#80000000")    // 50% scrim
    val GlassSurface = Color.parseColor("#1AFFFFFF")   // 10% white blur glass
    
    // Radii
    const val RadiusSmall = 8f
    const val RadiusMedium = 14f
    const val RadiusLarge = 24f
    const val RadiusFull = 999f                        // Pill / Circular buttons
    
    // Spacing (dp)
    const val PagePadding = 16
    const val ControlSpacing = 12
}
