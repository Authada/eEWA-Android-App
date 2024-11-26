/*
 * Copyright (c) 2023 European Commission
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
 *
 * Modified by AUTHADA GmbH August 2024
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

package eu.europa.ec.issuancefeature.ui.document.details

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import eu.europa.ec.issuancefeature.ui.document.details.components.DetailItem
import eu.europa.ec.commonfeature.ui.document_details.model.Priority
import eu.europa.ec.issuancefeature.ui.document.details.components.TopBarDocumentDetails
import eu.europa.ec.issuancefeature.ui.document.details.components.topBarMaxHeight
import eu.europa.ec.issuancefeature.ui.document.details.components.topBarMinHeight
import eu.europa.ec.issuancefeature.ui.document.details.preview.DocumentDetailsPreviewParameter
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.utils.LifecycleEffect
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.wrap.DialogBottomSheet
import eu.europa.ec.uilogic.component.wrap.WrapModalBottomSheet
import eu.europa.ec.uilogic.component.wrap.WrapSecondaryButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Composable
internal fun DocumentDetailsScreen(
    navController: NavController,
    viewModel: DocumentDetailsViewModel
) {
    val state = viewModel.viewState.value

    DocumentDetailsScreen(state = state,
        effectFlow = viewModel.effect,
        onEventSend = { viewModel.setEvent(it) },
        onNavigationRequested = { navigationEffect ->
            handleNavigationEffect(navigationEffect, navController)
        }
    )

    LifecycleEffect(
        lifecycleOwner = LocalLifecycleOwner.current,
        lifecycleEvent = Lifecycle.Event.ON_RESUME
    ) {
        viewModel.setEvent(Event.Init)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentDetailsScreen(
    state: State,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit,
    onNavigationRequested: (Effect.Navigation) -> Unit,
) {
    val isBottomSheetOpen = state.isBottomSheetOpen
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var expandHeight by remember { mutableStateOf(topBarMaxHeight) }

    val animatedHeight: Dp by animateDpAsState(targetValue = expandHeight, label = "")

    val isExpanded by remember {
        derivedStateOf { expandHeight > topBarMinHeight }
    }

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                expandHeight = (expandHeight + delta.dp).coerceIn(topBarMinHeight, topBarMaxHeight)
                return Offset.Zero
            }
        }
    }


    ContentScreen(
        modifier = Modifier.nestedScroll(scrollConnection),
        isLoading = state.isLoading,
        contentErrorConfig = state.error,
        navigatableAction = state.navigatableAction,
        onBack = state.onBackAction,
        topBar = {
            TopBarDocumentDetails(
                isExpanded = isExpanded,
                animatedHeight = animatedHeight,
                documentUi = state.document,
                onCloseClick = {
                    onEventSend(Event.Pop)
                },
                onDeleteClick = {
                    onEventSend(Event.DeleteDocumentPressed)
                }
            )
        }
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = effectFlow,
            onEventSend = { onEventSend(it) },
            onNavigationRequested = onNavigationRequested,
            paddingValues = paddingValues,
            topBarHeight = animatedHeight,
            coroutineScope = scope,
            modalBottomSheetState = bottomSheetState
        )

        if (isBottomSheetOpen) {
            WrapModalBottomSheet(
                onDismissRequest = {
                    onEventSend(
                        Event.BottomSheet.UpdateBottomSheetState(
                            isOpen = false
                        )
                    )
                },
                sheetState = bottomSheetState
            ) {
                SheetContent(
                    documentTypeUiName = state.document?.documentName.orEmpty(),
                    onEventSent = {
                        onEventSend(it)
                    }
                )
            }
        }
    }
}

private fun handleNavigationEffect(
    navigationEffect: Effect.Navigation,
    navController: NavController
) {
    when (navigationEffect) {
        is Effect.Navigation.SwitchScreen -> {
            navController.navigate(navigationEffect.screenRoute) {
                popUpTo(navigationEffect.popUpToScreenRoute) {
                    inclusive = navigationEffect.inclusive
                }
            }
        }

        is Effect.Navigation.Pop -> navController.popBackStack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: State,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit,
    onNavigationRequested: (Effect.Navigation) -> Unit,
    paddingValues: PaddingValues,
    topBarHeight: Dp,
    coroutineScope: CoroutineScope,
    modalBottomSheetState: SheetState,
    modifier: Modifier = Modifier
) {
    state.document?.let { documentUi ->

        val (lowPriority, normalPriority) = remember {
            documentUi.documentDetails.partition { it.priority == Priority.LOW }
        }


        Column(
            modifier
                .fillMaxSize()
                .padding(
                    top = topBarHeight,
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = SPACING_LARGE.dp),
                verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp),
            ) {
                items(items = normalPriority) { documentDetailsUi ->
                    DetailItem(documentDetailsUi)
                }

                if (lowPriority.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_MEDIUM.dp))
                    }
                }

                items(items = lowPriority) { documentDetailsUi ->
                    DetailItem(documentDetailsUi)
                }
            }

            Column(
                Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sticky Button
                WrapSecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onEventSend(Event.SharePressed)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.issuance_document_details_primary_button_text),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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
private fun SheetContent(
    documentTypeUiName: String,
    onEventSent: (event: Event) -> Unit
) {
    DialogBottomSheet(
        title = stringResource(
            id = R.string.document_details_bottom_sheet_delete_title,
            documentTypeUiName
        ),
        message = stringResource(
            id = R.string.document_details_bottom_sheet_delete_subtitle,
            documentTypeUiName
        ),
        positiveButtonText = stringResource(id = R.string.document_details_bottom_sheet_delete_primary_button_text),
        negativeButtonText = stringResource(id = R.string.document_details_bottom_sheet_delete_secondary_button_text),
        onPositiveClick = { onEventSent(Event.BottomSheet.Delete.PrimaryButtonPressed) },
        onNegativeClick = { onEventSent(Event.BottomSheet.Delete.SecondaryButtonPressed) }
    )
}


@PreviewLightDark
@Composable
private fun DashboardDocumentDetailsScreenPreview(
    @PreviewParameter(DocumentDetailsPreviewParameter::class) state: State
) {
    PreviewTheme {
        DocumentDetailsScreen(
            state = state,
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {}
        )
    }
}