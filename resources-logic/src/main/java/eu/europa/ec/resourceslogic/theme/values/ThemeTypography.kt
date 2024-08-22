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

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.templates.ThemeTextStyle
import eu.europa.ec.resourceslogic.theme.templates.ThemeTypographyTemplate


internal val NunitoLight = Font(
    resId = R.font.nunito_light,
    weight = FontWeight.W300,
    style = FontStyle.Normal,
)
internal val NunitoRegular = Font(
    resId = R.font.nunito_regular,
    weight = FontWeight.W400,
    style = FontStyle.Normal,
)
internal val NunitoBold = Font(
    resId = R.font.nunito_bold,
    weight = FontWeight.W500,
    style = FontStyle.Normal,
)

internal val NunitoExtraBold = Font(
    resId = R.font.nunito_extra_bold,
    weight = FontWeight.W800,
    style = FontStyle.Normal,
)


val Typography = ThemeTypographyTemplate(
    displayLarge = ThemeTextStyle(
        fontFamily = listOf(NunitoLight),
        fontSize = 96,
        letterSpacing = -0.14f,
        textAlign = TextAlign.Start
    ),
    displayMedium = ThemeTextStyle(
        fontFamily = listOf(NunitoLight),
        fontSize = 60,
        letterSpacing = -0.03f,
        textAlign = TextAlign.Start
    ),
    displaySmall = ThemeTextStyle(
        fontFamily = listOf(NunitoRegular),
        fontSize = 48,
        letterSpacing = 0f,
        textAlign = TextAlign.Start
    ),
    headlineLarge = ThemeTextStyle(),
    headlineMedium = ThemeTextStyle(
        fontFamily = listOf(NunitoRegular),
        fontSize = 30,
        letterSpacing = 0.01f,
        textAlign = TextAlign.Start
    ),
    headlineSmall = ThemeTextStyle(
        fontFamily = listOf(NunitoExtraBold),
        fontSize = 24,
        letterSpacing = 0f,
        textAlign = TextAlign.Start
    ),
    titleLarge = ThemeTextStyle(
        fontFamily = listOf(NunitoBold),
        fontSize = 20,
        letterSpacing = 0f,
        textAlign = TextAlign.Start
    ),
    titleMedium = ThemeTextStyle(
        fontFamily = listOf(NunitoBold),
        fontSize = 16,
        letterSpacing = 0f,
        textAlign = TextAlign.Start
    ),
    titleSmall = ThemeTextStyle(
        fontFamily = listOf(NunitoBold),
        fontSize = 14,
        letterSpacing = 0f,
        textAlign = TextAlign.Start
    ),
    bodyLarge = ThemeTextStyle(
        fontFamily = listOf(NunitoRegular),
        fontSize = 16,
        letterSpacing = 0.01f,
        textAlign = TextAlign.Start
    ),
    bodyMedium = ThemeTextStyle(
        fontFamily = listOf(NunitoRegular),
        fontSize = 14,
        letterSpacing = 0f,
        textAlign = TextAlign.Start
    ),
    bodySmall = ThemeTextStyle(
        fontFamily = listOf(NunitoRegular),
        fontSize = 12,
        letterSpacing = 0f,
        textAlign = TextAlign.Start
    ),
    labelLarge = ThemeTextStyle(
        fontFamily = listOf(NunitoBold),
        fontSize = 14,
        letterSpacing = 0.02f,
        textAlign = TextAlign.Start
    ),
    labelMedium = ThemeTextStyle(),
    labelSmall = ThemeTextStyle(
        fontFamily = listOf(NunitoRegular),
        fontSize = 10,
        letterSpacing = 0.01f,
        textAlign = TextAlign.Start
    )
)


/*
--M2--         --M3--                  --DS--
h1          displayLarge        H1
h2	        displayMedium       H2
h3	        displaySmall        H3
N/A	        headlineLarge       N/A
h4	        headlineMedium      H4
h5	        headlineSmall       H5
h6	        titleLarge          Roboto Medium 20dp
subtitle1   titleMedium         Roboto Medium 16dp
subtitle2   titleSmall          Roboto Medium 14dp
body1	    bodyLarge           Body 1
body2	    bodyMedium          Body 2
caption	    bodySmall           Roboto Regular 12dp
button	    labelLarge          BUTTON
N/A	        labelMedium         N/A
overline    labelSmall          OVERLINE
*/