package com.io.lkconsultants.color


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Raw color palette ────────────────────────────────────────────────────────

private object LKPalette {
    // Blues
    val Blue900  = Color(0xFF0D47A1)
    val Blue800  = Color(0xFF1565C0)
    val Blue600  = Color(0xFF1976D2)
    val Blue400  = Color(0xFF42A5F5)
    val Blue200  = Color(0xFF90CAF9)
    val Blue100  = Color(0xFFBBDEFB)

    // Accent / sky
    val Sky400   = Color(0xFF29B6F6)
    val Sky200   = Color(0xFF81D4FA)

    // Reds
    val Red800   = Color(0xFFC62828)
    val Red700   = Color(0xFFD32F2F)
    val Red400   = Color(0xFFEF5350)
    val Red200   = Color(0xFFEF9A9A)

    // Neutrals
    val White    = Color(0xFFFFFFFF)
    val Black    = Color(0xFF000000)

    // Surfaces – light
    val Surface0 = Color(0xFFFFFFFF)
    val Surface1 = Color(0xFFF0F4FF)
    val Surface2 = Color(0xFFE8EEF9)

    // Surfaces – dark
    val Dark0    = Color(0xFF0A0F1E)   // page background
    val Dark1    = Color(0xFF111827)   // surface (cards, bars)
    val Dark2    = Color(0xFF1C2536)   // elevated surface

    // Text – light
    val Text900  = Color(0xFF0D1B2A)
    val Text600  = Color(0xFF546E7A)
    val Text300  = Color(0xFF90A4AE)

    // Text – dark
    val TextD100 = Color(0xFFE8EEF9)
    val TextD400 = Color(0xFF90A4AE)
    val TextD500 = Color(0xFF607D8B)

    // Divider
    val DivLight = Color(0xFFCFD8DC)
    val DivDark  = Color(0xFF1E2D42)

    // Chat bubbles
    val BubbleAdminLight = Color(0xFF1565C0)
    val BubbleAdminDark  = Color(0xFF1976D2)
    val BubbleUserLight  = Color(0xFFE3F2FD)
    val BubbleUserDark   = Color(0xFF1C2D45)

    // File card
    val FileCardLight = Color(0xFFF5F9FF)
    val FileCardDark  = Color(0xFF151E2E)

    // Bottom bar
    val BottomBarLight = Color(0xFF0D47A1)
    val BottomBarDark  = Color(0xFF0D1B36)
}

// ─── Semantic color class ─────────────────────────────────────────────────────

@Immutable
data class LKColorScheme(
    val primaryBlue: Color,
    val primaryBlueDark: Color,
    val accentBlue: Color,
    val accentBlueMid: Color,
    val brandRed: Color,
    val brandRedLight: Color,
    val white: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val onSurface: Color,
    val subtitle: Color,
    val hint: Color,
    val divider: Color,
    val chatBubbleAdmin: Color,
    val chatBubbleUser: Color,
    val onChatBubbleAdmin: Color,
    val onChatBubbleUser: Color,
    val fileCardBg: Color,
    val bottomBar: Color,
    val isDark: Boolean
)

// ─── Light scheme ─────────────────────────────────────────────────────────────

val LightLKColors = LKColorScheme(
    primaryBlue       = LKPalette.Blue800,
    primaryBlueDark   = LKPalette.Blue900,
    accentBlue        = LKPalette.Sky400,
    accentBlueMid     = LKPalette.Blue600,
    brandRed          = LKPalette.Red700,
    brandRedLight     = LKPalette.Red400,
    white             = LKPalette.White,
    background        = LKPalette.Surface1,
    surface           = LKPalette.Surface0,
    surfaceElevated   = LKPalette.Surface2,
    onSurface         = LKPalette.Text900,
    subtitle          = LKPalette.Text600,
    hint              = LKPalette.Text300,
    divider           = LKPalette.DivLight,
    chatBubbleAdmin   = LKPalette.BubbleAdminLight,
    chatBubbleUser    = LKPalette.BubbleUserLight,
    onChatBubbleAdmin = LKPalette.White,
    onChatBubbleUser  = LKPalette.Text900,
    fileCardBg        = LKPalette.FileCardLight,
    bottomBar         = LKPalette.BottomBarLight,
    isDark            = false
)

// ─── Dark scheme ──────────────────────────────────────────────────────────────

val DarkLKColors = LKColorScheme(
    primaryBlue       = LKPalette.Blue400,       // lighter so it pops on dark bg
    primaryBlueDark   = LKPalette.Blue200,
    accentBlue        = LKPalette.Sky200,
    accentBlueMid     = LKPalette.Blue400,
    brandRed          = LKPalette.Red400,
    brandRedLight     = LKPalette.Red200,
    white             = LKPalette.TextD100,       // "white" → soft light text
    background        = LKPalette.Dark0,
    surface           = LKPalette.Dark1,
    surfaceElevated   = LKPalette.Dark2,
    onSurface         = LKPalette.TextD100,
    subtitle          = LKPalette.TextD400,
    hint              = LKPalette.TextD500,
    divider           = LKPalette.DivDark,
    chatBubbleAdmin   = LKPalette.BubbleAdminDark,
    chatBubbleUser    = LKPalette.BubbleUserDark,
    onChatBubbleAdmin = LKPalette.White,
    onChatBubbleUser  = LKPalette.TextD100,
    fileCardBg        = LKPalette.FileCardDark,
    bottomBar         = LKPalette.BottomBarDark,
    isDark            = true
)

// ─── CompositionLocal ─────────────────────────────────────────────────────────

val LocalLKColors = staticCompositionLocalOf { LightLKColors }

// ─── Accessor — use this in every composable ──────────────────────────────────
//   val colors = lkColors
//   Text(color = colors.primaryBlue)


// ─── Theme wrapper ────────────────────────────────────────────────────────────
//   Wrap your NavStack (or any root composable) with this once:
//   LKTheme { NavStack() }
@Composable
fun LKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkLKColors else LightLKColors
    CompositionLocalProvider(LocalLKColors provides colors) {
        content()
    }
}

// ─── Accessor (use this everywhere instead of LKColors.XYZ) ──────────────────
//
//   val colors = lkColors()
//   Text(color = colors.primaryBlue, ...)
//
val lkColors: LKColorScheme
    @Composable get() = LocalLKColors.current

// ─── Theme wrapper ────────────────────────────────────────────────────────────
//
// Wrap your existing AppTheme with this, or call it standalone:
//
//   LKTheme {
//       ChatScreen(...)
//   }
//
