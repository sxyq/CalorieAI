package com.calorieai.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object AppColors {
    @Composable
    private fun currentDarkTheme(): Boolean {
        return MaterialTheme.colorScheme.background.luminance() < 0.5f
    }

    @Composable
    fun getColors(darkTheme: Boolean = currentDarkTheme()): GlassColorScheme {
        return if (darkTheme) GlassDarkColors.toScheme() else GlassLightColors.toScheme()
    }

    fun getColorsSync(darkTheme: Boolean): GlassColorScheme {
        return if (darkTheme) GlassDarkColors.toScheme() else GlassLightColors.toScheme()
    }

    @Composable
    fun primary(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Primary else GlassLightColors.Primary
    }

    @Composable
    fun secondary(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Secondary else GlassLightColors.Secondary
    }

    @Composable
    fun tertiary(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Tertiary else GlassLightColors.Tertiary
    }

    @Composable
    fun surface(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Surface else GlassLightColors.Surface
    }

    @Composable
    fun background(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Background else GlassLightColors.Background
    }

    @Composable
    fun onSurface(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    }

    @Composable
    fun onSurfaceVariant(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    }

    @Composable
    fun textPrimary(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.TextPrimary else GlassLightColors.TextPrimary
    }

    @Composable
    fun textSecondary(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.TextSecondary else GlassLightColors.TextSecondary
    }

    @Composable
    fun cardBackground(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.CardBackground else GlassLightColors.CardBackground
    }

    @Composable
    fun surfaceContainerHigh(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.SurfaceContainerHigh else GlassLightColors.SurfaceContainerHigh
    }

    @Composable
    fun outline(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Outline else GlassLightColors.Outline
    }

    @Composable
    fun divider(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Divider else GlassLightColors.Divider
    }

    @Composable
    fun success(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Success else GlassLightColors.Success
    }

    @Composable
    fun error(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Error else GlassLightColors.Error
    }

    @Composable
    fun warning(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.Warning else GlassLightColors.Warning
    }

    @Composable
    fun navigationBarBackground(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.NavigationBarBackground else GlassLightColors.NavigationBarBackground
    }

    @Composable
    fun secondaryContainer(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.SecondaryContainer else GlassLightColors.SecondaryContainer
    }

    @Composable
    fun onPrimary(darkTheme: Boolean = currentDarkTheme()): Color {
        return if (darkTheme) GlassDarkColors.OnPrimary else GlassLightColors.OnPrimary
    }

    private val GlassDarkColors.TextPrimary get() = OnSurface
    private val GlassDarkColors.TextSecondary get() = OnSurfaceVariant
    private val GlassDarkColors.Divider get() = OutlineVariant
    private val GlassDarkColors.Success get() = Tertiary
    private val GlassDarkColors.Warning get() = Tertiary

    private val GlassLightColors.TextPrimary get() = OnSurface
    private val GlassLightColors.TextSecondary get() = OnSurfaceVariant
    private val GlassLightColors.Divider get() = OutlineVariant
    private val GlassLightColors.Success get() = Tertiary
    private val GlassLightColors.Warning get() = Tertiary

    private fun GlassDarkColors.toScheme(): GlassColorScheme = GlassColorScheme(
        Primary = Primary,
        Secondary = Secondary,
        Tertiary = Tertiary,
        Surface = Surface,
        Background = Background,
        OnSurface = OnSurface,
        OnSurfaceVariant = OnSurfaceVariant,
        SurfaceContainerHigh = SurfaceContainerHigh,
        Outline = Outline,
        CardBackground = CardBackground,
        NavigationBarBackground = NavigationBarBackground,
        Error = Error,
        Success = Tertiary,
        Warning = Tertiary,
        SecondaryContainer = SecondaryContainer,
        OnPrimary = OnPrimary,
        Divider = Divider,
        TextSecondary = TextSecondary
    )

    private fun GlassLightColors.toScheme(): GlassColorScheme = GlassColorScheme(
        Primary = Primary,
        Secondary = Secondary,
        Tertiary = Tertiary,
        Surface = Surface,
        Background = Background,
        OnSurface = OnSurface,
        OnSurfaceVariant = OnSurfaceVariant,
        SurfaceContainerHigh = SurfaceContainerHigh,
        Outline = Outline,
        CardBackground = CardBackground,
        NavigationBarBackground = NavigationBarBackground,
        Error = Error,
        Success = Tertiary,
        Warning = Tertiary,
        SecondaryContainer = SecondaryContainer,
        OnPrimary = OnPrimary,
        Divider = Divider,
        TextSecondary = TextSecondary
    )
}

data class GlassColorScheme(
    val Primary: Color,
    val Secondary: Color,
    val Tertiary: Color,
    val Surface: Color,
    val Background: Color,
    val OnSurface: Color,
    val OnSurfaceVariant: Color,
    val SurfaceContainerHigh: Color,
    val Outline: Color,
    val CardBackground: Color,
    val NavigationBarBackground: Color,
    val Error: Color,
    val Success: Color,
    val Warning: Color,
    val SecondaryContainer: Color,
    val OnPrimary: Color,
    val Divider: Color,
    val TextSecondary: Color
)
