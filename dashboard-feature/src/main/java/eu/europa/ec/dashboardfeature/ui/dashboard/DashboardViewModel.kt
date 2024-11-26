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

package eu.europa.ec.dashboardfeature.ui.dashboard

import android.net.Uri
import androidx.lifecycle.viewModelScope
import eu.europa.ec.commonfeature.config.IssuanceFlowUiConfig
import eu.europa.ec.commonfeature.config.OfferUiConfig
import eu.europa.ec.commonfeature.config.PresentationMode
import eu.europa.ec.commonfeature.config.RequestUriConfig
import eu.europa.ec.commonfeature.model.PinFlow
import eu.europa.ec.corelogic.config.WalletCoreConfig
import eu.europa.ec.corelogic.di.getOrCreatePresentationScope
import eu.europa.ec.corelogic.model.DocType
import eu.europa.ec.dashboardfeature.DashboardDocumentModel
import eu.europa.ec.dashboardfeature.interactor.DashboardInteractor
import eu.europa.ec.dashboardfeature.interactor.DashboardInteractorPartialState
import eu.europa.ec.eudi.wallet.transfer.openid4vp.ClientIdScheme
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.content.ContentErrorConfig
import eu.europa.ec.uilogic.config.ConfigNavigation
import eu.europa.ec.uilogic.config.NavigationType
import eu.europa.ec.uilogic.mvi.MviViewModel
import eu.europa.ec.uilogic.mvi.ViewEvent
import eu.europa.ec.uilogic.mvi.ViewSideEffect
import eu.europa.ec.uilogic.mvi.ViewState
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.DashboardScreens
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.ProximityScreens
import eu.europa.ec.uilogic.navigation.helper.DeepLinkType
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.uilogic.navigation.helper.hasDeepLink
import eu.europa.ec.uilogic.serializer.UiSerializer
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class State(
    val isLoading: Boolean = true,
    val error: ContentErrorConfig? = null,
    val isBottomSheetOpen: Boolean = false,
    val userBase64Image: String = "",
    val documents: List<DashboardDocumentModel> = emptyList(),

    val appVersion: String = ""
) : ViewState

sealed class Event : ViewEvent {
    data class Init(val deepLinkUri: Uri?) : Event()
    data object Pop : Event()
    data class NavigateToDocument(
        val documentId: String,
        val documentType: DocType,
    ) : Event()

    data object OptionsPressed : Event()
    sealed class Button : Event() {
        data object AddDocumentPressed : Button()
        data object QrPressed : Button()
    }

    data class OnProxyPressed(
        val documentId: String,
        val documentType: DocType,
    ) : Event()

    sealed class BottomSheet : Event() {
        data class UpdateBottomSheetState(val isOpen: Boolean) : BottomSheet()
        data object Close : BottomSheet()

        sealed class Options : BottomSheet() {
            data object OpenChangeQuickPin : Options()
            data object OpenScanQr : Options()
            data object OpenScanQrPid : Options()
            data object OpenVerifierWebsite : Options()
            data object OpenIssuerWebsite : Options()
            data object StartProximityFlowPressed : Options()
        }
    }
}

sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data object Pop : Navigation()
        data class SwitchScreen(val screenRoute: String) : Navigation()
        data class OpenDeepLinkAction(val deepLinkUri: Uri, val arguments: String?) :
            Navigation()
    }

    data object ShowBottomSheet : Effect()
    data object CloseBottomSheet : Effect()
}

