/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 *
 * Modified by AUTHADA GmbH August 2024
 * Copyright (c) 2024 AUTHADA GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.resourceslogic.theme.values

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

private val black: Color = Color(0xFF000000)

private val white100 = Color(0xFFFFFFFF)
private val white90 = Color(0xDEFFFFFF)
private val white80 = Color(0xFFF4F4F4)

private val purple100 = Color(0xFFF2F2FF)
private val purple90 = Color(0xFFD6D6FD)
private val purple70 = Color(0xFFADADFB)
private val purple60 = Color(0xFFAEAEFC)
private val purple50 = Color(0xFF6E23D2)

private val red100 = Color(0xFFFFDAD5)
private val red50 = Color(0xFFDA2C27)
private val red30 = Color(0xFF410002)

val green100 = Color(0xFFBFF6EC)
private val green60 = Color(0xFF80EDDA)
private val green50 = Color(0xFF00DCB4)
private val green40 = Color(0xFF0E3900)

private val orange60 = Color(0xFFFFB871)
private val orange50 = Color(0xFFF39626)

private val gray70 = Color(0xFFE4E4E4)
private val gray60 = Color(0xFF696969)
private val gray50 = Color(0xFF2F2F2F)
private val gray40 = Color(0xFF1F1F1F)

object ThemeColors {

    val lightColors = lightColorScheme(
        primary = purple50,
        onPrimary = white100,
        primaryContainer = purple90,
        secondary = purple90,
        onSecondary = black,
        secondaryContainer = purple100,
        tertiary = green50,
        tertiaryContainer = green60,
        onTertiaryContainer = black,
        error = red50,
        errorContainer = red100,
        onError = white100,
        onErrorContainer = red30,
        background = white100,
        onBackground = black,
        surface = purple90,
        onSurface = black,
        surfaceVariant = purple100,
        onSurfaceVariant = black,
        inverseOnSurface = white90,
        surfaceTint = white100,
        scrim = black
    )

    val darkColors = darkColorScheme(
        primary = purple70,
        primaryContainer = gray50,
        onPrimaryContainer = white80,
        secondary = purple90,
        secondaryContainer = gray60,
        onSecondaryContainer = white80,
        tertiary = green60,
        tertiaryContainer = green100,
        onTertiaryContainer = black,
        background = gray40,
        onBackground = purple90,
        surface = gray50,
        onSurface = purple90,
        surfaceVariant = gray50,
        onSurfaceVariant = white80,
        inverseOnSurface = black,
        surfaceTint = black,
    )
}

val ColorScheme.textPrimary: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        white80
    } else {
        black
    }

val ColorScheme.textSecondary: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        gray70
    } else {
        gray60
    }

val ColorScheme.textDisabledDark: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        gray40
    } else {
        white100
    }

val ColorScheme.warning: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        orange60
    } else {
        orange50
    }

val ColorScheme.iconsDisabledColor: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        purple90
    } else {
        purple60
    }

val ColorScheme.primaryButtonDisabledColor: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        gray60
    } else {
        purple60
    }


val ColorScheme.secondaryContainerDisabledColor: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        gray50
    } else {
        white80
    }

val ColorScheme.tooltipSecondaryTextColor: Color
    @Composable get() = gray60


val ColorScheme.success: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        green100
    } else {
        green50
    }

val ColorScheme.onSuccess: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        green40
    } else {
        white100
    }

fun String?.colorFromStringOrDefault(
    defaultValue: Color,
    applyIfColorValid: (Color) -> Unit = {}
): Color {
    return if (this != null) {
        try {
            Color(this.toColorInt()).apply(applyIfColorValid)
        } catch (e: Exception) {
            defaultValue
        }
    } else defaultValue
}