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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.VSpacer

@Composable
fun InfoContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    infoRoundIcon: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.textPrimary
            ),
            textAlign = TextAlign.Center
        )
        VSpacer.Large()

        infoRoundIcon()

        VSpacer.Large()

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.textPrimary
            ),
            textAlign = TextAlign.Center
        )
    }
}

@ThemeModePreviews
@Composable
private fun InfoContentPreview() {
    PreviewTheme {

        Column(modifier = Modifier.fillMaxSize()) {
            InfoContent(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.issuance_success_title),
                subtitle = stringResource(id = R.string.issuance_success_subtitle),
                infoRoundIcon = {
                    InfoRoundIcon(icon = AppIcons.Success, fitInsideTheCircle = true)
                }
            )
        }
    }
}
