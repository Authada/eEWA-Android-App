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

import androidx.lifecycle.viewModelScope
import eu.europa.ec.commonfeature.config.IssuanceFlowUiConfig
import eu.europa.ec.commonfeature.model.toUiName
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.eudi.wallet.EudiWallet
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.mvi.MviViewModel
import eu.europa.ec.uilogic.mvi.ViewEvent
import eu.europa.ec.uilogic.mvi.ViewSideEffect
import eu.europa.ec.uilogic.mvi.ViewState
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.DashboardScreens
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
internal class AddDocumentTypeSelectionViewModel(
    private val resourceProvider: ResourceProvider,
    private val walletCoreDocumentsController: WalletCoreDocumentsController
) :
    MviViewModel<AddDocumentTypeSelectionViewModel.Event, AddDocumentTypeSelectionViewModel.State, AddDocumentTypeSelectionViewModel.Effect>() {

    data class State(
        val onBackAction: (() -> Unit)? = null,
        val options: List<DocumentOptionItemUi> = emptyList(),
        val isLoading: Boolean = false,
    ) : ViewState

    sealed class Event : ViewEvent {
        data class OnDocumentSelected(val docIdentifier: DocumentIdentifier) : Event()
        data object Pop : Event()
        data object OnQrPressed : Event()
    }

    sealed class Effect : ViewSideEffect {
        sealed class Navigation : Effect() {
            data object Pop : Navigation()
            data class SwitchScreen(val screenRoute: String) : Navigation()
        }
    }

    private val hasSecureElement get() = EudiWallet.secureElementPidLib != null

    override fun setInitialState(): State {
        return State(
            onBackAction = {
                setEffect {
                    Effect.Navigation.Pop
                }
            },
            isLoading = true,
            options = emptyList()
        )
    }

    init {
        viewModelScope.launch {
            val hasStoredARealPid =
                walletCoreDocumentsController.getMainPidDocument()?.isProxy == false

            val options = mutableListOf(
                DocumentOptionItemUi(
                    text = DocumentIdentifier.MDL.toUiName(resourceProvider),
                    icon = AppIcons.DriversLicense,
                    type = DocumentIdentifier.MDL,
                    available = true
                ),
                DocumentOptionItemUi(
                    text = DocumentIdentifier.EMAIL_URN.toUiName(resourceProvider),
                    icon = AppIcons.OtherId,
                    type = DocumentIdentifier.EMAIL_URN,
                    available = true
                )
            )
            if (!hasStoredARealPid) {
                when (hasSecureElement) {
                    true -> {
                        options.add(
                            index = 0,
                            DocumentOptionItemUi(
                                text = resourceProvider.getString(R.string.dashboard_document_pid_title),
                                icon = AppIcons.Id,
                                type = DocumentIdentifier.PID_ISSUING,
                                available = true
                            )
                        )
                    }

                    false -> {
                        options.add(
                            DocumentOptionItemUi(
                                text = resourceProvider.getString(R.string.dashboard_document_pid_title),
                                icon = AppIcons.ProxyId,
                                type = DocumentIdentifier.PID_SDJWT,
                                available = false
                            )
                        )
                    }
                }
            }


            setState {
                copy(
                    isLoading = false,
                    options = options
                )
            }
        }
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.OnDocumentSelected -> {
                when (val identifier = event.docIdentifier) {
                    DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> {
                        navigateToProxyPidExplanation()
                    }

                    DocumentIdentifier.PID_ISSUING -> {
                        if (hasSecureElement) {
                            navigateToAddPid()
                        }
                    }

                    DocumentIdentifier.MDL, DocumentIdentifier.EMAIL_URN -> navigateToEaaIssuer(
                        identifier
                    )

                    else -> {
                        throw Exception("There is no other type issuing implemented")
                    }
                }
            }

            Event.Pop -> {
                setEffect { Effect.Navigation.Pop }
            }

            Event.OnQrPressed -> {
                navigateToQrScan()
            }
        }
    }

    private fun navigateToAddPid() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                IssuanceScreens.AddDocumentInfo.screenRoute
            )
        }
    }

    private fun navigateToProxyPidExplanation() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = DashboardScreens.ProxyExplanation.screenRoute
            )
        }
    }

    private fun navigateToEaaIssuer(documentIdentifier: DocumentIdentifier) {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = generateComposableNavigationLink(
                    screen = IssuanceScreens.AddDocument,
                    arguments = generateComposableArguments(
                        mapOf(
                            "flowType" to IssuanceFlowUiConfig.EXTRA_DOCUMENT,
                            "documentType" to documentIdentifier.docType
                        )
                    )
                )
            )
        }
    }

    private fun navigateToQrScan() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = CommonScreens.QrScan.screenRoute
            )
        }
    }
}