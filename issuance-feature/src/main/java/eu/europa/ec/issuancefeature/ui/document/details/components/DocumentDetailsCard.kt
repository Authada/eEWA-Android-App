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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.model.toCardBackgroundColor
import eu.europa.ec.commonfeature.model.toIcon
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.issuancefeature.ui.document.details.State
import eu.europa.ec.issuancefeature.ui.document.details.preview.DocumentDetailsPreviewParameter
import eu.europa.ec.resourceslogic.theme.values.colorFromStringOrDefault
import eu.europa.ec.resourceslogic.theme.values.secondaryContainerDisabledColor
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData
import eu.europa.ec.uilogic.component.DocumentMetaDataImage
import eu.europa.ec.uilogic.component.UserImageOrPlaceholder
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.uilogic.component.wrap.WrapCard


private val imageSize = 64.dp
private val cardHeight = 250.dp

@Composable
internal fun DocumentDetailsCard(
    modifier: Modifier = Modifier,
    documentUi: DocumentUi,
    contentPadding: PaddingValues = PaddingValues(all = SPACING_LARGE.dp)
) {
    val containerColor =
        documentUi.documentMetaData?.backgroundColor.colorFromStringOrDefault(
            documentUi.documentIdentifier.toCardBackgroundColor()
        )

    WrapCard(
        modifier = modifier.height(cardHeight),
        colors = CardDefaults.elevatedCardColors().copy(
            containerColor = containerColor,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainerDisabledColor
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                SPACING_SMALL.dp,
                alignment = Alignment.Bottom
            ),
            modifier = Modifier.padding(contentPadding)
        ) {
            TopPart(documentUi)
            HorizontalDivider(color = MaterialTheme.colorScheme.background)
            BottomPart(documentUi)
        }
    }
}

@Composable
private fun TopPart(documentUi: DocumentUi, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SPACING_LARGE.dp)
    ) {
        DocumentMetaDataImage(
            metaData = documentUi.documentMetaData?.logo,
            errorImage = documentUi.documentIdentifier.toIcon(),
            modifier = Modifier.size(imageSize)
        )

        val title = documentUi.documentMetaData?.documentName ?: documentUi.documentName

        DocumentHighlightItem(
            modifier = Modifier.weight(1f),
            documentMetaData = documentUi.documentMetaData,
            itemData = InfoTextWithNameAndValueData.create(
                title = title,
                documentUi.description ?: documentUi.documentIssuer
            )
        )
    }
}

@Composable
private fun ColumnScope.BottomPart(
    documentUi: DocumentUi,
    modifier: Modifier = Modifier
) {
    val isDriversLicense = documentUi.documentIdentifier == DocumentIdentifier.MDL
    val alignment = if (isDriversLicense) Alignment.CenterVertically else Alignment.Bottom
    Row(
        verticalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(SPACING_LARGE.dp)
    ) {
        if (isDriversLicense) {
            UserImageOrPlaceholder(
                userBase64Image = documentUi.base64Image,
                modifier = modifier.sizeIn(maxHeight = imageSize, maxWidth = imageSize)
            )
        }
        Highlights(
            highlightedFields = documentUi.highlightedFields,
            isDriversLicense = isDriversLicense,
            metaData = documentUi.documentMetaData
        )
    }
}

@PreviewLightDark
@Composable
private fun DocumentDetailsCardPreview(
    @PreviewParameter(DocumentDetailsPreviewParameter::class) state: State
) {
    PreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            DocumentDetailsCard(
                modifier = Modifier.fillMaxWidth(),
                documentUi = state.document!!
            )
        }
    }
}