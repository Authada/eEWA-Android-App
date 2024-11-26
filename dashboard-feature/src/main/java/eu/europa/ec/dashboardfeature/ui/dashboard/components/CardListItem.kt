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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.commonfeature.model.toCardBackgroundColor
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.dashboardfeature.DashboardDocumentModel
import eu.europa.ec.dashboardfeature.ui.dashboard.Event
import eu.europa.ec.dashboardfeature.ui.dashboard.preview.DocumentDashboardUiPreviewParameter
import eu.europa.ec.resourceslogic.theme.values.allCorneredShapeLarge
import eu.europa.ec.resourceslogic.theme.values.colorFromStringOrDefault
import eu.europa.ec.resourceslogic.theme.values.secondaryContainerDisabledColor
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.ScalableText
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.WrapCard
import eu.europa.ec.uilogic.component.wrap.WrapImage


@Composable
internal fun CardListItem(
    dataItem: DashboardDocumentModel,
    modifier: Modifier = Modifier,
    onEventSend: (Event) -> Unit
) {
    val pidDesign = when(dataItem.documentIdentifier) {
        DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> true
        else -> false
    }
    val containerColor =
        if(pidDesign) {
            dataItem.documentIdentifier.toCardBackgroundColor()
        } else {
            dataItem.documentMetaData?.backgroundColor.colorFromStringOrDefault(dataItem.documentIdentifier.toCardBackgroundColor())
        }
    val borderStroke = if(pidDesign) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    WrapCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors().copy(
            containerColor = containerColor,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainerDisabledColor
        ),
        borderStroke = borderStroke,
        onClick = {
            onEventSend(
                Event.NavigateToDocument(
                    documentId = dataItem.documentId,
                    documentType = dataItem.documentIdentifier.docType,
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(SPACING_MEDIUM.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Header(documentName = dataItem.documentName)
            val passedDocumentMetaData = if (!pidDesign) {
                dataItem.documentMetaData
            } else {
                dataItem.documentMetaData?.copy(
                    textColor = MaterialTheme.colorScheme.primary.toString()
                )
            }
            dataItem.bottomDetail?.run {
                DocumentHighlightItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SPACING_MEDIUM.dp),
                    itemData = dataItem.bottomDetail,
                    documentMetaData = passedDocumentMetaData
                )
            }
        }
    }
}


@Composable
private fun Header(documentName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.allCorneredShapeLarge
                )
                .padding(horizontal = SPACING_SMALL.dp),
            contentAlignment = Alignment.Center
        ) {
            ScalableText(
                modifier = Modifier.padding(SPACING_SMALL.dp),
                text = documentName,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.allCorneredShapeLarge
                )
                .padding(vertical = SPACING_SMALL.dp, horizontal = SPACING_MEDIUM.dp),
            horizontalArrangement = Arrangement.spacedBy(SPACING_EXTRA_SMALL.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_document_valid),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )

            VSpacer.ExtraSmall()

            WrapImage(
                iconData = AppIcons.Check
            )
        }
    }
}


@ThemeModePreviews
@Composable
private fun NewCardListItemPreview(
    @PreviewParameter(DocumentDashboardUiPreviewParameter::class) document: DashboardDocumentModel
) {
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CardListItem(dataItem = document, onEventSend = {})
        }
    }
}