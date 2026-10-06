package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.caprichoapp.core.designsystem.mascot.PixelSprite

/** Íconos pixel art de un solo color ('X' = tinta). Evita depender de material-icons. */
enum class PixelIcon(val rows: List<String>) {
    Home(
        listOf(
            "....X....",
            "...XXX...",
            "..XXXXX..",
            ".XXXXXXX.",
            "XXXXXXXXX",
            ".XXXXXXX.",
            ".XXX.XXX.",
            ".XXX.XXX.",
            ".XXX.XXX.",
        ),
    ),
    History(
        listOf(
            ".........",
            ".......X.",
            ".......X.",
            "...X...X.",
            "...X.X.X.",
            ".X.X.X.X.",
            ".X.X.X.X.",
            ".X.X.X.X.",
            "XXXXXXXXX",
        ),
    ),
    Goals(
        listOf(
            ".X.......",
            ".XXXXXX..",
            ".XXXXXXX.",
            ".XXXXXX..",
            ".X.......",
            ".X.......",
            ".X.......",
            ".X.......",
            "XXX......",
        ),
    ),
    Profile(
        listOf(
            "...XXX...",
            "..XXXXX..",
            "..XXXXX..",
            "..XXXXX..",
            "...XXX...",
            ".........",
            ".XXXXXXX.",
            "XXXXXXXXX",
            "XXXXXXXXX",
        ),
    ),
    Heart(
        listOf(
            ".XX.XX.",
            "XXXXXXX",
            "XXXXXXX",
            ".XXXXX.",
            "..XXX..",
            "...X...",
        ),
    ),
    ArrowRight(
        listOf(
            "X....",
            "XX...",
            "XXX..",
            "XXXX.",
            "XXX..",
            "XX...",
            "X....",
        ),
    ),
}

@Composable
fun PixelIconImage(
    icon: PixelIcon,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val palette = remember(tint) { mapOf('X' to tint) }
    PixelSprite(rows = icon.rows, palette = palette, modifier = modifier)
}
