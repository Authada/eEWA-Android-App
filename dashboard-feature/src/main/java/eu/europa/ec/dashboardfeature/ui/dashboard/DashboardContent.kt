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

package eu.europa.ec.dashboardfeature.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import eu.europa.ec.dashboardfeature.DashboardDocumentModel
import eu.europa.ec.dashboardfeature.isProxy
import eu.europa.ec.dashboardfeature.ui.dashboard.components.CardListItem
import eu.europa.ec.dashboardfeature.ui.dashboard.components.EncourageAddIdCardListItem
import eu.europa.ec.dashboardfeature.ui.dashboard.components.ProxyCardListItem
import eu.europa.ec.dashboardfeature.ui.dashboard.preview.DocumentDashboardUiPreviewParameter
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.bottomCorneredShapeSmall
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.ScalableText
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.HSpacer
import eu.europa.ec.uilogic.component.utils.SIZE_MEDIUM
import eu.europa.ec.uilogic.component.utils.SIZE_SMALL
import eu.europa.ec.uilogic.component.utils.SPACING_EXTRA_LARGE
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.BUTTON_HEIGHT_FAT
import eu.europa.ec.uilogic.component.wrap.WrapIcon
import eu.europa.ec.uilogic.component.wrap.WrapIconButton
import eu.europa.ec.uilogic.component.wrap.WrapImage
import eu.europa.ec.uilogic.component.wrap.WrapPrimaryButton
import eu.europa.ec.uilogic.component.wrap.WrapSecondaryButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val cardHeight = 200.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardContent(
    state: State,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit,
    onNavigationRequested: (navigationEffect: Effect.Navigation) -> Unit,
    paddingValues: PaddingValues,
    coroutineScope: CoroutineScope,
    modalBottomSheetState: SheetState
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Title section.
        Title(
            onEventSend = onEventSend,
            paddingValues = paddingValues
        )

        DocumentsList(
            documents = state.documents,
            onEventSend = onEventSend,
            paddingValues = paddingValues,
            isLoading = state.isLoading,
            modifier = Modifier.weight(1f)
        )

        FabContent(
            paddingValues = paddingValues,
            onEventSend = onEventSend,
            isAddButtonVisible = !state.isLoading //TODO For now do not check for documents In future//!state.isLoading && state.documents.isEmpty()
        )
    }

    LaunchedEffect(Unit) {
        effectFlow.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> onNavigationRequested(effect)

                is Effect.CloseBottomSheet -> {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }.invokeOnCompletion {
                        if (!modalBottomSheetState.isVisible) {
                            onEventSend(Event.BottomSheet.UpdateBottomSheetState(isOpen = false))
                        }
                    }
                }

                is Effect.ShowBottomSheet -> {
                    onEventSend(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))
                }
            }
        }.collect()
    }
}

@Composable
private fun FabContent(
    modifier: Modifier = Modifier,
    onEventSend: (Event) -> Unit,
    paddingValues: PaddingValues,
    isAddButtonVisible: Boolean
) {
    val titleSmallTextStyle = MaterialTheme.typography.titleSmall

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                bottom = SPACING_MEDIUM.dp,
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
            ),
        horizontalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isAddButtonVisible) {
            WrapPrimaryButton(
                modifier = Modifier.weight(1f),
                height = BUTTON_HEIGHT_FAT,
                onClick = { onEventSend(Event.Button.AddDocumentPressed) }) {
                ScalableText(
                    text = stringResource(id = R.string.dashboard_primary_fab_text),
                    textStyle = titleSmallTextStyle
                )
            }
        }


        WrapSecondaryButton(
            modifier = Modifier.weight(1f),
            height = BUTTON_HEIGHT_FAT,
            onClick = { onEventSend(Event.Button.QrPressed) }) {
            WrapIcon(
                modifier = Modifier.size(SIZE_MEDIUM.dp),
                iconData = AppIcons.QR
            )
            HSpacer.ExtraSmall()
            ScalableText(
                text = stringResource(id = R.string.dashboard_secondary_fab_text),
                textStyle = titleSmallTextStyle.copy(
                    color = MaterialTheme.colorScheme.textPrimary
                )
            )
        }
    }
}

@Composable
private fun Title(
    onEventSend: (Event) -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.bottomCorneredShapeSmall
            )
            .padding(
                PaddingValues(
                    top = SPACING_EXTRA_LARGE.dp,
                    bottom = SPACING_EXTRA_LARGE.dp,
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            WrapImage(
                iconData = AppIcons.User,
                Modifier
                    .clip(RoundedCornerShape(SIZE_SMALL.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = SPACING_MEDIUM.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.dashboard_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.textPrimary
                )
            }

            WrapIconButton(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.7.dp)
                ),
                iconData = AppIcons.VerticalMore,
                customTint = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    onEventSend(Event.OptionsPressed)
                }
            )
        }
    }
}

@Composable
private fun DocumentsList(
    documents: List<DashboardDocumentModel>,
    onEventSend: (Event) -> Unit,
    paddingValues: PaddingValues,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            ),
        verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp)
    ) {

        item {
            VSpacer.Large()
        }
        if (documents.isNotEmpty()) {
            items(
                documents.size,
                key = { documents[it].documentId }
            ) { index ->
                with(documents[index]) {
                    TileCard(
                        documentUiItem = this,
                        onEventSend = onEventSend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cardHeight)
                    )
                }
            }
        } else if (!isLoading) {
            item {
                EncourageAddIdCardListItem(
                    onClick = { onEventSend(Event.Button.AddDocumentPressed) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)

                )
            }
        }

        item {
            VSpacer.Large()
        }
    }
}

@Composable
private fun TileCard(
    documentUiItem: DashboardDocumentModel,
    onEventSend: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    if (documentUiItem.isProxy) {
        ProxyCardListItem(
            modifier = modifier,
            onClick = {
                onEventSend(
                    Event.OnProxyPressed(
                        documentUiItem.documentId,
                        documentUiItem.documentIdentifier.docType
                    )
                )
            }
        )
    } else {
        CardListItem(
            modifier = modifier,
            dataItem = documentUiItem,
            onEventSend = onEventSend
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun DashboardScreenWithSecureElementPreview() {
    PreviewTheme {
        DashboardContent(
            state = State(
                isLoading = false,
                error = null,
                documents = emptyList()
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(SPACING_LARGE.dp),
            coroutineScope = rememberCoroutineScope(),
            modalBottomSheetState = rememberModalBottomSheetState()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun DashboardScreenWithSeveralDocumentsPreview(
    @PreviewParameter(DocumentDashboardUiPreviewParameter::class) document: DashboardDocumentModel
) {
    PreviewTheme {
        DashboardContent(
            state = State(
                isLoading = false,
                error = null,
                documents = listOf(document)
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(SPACING_LARGE.dp),
            coroutineScope = rememberCoroutineScope(),
            modalBottomSheetState = rememberModalBottomSheetState()
        )
    }
}
