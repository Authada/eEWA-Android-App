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

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.europa.ec.resourceslogic.theme.values.secondaryContainerDisabledColor
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.wrap.WrapCard
import eu.europa.ec.uilogic.component.wrap.WrapIcon

@Composable
internal fun EncourageAddIdCardListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    WrapCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors().copy(
            containerColor = MaterialTheme.colorScheme.secondaryContainerDisabledColor,
        ),
        enabled = true,
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
                    iconData = AppIcons.Add,
                    modifier = Modifier.size(80.dp, 80.dp),
                    customTint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Preview(widthDp = 400, heightDp = 150)
@Preview(widthDp = 400, heightDp = 150, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EncourageIdCardAddingCardListItemPreview() {
    PreviewTheme {
        EncourageAddIdCardListItem(onClick = {})
    }
}