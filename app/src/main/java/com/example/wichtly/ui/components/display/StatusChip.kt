package com.example.wichtly.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wichtly.ui.theme.WichtlyTheme
import com.example.wichtly.ui.theme.Spacing

/** Color sets for [StatusChip]. */
enum class ChipTone { Gold, Berry, Success, Neutral, OnImage }

/**
 * Gift-tag shaped chip: pointed left end with a small punched hole, rounded right end.
 *
 * @param holeColor Color of the surface the chip sits on (the "hole" shows it).
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.Gold,
    holeColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        ChipTone.Gold -> colors.primary.copy(alpha = 0.16f) to colors.primary
        ChipTone.Berry -> colors.secondaryContainer to colors.onSecondaryContainer
        ChipTone.Success -> WichtlyThemeColors.success().copy(alpha = 0.16f) to WichtlyThemeColors.success()
        ChipTone.Neutral -> colors.onSurface.copy(alpha = 0.08f) to colors.onSurfaceVariant
        ChipTone.OnImage -> Color.Black.copy(alpha = 0.38f) to Color.White
    }
    val hole = if (tone == ChipTone.OnImage) Color.White.copy(alpha = 0.5f) else holeColor
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
        color = content,
        maxLines = 1,
        modifier = modifier
            .clip(GiftTagShape)
            .background(container)
            .drawWithContent {
                drawContent()
                drawCircle(color = hole, radius = 2.dp.toPx(), center = Offset(8.dp.toPx(), size.height / 2f))
            }
            .padding(start = Spacing.relaxed, end = Spacing.inline, top = Spacing.tight, bottom = Spacing.tight),
    )
}

/** Small bridge so the chip can read the extended success color. */
private object WichtlyThemeColors {
    @Composable
    fun success(): Color = com.example.wichtly.ui.theme.WichtlyTheme.extendedColors.success
}

/** Tag outline: point on the left, rounded corners on the right. */
object GiftTagShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val notch = with(density) { 7.dp.toPx() }
        val r = with(density) { 6.dp.toPx() }.coerceAtMost(size.height / 2f)
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(notch, 0f)
            lineTo(w - r, 0f)
            quadraticTo(w, 0f, w, r)
            lineTo(w, h - r)
            quadraticTo(w, h, w - r, h)
            lineTo(notch, h)
            lineTo(0f, h / 2f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF15392E)
@Composable
private fun StatusChipPreview() {
    WichtlyTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(text = "Gespeichert")
            StatusChip(text = "Bereit", tone = ChipTone.Success)
            StatusChip(text = "Teilnehmer fehlen", tone = ChipTone.Berry)
            StatusChip(text = "Offen", tone = ChipTone.Neutral)
        }
    }
}
