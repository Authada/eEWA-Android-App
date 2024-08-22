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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.model.toIcon
import eu.europa.ec.corelogic.model.DocumentType
import eu.europa.ec.dashboardfeature.ui.dashboard.Event
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.resourceslogic.theme.values.textSecondary
import eu.europa.ec.resourceslogic.theme.values.warning
import eu.europa.ec.uilogic.component.ScalableText
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.WrapCard
import eu.europa.ec.uilogic.component.wrap.WrapIcon
import eu.europa.ec.uilogic.extension.IconWarningIndicator

@Composable
internal fun CardListItem(
    dataItem: DocumentUi,
    modifier: Modifier = Modifier,
    onEventSend: (Event) -> Unit
) {
    WrapCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = {
            onEventSend(
                Event.NavigateToDocument(
                    documentId = dataItem.documentId,
                    documentType = dataItem.documentType.nameSpace,
                )
            )
        }
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
                    iconData = dataItem.documentType.toIcon(),
                    modifier = Modifier.size(60.dp, 60.dp),
                    customTint = MaterialTheme.colorScheme.primary
                )
                if (dataItem.documentHasExpired) {
                    IconWarningIndicator(
                        backgroundColor = MaterialTheme.colorScheme.background
                    )
                }
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                ScalableText(
                    text = dataItem.documentName,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.textPrimary
                    )
                )
            }
            VSpacer.Small()
            ExpiryDate(
                expirationDate = dataItem.documentExpirationDateFormatted,
                hasExpired = dataItem.documentHasExpired
            )
        }
    }
}

@Composable
private fun ExpiryDate(
    expirationDate: String,
    hasExpired: Boolean
) {
    val textStyle = MaterialTheme.typography.bodySmall
        .copy(color = MaterialTheme.colorScheme.textSecondary)
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasExpired) {
            val annotatedText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                        color = MaterialTheme.colorScheme.warning
                    )
                ) {
                    append(stringResource(id = R.string.dashboard_document_has_expired_one))
                }

                append(stringResource(id = R.string.dashboard_document_has_expired_two))
            }
            Text(text = annotatedText, style = textStyle)
        } else {
            Text(
                text = stringResource(id = R.string.dashboard_document_has_not_expired),
                style = textStyle
            )
        }
        Text(text = expirationDate, style = textStyle)
    }
}


@ThemeModePreviews
@Composable
private fun CardListItemPreview() {
    PreviewTheme {
        val docUi = DocumentUi(
            documentId = "0",
            documentName = "National ID",
            documentType = DocumentType.PID,
            documentExpirationDateFormatted = "30 Mar 2050",
            documentHasExpired = false,
            documentImage = "image1",
            documentDetails = emptyList(),
        )

        CardListItem(dataItem = docUi, onEventSend = {})
    }
}