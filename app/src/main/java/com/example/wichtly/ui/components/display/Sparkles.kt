package com.example.wichtly.ui.components.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wichtly.ui.theme.Gold
import com.example.wichtly.ui.theme.OnGoldContainer
import com.example.wichtly.ui.theme.WichtlyTheme
import kotlin.random.Random

/**
 * Zeichnet ein vierzackiges Sternchen (das goldene „Sternchen“) mit Mittelpunkt [center].
 *
 * @param radius Halbe Breite des Sternchens in Pixeln.
 * @param withRays Legt ein kleineres, um 45° gedrehtes Sternchen dahinter (achtzackige Variante).
 */
fun DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float = 1f,
    rotationDegrees: Float = 0f,
    withRays: Boolean = false,
) {
    rotate(rotationDegrees, pivot = center) {
        if (withRays) {
            rotate(45f, pivot = center) {
                drawPath(sparklePath(center, radius * 0.5f), color = color, alpha = alpha * 0.7f)
            }
        }
        drawPath(sparklePath(center, radius), color = color, alpha = alpha)
    }
}

/** Dieselbe Kurve wie im Mockup: M12 0 C13 7 17 11 24 12 … auf einem 24×24-Raster. */
private fun sparklePath(center: Offset, radius: Float): Path {
    val s = radius / 12f
    val x0 = center.x - radius
    val y0 = center.y - radius
    fun x(v: Float) = x0 + v * s
    fun y(v: Float) = y0 + v * s
    return Path().apply {
        moveTo(x(12f), y(0f))
        cubicTo(x(13f), y(7f), x(17f), y(11f), x(24f), y(12f))
        cubicTo(x(17f), y(13f), x(13f), y(17f), x(12f), y(24f))
        cubicTo(x(11f), y(17f), x(7f), y(13f), x(0f), y(12f))
        cubicTo(x(7f), y(11f), x(11f), y(7f), x(12f), y(0f))
        close()
    }
}

/** Breite des Streifens rechts im Seitenkopf, der für die Sternchen frei bleibt. */
val SparkleStripWidth: Dp = 64.dp

/**
 * Verteilt 1–3 Sternchen im oberen rechten Streifen ([SparkleStripWidth]) des Elements, wie im Mockup,
 * und mit 35 % Wahrscheinlichkeit ein weiteres kleines Sternchen unten rechts (insgesamt also 1–4).
 * Jedes Sternchen liegt vollständig im Element, damit die Top-Leiste es nie verdeckt und es nie in den
 * Titel ragt (der Titel hält den Streifen mit seinem rechten Innenabstand frei).
 * Die Anordnung ergibt sich aus [seed]: derselbe Titel bekommt immer dieselben Sternchen, verschiedene
 * Titel sehen in der Regel unterschiedlich aus (gleiche Hashwerte ergäben dasselbe Muster).
 */
fun Modifier.headerSparkles(seed: Int, color: Color = Gold, highlight: Color = OnGoldContainer): Modifier =
    drawWithContent {
        drawContent()
        val r = Random(seed)
        fun between(a: Float, b: Float) = a + (b - a) * r.nextFloat()
        val stripStart = (size.width - SparkleStripWidth.toPx()).coerceAtLeast(0f)
        val count = 1 + r.nextInt(3)
        repeat(count) { index ->
            val big = index == 0
            val sizeDp = if (big) between(14f, 22f) else between(6f, 11f)
            val radius = sizeDp.dp.toPx() / 2f
            // Zu schmales Element: kein Platz für den Funkel, coerceIn würde sonst mit leerem Bereich abstürzen
            if (size.width < 2 * radius) return@repeat
            // x und y werden begrenzt, damit das ganze Sternchen im Element liegt
            val x = (stripStart + (size.width - stripStart) * between(0.15f, 0.85f))
                .coerceIn(radius, size.width - radius)
            val y = (if (big) between(2f, 14f) else between(0f, 30f)).dp.toPx() + radius
            drawSparkle(
                center = Offset(x, y.coerceAtMost(size.height - radius)),
                radius = sizeDp.dp.toPx() / 2f,
                color = if (r.nextInt(3) == 0) highlight else color,
                alpha = between(0.55f, 1f),
                rotationDegrees = between(-25f, 25f),
                withRays = r.nextBoolean(),
            )
        }
        if (r.nextFloat() < 0.35f) {
            val sizeDp = between(6f, 9f)
            drawSparkle(
                center = Offset(
                    (size.width * between(0.84f, 0.96f)).coerceAtMost(size.width - sizeDp.dp.toPx() / 2f),
                    size.height - between(6f, 16f).dp.toPx(),
                ),
                radius = sizeDp.dp.toPx() / 2f,
                color = color,
                alpha = between(0.55f, 1f),
                rotationDegrees = between(-25f, 25f),
            )
        }
    }

/**
 * Verteilt 1–3 Sternchen über die Mitte einer Fläche (verwendet auf den farbigen Gruppenkarten ohne Foto).
 */
fun Modifier.areaSparkles(seed: Int, color: Color = Color.White): Modifier =
    drawWithContent {
        drawContent()
        val r = Random(seed)
        fun between(a: Float, b: Float) = a + (b - a) * r.nextFloat()
        val count = 1 + r.nextInt(3)
        repeat(count) { index ->
            val sizeDp = if (index == 0) between(14f, 22f) else between(6f, 11f)
            drawSparkle(
                center = Offset(size.width * between(0.30f, 0.78f), size.height * between(0.20f, 0.75f)),
                radius = sizeDp.dp.toPx() / 2f,
                color = color,
                alpha = between(0.55f, 1f),
                rotationDegrees = between(-25f, 25f),
                withRays = r.nextBoolean(),
            )
        }
    }

/** Ein einzelnes, frei platziertes Sternchen, z. B. neben dem Schriftzug „Wichtly“. */
@Composable
fun Sparkle(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = Gold,
    rotationDegrees: Float = 0f,
    alpha: Float = 1f,
) {
    Canvas(modifier = modifier.size(size)) {
        drawSparkle(
            center = center,
            radius = this.size.minDimension / 2f,
            color = color,
            alpha = alpha,
            rotationDegrees = rotationDegrees,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E2A22)
@Composable
private fun SparklesPreview() {
    WichtlyTheme {
        Box(modifier = Modifier.padding(24.dp).headerSparkles(seed = 7)) {
            Text("Alles bereit?", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 20.dp, end = 80.dp))
        }
    }
}
