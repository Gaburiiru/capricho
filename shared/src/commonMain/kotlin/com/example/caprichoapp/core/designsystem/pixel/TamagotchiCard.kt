package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.CaprichoTheme

/**
 * Carcasa de "bicho virtual" de los 90: cuerpo rosa, pantalla LCD con líneas
 * de barrido, sombra dura y tres botoncitos decorativos.
 * El contenido de la pantalla recibe los colores del dispositivo.
 */
@Composable
fun TamagotchiCard(
    modifier: Modifier = Modifier,
    brand: String? = "CAPRICHO",
    showControls: Boolean = true,
    screenContent: @Composable BoxScope.(TamagotchiColors) -> Unit,
) {
    val colors = tamagotchiColors()
    val shellShape = remember { PixelCutShape(4.dp) }
    val screenShape = remember { PixelCutShape(3.dp) }
    val buttonShape = remember { PixelCutShape(3.dp) }

    Box(modifier.padding(end = 6.dp, bottom = 6.dp)) {
        // Sombra dura desplazada
        Box(
            Modifier
                .matchParentSize()
                .offset(6.dp, 6.dp)
                .background(colors.shadow, shellShape),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.shell, shellShape)
                .border(4.dp, colors.shellDark, shellShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (brand != null) {
                Text(
                    text = brand,
                    color = colors.shellDark,
                    style = CaprichoTheme.pixelText.tag.copy(letterSpacing = 3.sp),
                )
                Spacer(Modifier.height(10.dp))
            }

            // Pantalla LCD
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.lcdBackground, screenShape)
                    .border(3.dp, colors.shellDark, screenShape)
                    .drawWithContent {
                        drawContent()
                        // Líneas de barrido muy tenues, como un LCD viejo
                        val line = 2.dp.toPx()
                        val gap = 4.dp.toPx()
                        var y = 0f
                        while (y < size.height) {
                            drawRect(
                                color = Color.Black.copy(alpha = 0.07f),
                                topLeft = Offset(0f, y),
                                size = Size(size.width, line),
                            )
                            y += gap
                        }
                    },
            ) {
                screenContent(colors)
            }

            if (showControls) {
                Spacer(Modifier.height(14.dp))
                // Botones decorativos
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    repeat(3) {
                        Box(Modifier.size(20.dp).background(colors.shellDark, buttonShape))
                    }
                }
            }
        }
    }
}
