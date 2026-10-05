package com.example.caprichoapp.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import caprichoapp.shared.generated.resources.Res
import caprichoapp.shared.generated.resources.Nunito_Bold
import caprichoapp.shared.generated.resources.Nunito_ExtraBold
import caprichoapp.shared.generated.resources.Nunito_Regular
import caprichoapp.shared.generated.resources.Nunito_SemiBold
import org.jetbrains.compose.resources.Font

@Composable
fun nunitoFamily() = FontFamily(
    Font(Res.font.Nunito_Regular, FontWeight.Normal),
    Font(Res.font.Nunito_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Nunito_Bold, FontWeight.Bold),
    Font(Res.font.Nunito_ExtraBold, FontWeight.ExtraBold),
)

@Composable
fun caprichoTypography(): Typography {
    val nunito = nunitoFamily()
    return Typography(
        displayLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, lineHeight = 64.sp),
        headlineMedium = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        titleLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontFamily = nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
        bodyLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontFamily = nunito, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    )
}