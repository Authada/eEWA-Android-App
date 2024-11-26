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
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.resourceslogic.theme.values.colorFromStringOrDefault
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData


@Composable
internal fun DocumentHighlightItem(
    itemData: InfoTextWithNameAndValueData,
    documentMetaData: DocumentMetaData?,
    modifier: Modifier = Modifier
) {
    val titleColor = documentMetaData?.textColor.colorFromStringOrDefault(
        MaterialTheme.colorScheme.primary,
        applyIfColorValid = {
            it.copy(alpha = 0.8f)
        })


    val valueColor =
        documentMetaData?.textColor.colorFromStringOrDefault(MaterialTheme.colorScheme.primary)


    val titleStyle = MaterialTheme.typography.bodyMedium.copy(
        color = titleColor
    )

    val valueStyle = MaterialTheme.typography.titleMedium.copy(
        color = valueColor
    )


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = itemData.title,
            style = titleStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        itemData.infoValues?.firstOrNull()?.let { infoValue ->
            Text(
                text = infoValue,
                style = valueStyle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}
