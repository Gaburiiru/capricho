package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.designsystem.mascot.PixelSprite

/**
 * Carcasa de "bicho virtual" de los 90: cuerpo rosa, pantalla LCD con líneas
 * de barrido, sombra dura y tres botones inferiores.
 * Los tres botones inferiores son funcionales:
 * - Botón Izquierda (<): Canal anterior.
 * - Botón Centro (⏻): Encendido / Apagado.
 * - Botón Derecha (>): Canal siguiente.
 */
@Composable
fun TamagotchiCard(
    modifier: Modifier = Modifier,
    brand: String? = "CAPRICHO",
    showControls: Boolean = true,
    isPoweredOn: Boolean = true,
    onPowerToggle: (() -> Unit)? = null,
    onPrevChannel: (() -> Unit)? = null,
    onNextChannel: (() -> Unit)? = null,
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

            // Pantalla LCD (con tono tenue si la TV está apagada)
            val lcdBg = if (isPoweredOn) colors.lcdBackground else Color(0xFF111827)
            val lcdBorder = if (isPoweredOn) colors.shellDark else Color(0xFF1F2937)

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(lcdBg, screenShape)
                    .border(3.dp, lcdBorder, screenShape)
                    .drawWithContent {
                        drawContent()
                        if (isPoweredOn) {
                            // Líneas de barrido de LCD encendido
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
                        } else {
                            // Pantalla apagada
                            drawRect(color = Color.Black.copy(alpha = 0.55f))
                        }
                    },
            ) {
                val displayColors = if (isPoweredOn) colors else colors.copy(lcdInk = Color(0xFF6B7280))
                screenContent(displayColors)
            }

            if (showControls) {
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Botón 1: Canal Anterior (<)
                    val prevEnabled = isPoweredOn && onPrevChannel != null
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(buttonShape)
                            .background(if (prevEnabled) colors.shellDark else colors.shellDark.copy(alpha = 0.5f), buttonShape)
                            .border(2.dp, colors.shellDark, buttonShape)
                            .semantics { contentDescription = "Canal anterior" }
                            .then(
                                if (prevEnabled) {
                                    Modifier.clickable(role = Role.Button, onClick = { onPrevChannel?.invoke() })
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        val arrowTint = if (prevEnabled) Color.White else Color.Gray
                        PixelSprite(
                            rows = TV_ARROW_LEFT,
                            palette = remember(arrowTint) { mapOf('X' to arrowTint) },
                            modifier = Modifier.width(8.dp), // 4 columnas x 2dp, misma escala que antes
                        )
                    }

                    // Botón 2: Símbolo Universal de Power (⏻)
                    val powerColor = if (isPoweredOn) Color(0xFF22C55E) else Color(0xFFEF4444)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(buttonShape)
                            .background(colors.shellDark, buttonShape)
                            .border(2.dp, colors.shellDark, buttonShape)
                            .semantics {
                                contentDescription = if (isPoweredOn) "Apagar TV" else "Encender TV"
                            }
                            .then(
                                if (onPowerToggle != null) {
                                    Modifier.clickable(role = Role.Button, onClick = { onPowerToggle?.invoke() })
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Símbolo ⏻ en pixel art 5x5
                        val powerSymbolRows = remember {
                            listOf(
                                "..X..",
                                "X.X.X",
                                "X...X",
                                "X...X",
                                ".XXX.",
                            )
                        }
                        val palette = remember(powerColor) { mapOf('X' to powerColor) }
                        PixelSprite(
                            rows = powerSymbolRows,
                            palette = palette,
                            modifier = Modifier.size(12.dp),
                        )
                    }

                    // Botón 3: Canal Siguiente (>)
                    val nextEnabled = isPoweredOn && onNextChannel != null
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(buttonShape)
                            .background(if (nextEnabled) colors.shellDark else colors.shellDark.copy(alpha = 0.5f), buttonShape)
                            .border(2.dp, colors.shellDark, buttonShape)
                            .semantics { contentDescription = "Siguiente canal" }
                            .then(
                                if (nextEnabled) {
                                    Modifier.clickable(role = Role.Button, onClick = { onNextChannel?.invoke() })
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        val arrowTint = if (nextEnabled) Color.White else Color.Gray
                        PixelSprite(
                            rows = TV_ARROW_RIGHT,
                            palette = remember(arrowTint) { mapOf('X' to arrowTint) },
                            modifier = Modifier.width(8.dp), // 4 columnas x 2dp, misma escala que antes
                        )
                    }
                }
            }
        }
    }
}

// Las flechas del set de íconos tienen una columna vacía (grilla de 5 con forma de 4), lo que las
// corre hacia adentro del botón. Acá se recorta esa columna para que el triángulo quede centrado.
private val TV_ARROW_LEFT = PixelIcon.ArrowLeft.rows.map { it.drop(1) }
private val TV_ARROW_RIGHT = PixelIcon.ArrowRight.rows.map { it.dropLast(1) }
