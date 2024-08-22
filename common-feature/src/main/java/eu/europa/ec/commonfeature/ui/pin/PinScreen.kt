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

@file:OptIn(ExperimentalMaterial3Api::class)

package eu.europa.ec.commonfeature.ui.pin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import eu.europa.ec.commonfeature.model.PinFlow
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.ActionTopBar
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.content.ContentTitle
import eu.europa.ec.uilogic.component.content.ToolbarAction
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.DialogBottomSheet
import eu.europa.ec.uilogic.component.wrap.WrapModalBottomSheet
import eu.europa.ec.uilogic.component.wrap.WrapPinTextField
import eu.europa.ec.uilogic.component.wrap.WrapPrimaryButton
import eu.europa.ec.uilogic.extension.finish
import eu.europa.ec.uilogic.navigation.CommonScreens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    navController: NavController,
    viewModel: PinViewModel,
) {
    val state = viewModel.viewState.value
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    PinScreen(
        state = state,
        coroutineScope = rememberCoroutineScope(),
        bottomSheetState = bottomSheetState,
        navController = navController,
        effectFlow = viewModel.effect,
        onEventSend = {
            viewModel.setEvent(it)
        }
    )
}

@Composable
private fun PinScreen(
    state: State,
    coroutineScope: CoroutineScope,
    bottomSheetState: SheetState,
    navController: NavController,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit
) {
    val context = LocalContext.current

    val isBottomSheetOpen = state.isBottomSheetOpen

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = state.action,
        onBack = { onEventSend(state.onBackEvent) },
        topBar = {
            ActionTopBar(
                contentColor = MaterialTheme.colorScheme.background,
                iconColor = MaterialTheme.colorScheme.primary,
                iconData = AppIcons.Close,
                toolbarActions = listOf(
                    ToolbarAction(
                        icon = AppIcons.Help.copy(
                            tint = MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            onEventSend(Event.ShowHelp)
                        }
                    )
                )
            ) {
                onEventSend(state.onBackEvent)
            }
        },
        stickyBottom = {
            WrapPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isButtonEnabled,
                onClick = {
                    onEventSend(
                        Event.NextButtonPressed(
                            pin = state.pin
                        )
                    )
                }
            ) {
                Text(text = state.buttonText)
            }
        }
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = effectFlow,
            onEventSend = { event -> onEventSend(event) },
            onNavigationRequested = { navigationEffect ->
                handleNavigationEffect(
                    context,
                    navigationEffect,
                    navController
                )
            },
            paddingValues = paddingValues,
            coroutineScope = coroutineScope,
            modalBottomSheetState = bottomSheetState,
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
                    onEventSent = {
                        onEventSend(it)
                    }
                )
            }
        }
    }
}

private fun handleNavigationEffect(
    context: Context,
    navigationEffect: Effect.Navigation,
    navController: NavController
) {
    when (navigationEffect) {
        is Effect.Navigation.SwitchScreen -> {
            navController.navigate(navigationEffect.screen) {
                popUpTo(CommonScreens.QuickPin.screenRoute) {
                    inclusive = true
                }
            }
        }

        is Effect.Navigation.SwitchModule -> navController.navigate(navigationEffect.moduleRoute.route)

        is Effect.Navigation.Pop -> {}
        is Effect.Navigation.GoBack -> navController.popBackStack()
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
    coroutineScope: CoroutineScope,
    modalBottomSheetState: SheetState,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        ContentTitle(
            title = state.title,
            subtitle = state.subtitle
        )

        VSpacer.Medium()

        PinFieldLayout(
            state = state,
            onPinInput = { quickPin ->
                onEventSend(
                    Event.OnQuickPinEntered(
                        quickPin
                    )
                )
            },
            onTogglePasswordVisibility = {
                onEventSend(
                    Event.OnPasswordVisibilityChanged
                )
            }
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

                is Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.collect()
    }
}

@Composable
private fun SheetContent(
    onEventSent: (event: Event) -> Unit
) {
    DialogBottomSheet(
        title = stringResource(id = R.string.quick_pin_bottom_sheet_cancel_title),
        message = stringResource(id = R.string.quick_pin_bottom_sheet_cancel_subtitle),
        positiveButtonText = stringResource(id = R.string.quick_pin_bottom_sheet_cancel_primary_button_text),
        negativeButtonText = stringResource(id = R.string.quick_pin_bottom_sheet_cancel_secondary_button_text),
        onPositiveClick = { onEventSent(Event.BottomSheet.Cancel.PrimaryButtonPressed) },
        onNegativeClick = { onEventSent(Event.BottomSheet.Cancel.SecondaryButtonPressed) }
    )
}

@Composable
private fun PinFieldLayout(
    state: State,
    onPinInput: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    WrapPinTextField(
        onPinUpdate = {
            onPinInput(it)
        },
        length = state.quickPinSize,
        hasError = !state.quickPinError.isNullOrEmpty(),
        errorMessage = state.quickPinError,
        pinWidth = 46.dp,
        clearCode = state.resetPin,
        focusOnCreate = true,
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = onTogglePasswordVisibility
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun PinScreenEmptyPreview() {
    PreviewTheme {
        PinScreen(
            state = State(
                pinFlow = PinFlow.CREATE,
                pinState = PinValidationState.ENTER,
                buttonText = stringResource(id = R.string.quick_pin_create_success_btn),
                isPasswordVisible = true,
                pin = "123456"
            ),
            coroutineScope = rememberCoroutineScope(),
            bottomSheetState = rememberModalBottomSheetState(),
            navController = rememberNavController(),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {}
        )
    }
}

@ThemeModePreviews
@Composable
private fun SheetContentCancelPreview() {
    PreviewTheme {
        SheetContent(
            onEventSent = {}
        )
    }
}