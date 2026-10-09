package com.example.caprichoapp.core.designsystem.mascot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun PixelSprite(
    rows: List<String>,
    palette: Map<Char, Color>,
    modifier: Modifier = Modifier,
) {
    val cols = rows.first().length
    Canvas(modifier.aspectRatio(cols.toFloat() / rows.size)) {
        val cell = size.width / cols
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, char ->
                palette[char]?.let { color ->
                    drawRect(
                        color = color,
                        topLeft = Offset(x * cell, y * cell),
                        size = Size(cell + 0.5f, cell + 0.5f), // evita líneas finas entre píxeles
                    )
                }
            }
        }
    }
}