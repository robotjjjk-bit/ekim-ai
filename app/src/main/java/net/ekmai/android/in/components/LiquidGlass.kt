package net.ekmai.android.`in`.components

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android adaptation of Apple's Liquid Glass (iOS 26+ skill).
 *
 * Prinsip skill yang diadaptasi:
 * - glassEffect setelah layout/appearance modifiers (modifier order)
 * - GlassEffectContainer -> grouping + spacing konsisten
 * - .interactive() hanya untuk elemen tappable
 * - availability gate + fallback (API 31+ window blur, <31 translucent saja)
 * - shapes konsisten per fitur
 *
 * Tidak mengubah logika / navigasi / state. Hanya visual layer.
 */
object LiquidGlass {

    /** Alpha dasar material kaca regular. */
    const val SurfaceAlphaLight = 0.72f
    const val SurfaceAlphaDark = 0.62f

    /** Border highlight ala kaca (refleksi tepi atas). */
    const val BorderAlpha = 0.28f
    const val HighlightAlpha = 0.35f

    /**
     * Warna surface kaca yang adaptif tema.
     * Fallback aman untuk semua API level (tanpa blur).
     */
    @Composable
    fun surfaceColor(): Color {
        val scheme = MaterialTheme.colorScheme.surfaceContainerHigh
        val dark = isSystemInDarkTheme()
        return scheme.copy(alpha = if (dark) SurfaceAlphaDark else SurfaceAlphaLight)
    }

    @Composable
    fun sheetColor(): Color {
        val scheme = MaterialTheme.colorScheme.surface
        val dark = isSystemInDarkTheme()
        return scheme.copy(alpha = if (dark) 0.82f else 0.88f)
    }

    @Composable
    fun borderColor(): Color {
        val dark = isSystemInDarkTheme()
        return if (dark) Color.White.copy(alpha = 0.18f)
        else Color.White.copy(alpha = BorderAlpha)
    }
}

/**
 * Modifier kaca utama. Terapkan SETELAH size/padding/clip (sesuai skill: glassEffect last).
 *
 * @param shape harus konsisten per grup elemen (input 24dp, card 16dp, button Circle)
 * @param tint opsional, mis. model.tintColor.copy(alpha) untuk aksen
 */
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color? = null,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 8.dp,
    borderColor: Color? = null,
): Modifier = this
    .shadow(shadowElevation, shape, clip = false)
    .clip(shape)
    .then(
        if (tint != null) Modifier.background(tint, shape)
        else Modifier
    )
    .border(
        width = borderWidth,
        color = borderColor ?: Color.White.copy(alpha = 0.22f),
        shape = shape
    )

/**
 * Border kaca untuk tombol lingkaran (mic/send/voice).
 * Dipakai di atas background yang sudah ada, jadi tidak menimpa onClick/scale logic.
 */
fun Modifier.glassCircleBorder(
    borderColor: Color = Color.White.copy(alpha = 0.28f),
): Modifier = this.border(
    width = 1.dp,
    color = borderColor,
    shape = CircleShape
)

/**
 * Highlight spekulatif 1dp di tepi atas — meniru refleksi Liquid Glass.
 * Non-interaktif, murni visual. Taruh sebagai child pertama dalam container kaca.
 */
@Composable
fun GlassTopHighlight(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    alpha: Float = LiquidGlass.HighlightAlpha,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Window-level background blur (Android 12+ / API 31+).
 * Setara availability gate `#available(iOS 26, *)` pada skill:
 * - API 31+: blur behind dialogs/bottom sheets aktif
 * - API <31: no-op, fallback translucency dari surfaceColor() tetap tampil kaca
 *
 * Panggil sekali dari Theme (SideEffect). Tidak mengubah behavior lain.
 */
fun applyLiquidGlassWindowBlur(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            // Blur untuk konten di belakang window (dialogs, bottom sheets).
            activity.window.setBackgroundBlurRadius(48)
            activity.window.setBlurBehindRadius(32)
        } catch (_: Throwable) {
            // Fallback diam: tetap tampil sebagai kaca translusen biasa.
        }
    }
}
