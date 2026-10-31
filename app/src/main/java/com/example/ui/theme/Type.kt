package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val PlusJakartaSans = try {
    FontFamily(
        Font(R.font.plus_jakarta_sans, FontWeight.Normal),
        Font(R.font.plus_jakarta_sans, FontWeight.Medium),
        Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
        Font(R.font.plus_jakarta_sans, FontWeight.Bold)
    )
} catch (e: Throwable) {
    FontFamily.SansSerif
}

val Fraunces = try {
    FontFamily(
        Font(R.font.fraunces, FontWeight.Normal),
        Font(R.font.fraunces, FontWeight.Medium),
        Font(R.font.fraunces, FontWeight.SemiBold),
        Font(R.font.fraunces, FontWeight.Bold)
    )
} catch (e: Throwable) {
    FontFamily.Serif
}

val LumoraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 2.sp
    )
)

/**
 * Headline in a serif font (Fraunces, fallback Serif) with ONE italic word.
 * E.g., "Welcome back" with "back" italic.
 */
@Composable
fun SerifHeadline(
    text: String,
    italicWord: String? = null,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF4F4F6),
    fontSize: TextUnit = 28.sp,
    lineHeight: TextUnit = 36.sp,
    textAlign: TextAlign = TextAlign.Start
) {
    val annotated = remember(text, italicWord) {
        styledHeadline(text, italicWord)
    }
    Text(
        text = annotated,
        style = TextStyle(
            fontFamily = Fraunces,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = FontWeight.Normal,
            color = color,
            textAlign = textAlign
        ),
        modifier = modifier
    )
}

/**
 * Small gold eyebrow label in caps with letter spacing (e.g. "ABOUT YOU")
 */
@Composable
fun GoldEyebrow(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 2.5.sp,
            color = Color(0xFFD9C59B)
        ),
        modifier = modifier
    )
}

/**
 * Generates an AnnotatedString where ONE targeted word is italicized in the Fraunces Serif font.
 */
fun styledHeadline(text: String, italicWord: String? = null): AnnotatedString {
    val targetWord = italicWord ?: run {
        val cleanWords = text.split(" ").filter { it.isNotBlank() }
        cleanWords.lastOrNull()?.trim('?', '!', '.', ',', ' ') ?: ""
    }

    val startIndex = if (targetWord.isNotEmpty()) text.indexOf(targetWord, ignoreCase = true) else -1
    return if (startIndex >= 0) {
        val endIndex = startIndex + targetWord.length
        buildAnnotatedString {
            append(text.substring(0, startIndex))
            withStyle(
                SpanStyle(
                    fontFamily = Fraunces,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal
                )
            ) {
                append(text.substring(startIndex, endIndex))
            }
            append(text.substring(endIndex))
        }
    } else {
        AnnotatedString(text)
    }
}
