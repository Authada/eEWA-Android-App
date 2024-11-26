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
 * Modified by AUTHADA GmbH November 2024
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

package eu.europa.ec.issuancefeature.ui.document.code

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import eu.europa.ec.commonfeature.config.OfferCodeUiConfig
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.content.ContentTitle
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.utils.LifecycleEffect
import eu.europa.ec.uilogic.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.uilogic.component.wrap.WrapIcon
import eu.europa.ec.uilogic.component.wrap.WrapPinTextField
import eu.europa.ec.uilogic.config.ConfigNavigation
import eu.europa.ec.uilogic.config.NavigationType
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

@Composable
fun DocumentOfferCodeScreen(
    navController: NavController,
    viewModel: DocumentOfferCodeViewModel
) {
    val state = viewModel.viewState.value
    val context = LocalContext.current

    DocumentOfferCodeScreen(
        state = state,
        onNavigationRequested = { navigationEffect ->
            handleNavigationEffect(navigationEffect, navController)
        },
        onEventSend = { viewModel.setEvent(it) },
        effectFlow = viewModel.effect
    )

    LifecycleEffect(
        lifecycleOwner = LocalLifecycleOwner.current,
        lifecycleEvent = Lifecycle.Event.ON_RESUME
    ) {
        viewModel.setEvent(
            Event.OnResume(
                savedStateHandle = navController.currentBackStackEntry?.savedStateHandle,
                context = context
            )
        )

    }
}

@Composable
private fun DocumentOfferCodeScreen(
    state: State,
    onNavigationRequested: (Effect.Navigation) -> Unit,
    onEventSend: (Event) -> Unit,
    effectFlow: Flow<Effect>
) {
    val context = LocalContext.current

    ContentScreen(
        loadingType = state.isLoading,
        contentErrorConfig = state.error,
        navigatableAction = ScreenNavigateAction.BACKABLE,
        onBack = { onEventSend(Event.Pop) },
    ) { paddingValues ->
        Content(
            context = context,
            state = state,
            effectFlow = effectFlow,
            onEventSend = { onEventSend(it) },
            onNavigationRequested = onNavigationRequested,
            paddingValues = paddingValues
        )
    }
}


private fun handleNavigationEffect(
    navigationEffect: Effect.Navigation,
    navController: NavController
) {
    when (navigationEffect) {
        is Effect.Navigation.SwitchScreen -> {
            navController.navigate(navigationEffect.screenRoute) {
                popUpTo(IssuanceScreens.DocumentOfferCode.screenRoute) {
                    inclusive = navigationEffect.inclusive
                }
            }
        }

        is Effect.Navigation.Pop -> {
            navController.popBackStack()
        }
    }
}

@Composable
private fun Content(
    context: Context,
    state: State,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit,
    onNavigationRequested: (Effect.Navigation) -> Unit,
    paddingValues: PaddingValues
) {

    val configuration = LocalConfiguration.current
    val messageIconSize = (configuration.screenWidthDp / 4).dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        ContentTitle(
            title = state.screenTitle,
            subtitle = state.screenSubtitle,
        )

        WrapIcon(
            modifier = Modifier.size(messageIconSize),
            iconData = AppIcons.Message,
            customTint = MaterialTheme.colorScheme.primary
        )

        CodeFieldLayout(state = state) {
            onEventSend(Event.OnPinChange(code = it, context = context))
        }
    }

    LaunchedEffect(Unit) {
        effectFlow.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> onNavigationRequested(effect)
            }
        }.collect()
    }
}

@Composable
fun CodeFieldLayout(
    state: State,
    onPinInput: (String) -> Unit,
) {
    WrapPinTextField(
        onPinUpdate = {
            onPinInput(it)
        },
        length = state.offerCodeUiConfig.txCodeLength,
        pinWidth = 46.dp,
        focusOnCreate = true,
        isPasswordVisible = true,
        onTogglePasswordVisibility = {}
    )
}

@PreviewLightDark
@Composable
private fun DocumentOfferCodeScreenPreview() {
    val state = State(
        offerCodeUiConfig = OfferCodeUiConfig(
            offerURI = "http://uri.com",
            txCodeLength = 5,
            issuerName = "Issuer Name",
            onSuccessNavigation = ConfigNavigation(navigationType = NavigationType.Pop)
        ),
        screenTitle = "Title",
        screenSubtitle = "Subtitle"
    )
    PreviewTheme {
        DocumentOfferCodeScreen(
            state = state,
            onNavigationRequested = {},
            onEventSend = {},
            effectFlow = flowOf()
        )
    }
}