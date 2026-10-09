package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.CaprichoTheme

/** Logotipo de la app en fuente pixel, para Splash y Login. */
@Composable
fun PixelWordmark(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = "CAPRICHO",
        color = color,
        style = CaprichoTheme.pixelText.title.copy(fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = 2.sp),
        maxLines = 1,
        modifier = modifier,
    )
}
