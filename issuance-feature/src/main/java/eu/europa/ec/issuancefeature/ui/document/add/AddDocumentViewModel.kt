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

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import eu.europa.ec.authenticationlogic.controller.authentication.DeviceAuthenticationResult
import eu.europa.ec.commonfeature.config.BiometricUiConfig
import eu.europa.ec.commonfeature.config.IssuanceFlowUiConfig
import eu.europa.ec.commonfeature.config.OfferUiConfig
import eu.europa.ec.commonfeature.config.OnBackNavigationConfig
import eu.europa.ec.corelogic.controller.IssuanceMethod
import eu.europa.ec.corelogic.controller.IssueDocumentPartialState
import eu.europa.ec.corelogic.model.DocType
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.eudi.wallet.EudiWallet
import eu.europa.ec.issuancefeature.interactor.document.AddDocumentInteractor
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.content.ContentErrorConfig
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.config.ConfigNavigation
import eu.europa.ec.uilogic.config.NavigationType
import eu.europa.ec.uilogic.mvi.MviViewModel
import eu.europa.ec.uilogic.mvi.ViewEvent
import eu.europa.ec.uilogic.mvi.ViewSideEffect
import eu.europa.ec.uilogic.mvi.ViewState
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.DashboardScreens
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.helper.DeepLinkType
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.uilogic.navigation.helper.hasDeepLink
import eu.europa.ec.uilogic.serializer.UiSerializer
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

data class State(
    val navigatableAction: ScreenNavigateAction,
    val onBackAction: (() -> Unit)? = null,

    val isLoading: Boolean = false,
    val error: ContentErrorConfig? = null,
    val isInitialised: Boolean = false,
    val subtitle: String = ""
) : ViewState

sealed class Event : ViewEvent {
    data class OnResume(val deepLink: Uri?, val savedStateHandle: SavedStateHandle?) :
        Event()

    data object Pop : Event()
    data object OnPause : Event()
    data class OnAuthorizationUriReceived(val uri: String) : Event()
    data object Finish : Event()
    data object DismissError : Event()
    data class StartIssuance(
        val issuanceMethod: IssuanceMethod,
        val context: Context
    ) : Event()
}

sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data object Pop : Navigation()
        data object Finish : Navigation()
        data class SwitchScreen(val screenRoute: String, val inclusive: Boolean) : Navigation()
        data class OpenDeepLinkAction(val deepLinkUri: Uri, val arguments: String?) : Navigation()
    }
}

