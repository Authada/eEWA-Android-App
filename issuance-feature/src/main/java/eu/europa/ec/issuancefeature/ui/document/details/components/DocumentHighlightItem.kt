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

package eu.europa.ec.issuancefeature.ui.document.details.components

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
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.resourceslogic.theme.values.textSecondary
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData
import eu.europa.ec.uilogic.component.utils.VSpacer


@Composable
internal fun DocumentHighlightItem(
    itemData: InfoTextWithNameAndValueData,
    documentMetaData: DocumentMetaData?,
    modifier: Modifier = Modifier,
    invertTitleAndSubtitleStyles: Boolean = false
) {
    val titleColor =
        documentMetaData?.textColor.colorFromStringOrDefault(MaterialTheme.colorScheme.textPrimary)
    val subtitleColor = documentMetaData?.textColor.colorFromStringOrDefault(
        MaterialTheme.colorScheme.textSecondary,
        applyIfColorValid = {
            it.copy(alpha = 0.8f)
        })


    val titleStyle = MaterialTheme.typography.titleMedium.copy(
        color = titleColor
    )

    val subtitleStyle = MaterialTheme.typography.bodySmall.copy(
        color = subtitleColor
    )


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = itemData.title,
            style = if (invertTitleAndSubtitleStyles) subtitleStyle else titleStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        itemData.infoValues?.let { infoValues ->
            Column {
                infoValues.forEach { infoValue ->
                    VSpacer.ExtraSmall()

                    Text(
                        text = infoValue,
                        style = if (invertTitleAndSubtitleStyles) titleStyle else subtitleStyle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
