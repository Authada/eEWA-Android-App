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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.model.toIcon
import eu.europa.ec.issuancefeature.ui.document.details.State
import eu.europa.ec.issuancefeature.ui.document.details.preview.DocumentDetailsPreviewParameter
import eu.europa.ec.resourceslogic.theme.values.bottomCorneredShapeSmall
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.utils.HSpacer
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.wrap.WrapIconButton
import eu.europa.ec.uilogic.component.wrap.WrapImage


internal val topBarMinHeight = 56.dp
internal val topBarMaxHeight = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBarDocumentDetails(
    isExpanded: Boolean,
    animatedHeight: Dp,
    documentUi: DocumentUi?,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            documentUi?.run {
                AnimatedVisibility(visible = !isExpanded) {
                    TopBarCollapsed(documentUi)
                }
            }
        },
        navigationIcon = {
            WrapIconButton(
                iconData = AppIcons.Close,
                customTint = MaterialTheme.colorScheme.primary,
                onClick = onCloseClick
            )
        },
        actions = {
            WrapIconButton(
                iconData = AppIcons.Delete,
                customTint = MaterialTheme.colorScheme.primary,
                onClick = onDeleteClick
            )
        }
    )
    if (isExpanded && documentUi != null) {
        ExpandedBody(documentUi, animatedHeight)
    }
}


@Composable
private fun ExpandedBody(documentUi: DocumentUi, animatedHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .padding(top = topBarMinHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeightIn(topBarMinHeight, topBarMaxHeight * 0.1f)
                .height(animatedHeight)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.bottomCorneredShapeSmall
                )
        )

        DocumentDetailsCard(
            modifier = Modifier.padding(horizontal = SPACING_LARGE.dp),
            documentUi = documentUi
        )
    }
}

@Composable
private fun TopBarCollapsed(documentUi: DocumentUi, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(topBarMinHeight)
    ) {
        WrapImage(
            iconData = documentUi.documentIdentifier.toIcon(),
            modifier = Modifier.size(32.dp)
        )
        HSpacer.Small()
        Text(
            text = documentUi.documentName,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarDocumentDetailsPreview(
    @PreviewParameter(DocumentDetailsPreviewParameter::class) state: State
) {
    PreviewTheme {
        TopBarDocumentDetails(
            isExpanded = true,
            animatedHeight = 300.dp,
            onDeleteClick = {},
            onCloseClick = {},
            documentUi = state.document
        )
    }
}