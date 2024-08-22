/*
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

package eu.europa.ec.uilogic.component.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.IconData
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.wrap.WrapImage

@Composable
fun InfoRoundIcon(
    icon: IconData,
    fitInsideTheCircle: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1f)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        val modifierFit = Modifier.fillMaxSize(0.6f)
        val modifierNonFit = Modifier.fillMaxSize()

        WrapImage(
            modifier = if(fitInsideTheCircle) modifierFit else modifierNonFit,
            iconData = icon
        )
    }
}


@ThemeModePreviews
@Composable
private fun InfoRoundIconPreview() {
    PreviewTheme {
        Column {
            InfoRoundIcon(
                icon = AppIcons.StoreId,
                fitInsideTheCircle = false
            )

            InfoRoundIcon(
                icon = AppIcons.StoreId,
                fitInsideTheCircle = true
            )

            InfoRoundIcon(
                icon = AppIcons.Success,
                fitInsideTheCircle = false
            )

            InfoRoundIcon(
                icon = AppIcons.Success,
                fitInsideTheCircle = true
            )
        }
    }
}
