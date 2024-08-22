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

package eu.europa.ec.dashboardfeature.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.europa.ec.dashboardfeature.ui.dashboard.Event
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.iconsDisabledColor
import eu.europa.ec.resourceslogic.theme.values.textSecondary
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.ScalableText
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.WrapCard
import eu.europa.ec.uilogic.component.wrap.WrapIcon

@Composable
internal fun EncourageAddIdCardListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    WrapCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        enabled = false,
        onClick = onClick
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SPACING_MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                WrapIcon(
                    iconData = AppIcons.Id,
                    modifier = Modifier.size(60.dp, 60.dp),
                    customTint = MaterialTheme.colorScheme.iconsDisabledColor
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                ScalableText(
                    text = stringResource(id = R.string.dashboard_document_add_pid_title),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                )
            }
            VSpacer.Small()
            Text(
                text = stringResource(id = R.string.dashboard_document_add_pid_description),
                style = MaterialTheme.typography.bodySmall
                    .copy(color = MaterialTheme.colorScheme.textSecondary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@ThemeModePreviews
@Composable
private fun EncourageIdCardAddingCardListItemPreview() {
    PreviewTheme {
        EncourageAddIdCardListItem(onClick = {})
    }
}