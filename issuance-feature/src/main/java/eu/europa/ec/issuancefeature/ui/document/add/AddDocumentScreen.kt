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

package eu.europa.ec.issuancefeature.ui.document.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import eu.europa.ec.corelogic.controller.IssuanceMethod
import eu.europa.ec.corelogic.model.DocumentType
import eu.europa.ec.corelogic.util.CoreActions
import eu.europa.ec.uilogic.component.SystemBroadcastReceiver
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.utils.LifecycleEffect
import eu.europa.ec.uilogic.extension.finish
import eu.europa.ec.uilogic.extension.getPendingDeepLink
import eu.europa.ec.uilogic.navigation.DashboardScreens
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.helper.handleDeepLinkAction
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun AddDocumentScreen(
    navController: NavController,
    viewModel: AddDocumentViewModel
) {
    val state = viewModel.viewState.value
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.setEvent(
            Event.IssueDocument(
                issuanceMethod = IssuanceMethod.OPENID4VCI,
                documentType = DocumentType.PID.docType,
                context = context
            )
        )
    }

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = state.navigatableAction,
        onBack = state.onBackAction,
        contentErrorConfig = state.error
    ) { }

    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> {
                    when (effect) {
                        is Effect.Navigation.Pop -> navController.popBackStack(DashboardScreens.Dashboard.screenRoute, inclusive = false)
                        is Effect.Navigation.SwitchScreen -> {
                            navController.navigate(effect.screenRoute) {
                                popUpTo(IssuanceScreens.AddDocument.screenRoute) {
                                    inclusive = effect.inclusive
                                }
                            }
                        }

                        is Effect.Navigation.Finish -> context.finish()
                        is Effect.Navigation.OpenDeepLinkAction -> handleDeepLinkAction(
                            navController,
                            effect.deepLinkUri,
                            effect.arguments
                        )
                    }
                }
            }
        }.collect()
    }

    LifecycleEffect(
        lifecycleOwner = LocalLifecycleOwner.current,
        lifecycleEvent = Lifecycle.Event.ON_PAUSE
    ) {
        viewModel.setEvent(Event.OnPause)
    }

    LifecycleEffect(
        lifecycleOwner = LocalLifecycleOwner.current,
        lifecycleEvent = Lifecycle.Event.ON_RESUME
    ) {
        viewModel.setEvent(Event.Init(context.getPendingDeepLink()))
    }

    SystemBroadcastReceiver(action = CoreActions.VCI_RESUME_ACTION) {
        viewModel.setEvent(Event.OnResumeIssuance)
    }
}