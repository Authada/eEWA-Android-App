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

package eu.europa.ec.issuancefeature.ui.document.preparation.typeselection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.uilogic.component.ActionTopBar
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.issuancefeature.ui.document.preparation.typeselection.AddDocumentTypeSelectionViewModel.*
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.uilogic.component.ScalableText
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.utils.HSpacer
import eu.europa.ec.uilogic.component.utils.SIZE_MEDIUM
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.BUTTON_HEIGHT_FAT
import eu.europa.ec.uilogic.component.wrap.WrapIcon
import eu.europa.ec.uilogic.component.wrap.WrapPrimaryButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
internal fun AddDocumentTypeSelectionScreen(
    navController: NavController,
    viewModel: AddDocumentTypeSelectionViewModel
) {
    val state = viewModel.viewState.value
    AddDocumentTypeSelectionScreen(state, onEventSend = {
        viewModel.setEvent(it)
    })

    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> {
                    when (effect) {
                        is Effect.Navigation.Pop -> navController.popBackStack()
                        is Effect.Navigation.SwitchScreen -> {
                            navController.navigate(effect.screenRoute)
                        }
                    }
                }
            }
        }.collect()
    }
}

@Composable
private fun AddDocumentTypeSelectionScreen(state: State, onEventSend: (Event) -> Unit) {
    ContentScreen(
        navigatableAction = ScreenNavigateAction.BACKABLE,
        isLoading = false,
        topBar = {
            ActionTopBar(
                contentColor = MaterialTheme.colorScheme.background,
                iconColor = MaterialTheme.colorScheme.primary,
                iconData = AppIcons.ArrowBack,
                onClick = {
                    onEventSend(Event.Pop)
                })
        },
        onBack = {
            onEventSend(Event.Pop)
        }
    ) { paddingValues ->
        Content(
            onEventSend = { onEventSend(it) },
            paddingValues = paddingValues,
            state = state
        )
    }
}

@Composable
private fun Content(
    onEventSend: (Event) -> Unit,
    paddingValues: PaddingValues,
    state: State,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.issuance_add_document_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.textPrimary
            )
        )

        VSpacer.Small()

        Text(
            text = stringResource(id = R.string.issuance_add_document_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.textPrimary
            )
        )

        VSpacer.Medium()

        Body(state, onEventSend, paddingValues)
        StickyButton(onClick = {
            onEventSend(Event.OnQrPressed)
        })
    }
}

@Composable
private fun ColumnScope.Body(
    state: State,
    onEventSend: (Event) -> Unit,
    paddingValues: PaddingValues
) {
    val nrOfColumns = 2

    val paddingOfParent = paddingValues.calculateStartPadding(
        LayoutDirection.Ltr
    ) + paddingValues.calculateEndPadding(LayoutDirection.Ltr)
    val itemSize = (LocalConfiguration.current.screenWidthDp.dp / nrOfColumns) - paddingOfParent

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(MaterialTheme.colorScheme.background),
        columns = GridCells.Fixed(nrOfColumns),
        verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp),
        horizontalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp)
    ) {

        state.options.forEach { option ->
            item {
                TypeItem(option,
                    modifier = Modifier.size(itemSize),
                    onClick = {
                        onEventSend(
                            Event.OnDocumentSelected(
                                docIdentifier = option.type
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun StickyButton(onClick: () -> Unit) {
    WrapPrimaryButton(
        modifier = Modifier.fillMaxWidth(),
        height = BUTTON_HEIGHT_FAT,
        onClick = onClick
    ) {
        WrapIcon(
            modifier = Modifier.size(SIZE_MEDIUM.dp),
            iconData = AppIcons.QR,
            customTint = MaterialTheme.colorScheme.onPrimary
        )
        HSpacer.ExtraSmall()
        ScalableText(
            text = stringResource(id = R.string.issuance_add_document_scan_qr),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}


@PreviewLightDark
@Composable
private fun AddDocumentTypeSelectionScreenPreview() {
    PreviewTheme {
        AddDocumentTypeSelectionScreen(
            onEventSend = {},
            state = State(
                onBackAction = {},
                options = listOf(
                    DocumentOptionItemUi(
                        text = "ID Document",
                        icon = AppIcons.Id,
                        type = DocumentIdentifier.PID_ISSUING,
                        available = true
                    ),
                    DocumentOptionItemUi(
                        text = "Drivers License",
                        icon = AppIcons.DriversLicense,
                        type = DocumentIdentifier.MDL,
                        available = true
                    )
                )
            )
        )
    }
}