@KoinViewModel
class DashboardViewModel(
    private val dashboardInteractor: DashboardInteractor,
    private val uiSerializer: UiSerializer,
    private val resourceProvider: ResourceProvider,
    private val walletConfig: WalletCoreConfig
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State = State(
        appVersion = dashboardInteractor.getAppVersion()
    )

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                getDocuments(event, event.deepLinkUri)
            }

            is Event.Pop -> setEffect { Effect.Navigation.Pop }

            is Event.NavigateToDocument -> {
                navigateToDetailsScreen(event.documentId, event.documentType)
            }

            is Event.OptionsPressed -> {
                setEffect {
                    Effect.ShowBottomSheet
                }
            }

            is Event.Button.AddDocumentPressed -> {
                setEffect {
                    Effect.Navigation.SwitchScreen(
                        IssuanceScreens.AddDocumentTypeSelection.screenRoute
                    )
                }
            }

            is Event.Button.QrPressed -> {
                navigateToQrScan()
            }

            is Event.BottomSheet.UpdateBottomSheetState -> {
                setState {
                    copy(isBottomSheetOpen = event.isOpen)
                }
            }

            is Event.BottomSheet.Close -> {
                hideBottomSheet()
            }

            is Event.BottomSheet.Options.OpenChangeQuickPin -> {
                hideBottomSheet()
                navigateToChangeQuickPin()
            }

            is Event.BottomSheet.Options.OpenScanQr -> {
                hideBottomSheet()
                navigateToQrScan()
            }

            is Event.BottomSheet.Options.StartProximityFlowPressed -> {
                hideBottomSheet()
                startProximityFlow()
            }

            Event.BottomSheet.Options.OpenScanQrPid -> {
                hideBottomSheet()
                navigateToQrScanPid()
            }

            Event.BottomSheet.Options.OpenVerifierWebsite -> {
                hideBottomSheet()
                navigateToVerifierWebsite()
            }

            Event.BottomSheet.Options.OpenIssuerWebsite -> {
                hideBottomSheet()
                navigateToIssuerWebsite()
            }

            is Event.OnProxyPressed -> {
                navigateToProxyPidExplanation()
            }
        }
    }

    private fun navigateToDetailsScreen(documentId: String, documentType: DocType) {
        setEffect {
            Effect.Navigation.SwitchScreen(
                generateComposableNavigationLink(
                    screen = IssuanceScreens.DocumentDetails,
                    arguments = generateComposableArguments(
                        mapOf(
                            "detailsType" to IssuanceFlowUiConfig.EXTRA_DOCUMENT,
                            "documentId" to documentId,
                            "documentType" to documentType,
                        )
                    )
                )
            )
        }
    }

    private fun getDocuments(event: Event, deepLinkUri: Uri?) {
        setState {
            copy(
                isLoading = documents.isEmpty(),
                error = null
            )
        }
        viewModelScope.launch {
            when (val response = dashboardInteractor.getStoredDocumentsWithMetadata()) {
                is DashboardInteractorPartialState.Failure -> {
                    setState {
                        copy(
                            isLoading = false,
                            error = ContentErrorConfig(
                                onRetry = { setEvent(event) },
                                errorSubTitle = response.error,
                                onCancel = {
                                    setState { copy(error = null) }
                                    setEvent(Event.Pop)
                                }
                            )
                        )
                    }
                }

                is DashboardInteractorPartialState.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            error = null,
                            documents = response.documents,
                            userBase64Image = response.userBase64Portrait
                        )
                    }
                    handleDeepLink(deepLinkUri)
                }
            }
        }
    }

    private fun handleDeepLink(deepLinkUri: Uri?) {
        deepLinkUri?.let { uri ->
            hasDeepLink(uri)?.let {
                val arguments: String? = when (it.type) {
                    DeepLinkType.OPENID4VP -> {
                        getOrCreatePresentationScope()
                        generateComposableArguments(
                            mapOf(
                                RequestUriConfig.serializedKeyName to uiSerializer.toBase64(
                                    RequestUriConfig(PresentationMode.OpenId4Vp(uri.toString())),
                                    RequestUriConfig.Parser
                                )
                            )
                        )
                    }

                    DeepLinkType.OPENID4VCI -> generateComposableArguments(
                        mapOf(
                            OfferUiConfig.serializedKeyName to uiSerializer.toBase64(
                                OfferUiConfig(
                                    offerURI = it.link.toString(),
                                    onSuccessNavigation = ConfigNavigation(
                                        navigationType = NavigationType.PopTo(
                                            screen = DashboardScreens.Dashboard
                                        )
                                    ),
                                    onCancelNavigation = ConfigNavigation(
                                        navigationType = NavigationType.Pop
                                    )
                                ),
                                OfferUiConfig.Parser
                            )
                        )
                    )

                    else -> null
                }
                setEffect {
                    Effect.Navigation.OpenDeepLinkAction(
                        deepLinkUri = uri,
                        arguments = arguments
                    )
                }
            }
        }
    }

    private fun navigateToChangeQuickPin() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = generateComposableNavigationLink(
                    screen = CommonScreens.QuickPin,
                    arguments = generateComposableArguments(
                        mapOf("pinFlow" to PinFlow.UPDATE)
                    )
                )
            )
        }
    }

    private fun startProximityFlow() {
        // Create Koin scope for presentation
        getOrCreatePresentationScope()
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = generateComposableNavigationLink(
                    screen = ProximityScreens.Qr,
                    arguments = generateComposableArguments(
                        mapOf(
                            RequestUriConfig.serializedKeyName to uiSerializer.toBase64(
                                RequestUriConfig(PresentationMode.Ble),
                                RequestUriConfig.Parser
                            )
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

    private fun navigateToProxyPidExplanation() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = DashboardScreens.ProxyExplanation.screenRoute
            )
        }
    }

    private fun navigateToVerifierWebsite() {
        val preregisteredClientSchemes =
            walletConfig.config.openId4VPConfig?.clientIdSchemes?.filterIsInstance<ClientIdScheme.Preregistered>()
        val preregisteredVerifier = preregisteredClientSchemes
            ?.firstOrNull()?.preregisteredVerifiers?.firstOrNull()

        if (preregisteredVerifier != null) {
            handleDeepLink(deepLinkUri = Uri.parse(preregisteredVerifier.verifierApi))
        }
    }

    private fun navigateToIssuerWebsite() {
        val issuerWebsiteForBrowser = walletConfig.config.issuerWebsiteForBrowser

        if (issuerWebsiteForBrowser != null) {
            handleDeepLink(deepLinkUri = Uri.parse(issuerWebsiteForBrowser))
        }

    }

    private fun navigateToQrScanPid() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = CommonScreens.QrScan.screenRoute
            )
        }
    }

    private fun hideBottomSheet() {
        setEffect {
            Effect.CloseBottomSheet
        }
    }
}