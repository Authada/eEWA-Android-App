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

package eu.europa.ec.commonfeature.ui.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.commonfeature.ui.InformationCard
import eu.europa.ec.resourceslogic.theme.WalletTheme
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.component.utils.OneTimeLaunchedEffect
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun LoadingScreen(
    navController: NavController,
    viewModel: LoadingViewModel
) {
    val state = viewModel.viewState.value
    val context = LocalContext.current

    LoadingScreen(
        state = state
    )

    OneTimeLaunchedEffect {
        viewModel.setEvent(Event.DoWork(context))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> {
                    when (effect) {
                        is Effect.Navigation.SwitchScreen -> {
                            navController.navigate(effect.screenRoute) {
                                popUpTo(viewModel.getCallerScreen().screenRoute) {
                                    inclusive = true
                                }
                            }
                        }

                        is Effect.Navigation.PopBackStackUpTo -> {
                            navController.popBackStack(
                                route = effect.screenRoute,
                                inclusive = effect.inclusive
                            )
                        }
                    }
                }
            }
        }.collect()
    }
}

@Composable
private fun LoadingScreen(
    state: State
) {

    ContentScreen(
        isLoading = state.error != null,
        navigatableAction = ScreenNavigateAction.NONE,
        contentErrorConfig = state.error
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top
        ) {

            InformationCard(
                modifier = Modifier.fillMaxHeight(0.7f),
                title = state.screenTitle,
                subtitle = state.screenSubtitle,
                infoRoundIcon = {
                    PieChart(modifier = Modifier.size(200.dp))
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoadingContent(modifier: Modifier = Modifier) {
    WalletTheme {
        LoadingScreen(
            state = State(
                screenTitle = "Loading",
                screenSubtitle = stringResource(id = eu.europa.ec.resourceslogic.R.string.loading_subtitle)
            )
        )
    }
}