@KoinViewModel
class AddDocumentViewModel(
    private val addDocumentInteractor: AddDocumentInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
    @InjectedParam private val flowType: IssuanceFlowUiConfig,
    @InjectedParam private val docIdentifier: DocumentIdentifier
) : MviViewModel<Event, State, Effect>() {
    override fun setInitialState(): State = State(
        navigatableAction = getNavigatableAction(flowType),
        onBackAction = getOnBackAction(flowType),
        subtitle = resourceProvider.getString(R.string.issuance_add_document_subtitle)
    )

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.OnResume -> {
                val authorizationKeyIfComingFromPinEntry = event.savedStateHandle?.get<String>(
                    AUTHORIZATION_KEY
                )
                authorizationKeyIfComingFromPinEntry?.let {
                    EudiWallet.resumeOpenId4VciWithAuthorization(it)
                } ?: run {
                    handleDeepLink(event.deepLink)
                }
            }

            is Event.Pop -> setEffect { Effect.Navigation.Pop }

            is Event.DismissError -> {
                setEffect { Effect.Navigation.Pop }
            }

            is Event.StartIssuance -> {
                issueDocument(
                    issuanceMethod = event.issuanceMethod,
                    docType = docIdentifier.docType,
                    context = event.context
                )
            }

            is Event.Finish -> setEffect { Effect.Navigation.Finish }

            is Event.OnPause -> {
                if (viewState.value.isInitialised) {
                    setState { copy(isLoading = false) }
                }
            }

            is Event.OnAuthorizationUriReceived -> {
                setEffect {
                    Effect.Navigation.SwitchScreen(
                        screenRoute = askForPinAndAuthorizeTheIssuance(
                            event.uri
                        ),
                        inclusive = false
                    )
                }
            }
        }
    }

    private fun askForPinAndAuthorizeTheIssuance(uri: String): String {
        return generateComposableNavigationLink(
            screen = CommonScreens.Biometric,
            arguments = generateComposableArguments(
                mapOf(
                    BiometricUiConfig.serializedKeyName to uiSerializer.toBase64(
                        BiometricUiConfig(
                            title = resourceProvider.getString(R.string.issuance_confirm_with_pin_title),
                            subTitle = resourceProvider.getString(R.string.issuance_confirm_with_biometry_subtitle),
                            quickPinOnlySubTitle = resourceProvider.getString(R.string.issuance_confirm_with_pin_subtitle),
                            onSuccessNavigation = ConfigNavigation(
                                navigationType = NavigationType.PopAndSetResult(
                                    key = AUTHORIZATION_KEY,
                                    value = uri,
                                )
                            ),
                            onBackNavigationConfig = OnBackNavigationConfig(
                                onBackNavigation = ConfigNavigation(
                                    navigationType = NavigationType.PopTo(IssuanceScreens.AddDocumentTypeSelection),
                                ),
                                hasToolbarCancelIcon = true
                            )
                        ),
                        BiometricUiConfig.Parser
                    ).orEmpty()
                )
            )
        )
    }

    private fun issueDocument(
        issuanceMethod: IssuanceMethod,
        docType: DocType,
        context: Context
    ) {
        viewModelScope.launch {

            setState {
                copy(
                    isLoading = true,
                    error = null
                )
            }

            addDocumentInteractor.issueDocument(
                issuanceMethod = issuanceMethod,
                documentType = docType
            ).collect { response ->
                when (response) {
                    is IssueDocumentPartialState.Failure -> {
                        setState {
                            copy(
                                error = ContentErrorConfig(
                                    onRetry = null,
                                    errorSubTitle = response.errorMessage,
                                    onCancel = { setEvent(Event.DismissError) }
                                ),
                                isLoading = false
                            )
                        }
                    }

                    is IssueDocumentPartialState.Success -> {
                        setState {
                            copy(
                                error = null,
                                isLoading = false
                            )
                        }
                        navigateToSuccessScreen(
                            documentId = response.documentId
                        )
                    }

                    is IssueDocumentPartialState.UserAuthRequired -> {
                        addDocumentInteractor.handleUserAuth(
                            context = context,
                            crypto = response.crypto,
                            resultHandler = DeviceAuthenticationResult(
                                onAuthenticationSuccess = {
                                    response.resultHandler.onAuthenticationSuccess()
                                },
                                onAuthenticationFailure = {
                                    response.resultHandler.onAuthenticationFailure()
                                },
                                onAuthenticationError = {
                                    response.resultHandler.onAuthenticationError()
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    private fun navigateToSuccessScreen(documentId: String) {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = generateComposableNavigationLink(
                    screen = IssuanceScreens.Success,
                    arguments = generateComposableArguments(
                        mapOf(
                            "flowType" to IssuanceFlowUiConfig.fromIssuanceFlowUiConfig(flowType),
                            "documentId" to documentId,
                        )
                    )
                ),
                inclusive = false
            )
        }
    }

    private fun getNavigatableAction(flowType: IssuanceFlowUiConfig): ScreenNavigateAction {
        return when (flowType) {
            IssuanceFlowUiConfig.NO_DOCUMENT -> ScreenNavigateAction.NONE
            IssuanceFlowUiConfig.EXTRA_DOCUMENT -> ScreenNavigateAction.CANCELABLE
        }
    }

    private fun getOnBackAction(flowType: IssuanceFlowUiConfig): (() -> Unit) {
        return when (flowType) {
            IssuanceFlowUiConfig.NO_DOCUMENT -> {
                { setEvent(Event.Finish) }
            }

            IssuanceFlowUiConfig.EXTRA_DOCUMENT -> {
                { setEvent(Event.Pop) }
            }
        }
    }

    private fun handleDeepLink(deepLinkUri: Uri?) {
        deepLinkUri?.let { uri ->
            hasDeepLink(uri)?.let {
                when (it.type) {
                    DeepLinkType.OPENID4VCI -> {
                        setEffect {
                            Effect.Navigation.OpenDeepLinkAction(
                                deepLinkUri = uri,
                                arguments = generateComposableArguments(
                                    mapOf(
                                        OfferUiConfig.serializedKeyName to uiSerializer.toBase64(
                                            OfferUiConfig(
                                                offerURI = it.link.toString(),
                                                onSuccessNavigation = ConfigNavigation(
                                                    navigationType = NavigationType.PushScreen(
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
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private companion object {
        const val AUTHORIZATION_KEY = "AUTHORIZATION_KEY"
    }
}