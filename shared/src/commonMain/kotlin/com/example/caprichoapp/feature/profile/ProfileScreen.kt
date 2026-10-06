package com.example.caprichoapp.feature.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelCutShape
import com.example.caprichoapp.core.designsystem.pixelFontFamily
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.domain.model.Profile

@Composable
fun ProfileScreen(
    profile: Profile?,
    onSignOut: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val cardShape = remember { PixelCutShape(3.dp) }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Mascot(MascotMood.Idle, Modifier.size(110.dp))
        Spacer(Modifier.height(16.dp))

        if (profile != null) {
            Text(
                text = profile.shownName,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (profile.shownName != profile.displayName) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, colors.outline, cardShape)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "SUELDO MENSUAL",
                    color = colors.onSurfaceVariant,
                    fontFamily = pixelFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$ ${profile.monthlySalary.toLong().formatThousands()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onSignOut) { Text("Cerrar sesión") }
    }
}
