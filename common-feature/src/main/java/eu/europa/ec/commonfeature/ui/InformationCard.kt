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

package eu.europa.ec.commonfeature.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.InfoRoundIcon
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.utils.VSpacer

@Composable
fun InformationCard(
    title: String,
    subtitle: String,
    infoRoundIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors().copy(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        InfoContent(
            modifier = Modifier.fillMaxSize(),
            title = title,
            subtitle = subtitle,
            infoRoundIcon = infoRoundIcon
        )
    }
}

@Composable
private fun InfoContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    infoRoundIcon: @Composable () -> Unit
) {
    val titleColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = titleColor
            ),
            textAlign = TextAlign.Center
        )
        VSpacer.Large()

        infoRoundIcon()

        VSpacer.Large()

        Text(
            modifier = Modifier.padding(horizontal = SPACING_LARGE.dp),
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@ThemeModePreviews
@Composable
private fun InformationCardPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(SPACING_LARGE.dp),
            verticalArrangement = Arrangement.Center
        ) {
            InformationCard(
                modifier = Modifier.fillMaxSize(),
                title = stringResource(id = R.string.onboarding_title_0),
                subtitle = stringResource(id = R.string.onboarding_subtitle_0),
                infoRoundIcon = {
                    InfoRoundIcon(icon = AppIcons.StoreId, fitInsideTheCircle = false)
                }
            )
        }
    }
}
