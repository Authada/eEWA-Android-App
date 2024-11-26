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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.commonfeature.ui.preview.DocumentUiPreviewParameter
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL


@Composable
internal fun RowScope.Highlights(
    highlightedFields: List<DocumentDetailsUi>,
    isDriversLicense: Boolean,
    metaData: DocumentMetaData?
) {
    val fieldsWithCorrectGrouping by remember {
        derivedStateOf {
            highlightedFields
                .reversed() //Reverse the list to start grouping from the end
                .chunked(2)
        }
    }

    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp),
    ) {
        fieldsWithCorrectGrouping.fastForEachReversed { row ->
            if (isDriversLicense) {
                row.ToHighlightsUiElements(metaData)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
                ) {
                    row.ToHighlightsUiElements(metaData, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
private fun List<DocumentDetailsUi>.ToHighlightsUiElements(
    metaData: DocumentMetaData?,
    modifier: Modifier = Modifier
) {
    this.forEach { documentDetailsUi ->
        when (documentDetailsUi) {
            is DocumentDetailsUi.DefaultItem -> {
                documentDetailsUi.itemData.infoValues
                    ?.toTypedArray()
                    ?.let { infoValues ->
                        val itemData = InfoTextWithNameAndValueData.create(
                            title = documentDetailsUi.itemData.title,
                            *infoValues
                        )
                        DocumentHighlightItem(
                            itemData,
                            metaData,
                            invertTitleAndSubtitleStyles = true,
                            modifier = modifier
                        )
                    }
            }

            else -> {} //Should not happen
        }
    }
}

@ThemeModePreviews
@Composable
private fun HighlightsPreview(
    @PreviewParameter(DocumentUiPreviewParameter::class) documentUi: DocumentUi
) {
    PreviewTheme {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            horizontalArrangement = Arrangement.spacedBy(SPACING_LARGE.dp)
        ) {
            Highlights(
                highlightedFields = documentUi.highlightedFields,
                metaData = documentUi.documentMetaData,
                isDriversLicense = false
            )
        }
    }
}