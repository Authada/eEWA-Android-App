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

package eu.europa.ec.issuancefeature.ui.document.preparation

import eu.europa.ec.commonfeature.config.IssuanceFlowUiConfig
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.uilogic.component.utils.NOT_IMPLEMENTED_MESSAGE
import eu.europa.ec.uilogic.mvi.MviViewModel
import eu.europa.ec.uilogic.mvi.ViewEvent
import eu.europa.ec.uilogic.mvi.ViewSideEffect
import eu.europa.ec.uilogic.mvi.ViewState
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink


class AddDocumentInfoViewModel :
    MviViewModel<AddDocumentInfoViewModel.Event, AddDocumentInfoViewModel.State, AddDocumentInfoViewModel.Effect>() {

    data class State(
        val onBackAction: (() -> Unit)? = null,

        val isLoading: Boolean = false,
        val isHelpRequested: Boolean = false
    ) : ViewState

    sealed class Event : ViewEvent {
        data object OnNextPressed : Event()
        data object ShowHelp : Event()
        data object DismissHelp : Event()
        data object Pop : Event()
    }

    sealed class Effect : ViewSideEffect {
        data class ShowToast(val message: String) : Effect()
        sealed class Navigation : Effect() {
            data object Pop : Navigation()
            data class SwitchScreen(val screenRoute: String) : Navigation()
        }
    }

    override fun setInitialState(): State {
        return State(
            onBackAction = {
            },
            isLoading = false,
            isHelpRequested = false

        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            Event.DismissHelp -> TODO()
            Event.ShowHelp -> {
                setEffect { Effect.ShowToast(NOT_IMPLEMENTED_MESSAGE) }
            }

            Event.OnNextPressed -> {
                navigateToAddDocument()
            }

            Event.Pop -> {
                setEffect { Effect.Navigation.Pop }
            }
        }
    }

    private fun navigateToAddDocument() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = generateComposableNavigationLink(
                    screen = IssuanceScreens.AddDocument,
                    arguments = generateComposableArguments(
                        mapOf(
                            "flowType" to IssuanceFlowUiConfig.EXTRA_DOCUMENT,
                            "documentType" to DocumentIdentifier.PID_ISSUING.docType
                        )
                    )
                )
            )
        }
    }


}