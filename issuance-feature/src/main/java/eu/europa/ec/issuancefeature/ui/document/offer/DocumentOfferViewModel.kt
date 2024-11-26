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

package eu.europa.ec.issuancefeature.ui.document.offer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import eu.europa.ec.businesslogic.extension.toUri
import eu.europa.ec.commonfeature.config.BiometricUiConfig
import eu.europa.ec.commonfeature.config.OfferCodeUiConfig
import eu.europa.ec.commonfeature.config.OfferUiConfig
import eu.europa.ec.commonfeature.config.OnBackNavigationConfig
import eu.europa.ec.commonfeature.ui.request.model.DocumentItemUi
import eu.europa.ec.eudi.wallet.EudiWallet
import eu.europa.ec.issuancefeature.interactor.document.DocumentOfferInteractor
import eu.europa.ec.issuancefeature.interactor.document.IssueDocumentsInteractorPartialState
import eu.europa.ec.issuancefeature.interactor.document.ResolveDocumentOfferInteractorPartialState
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.content.ContentErrorConfig
import eu.europa.ec.uilogic.config.ConfigNavigation
import eu.europa.ec.uilogic.config.NavigationType
import eu.europa.ec.uilogic.mvi.MviViewModel
import eu.europa.ec.uilogic.mvi.ViewEvent
import eu.europa.ec.uilogic.mvi.ViewSideEffect
import eu.europa.ec.uilogic.mvi.ViewState
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.uilogic.serializer.UiSerializer
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

data class State(
    val offerUiConfig: OfferUiConfig,

    val isLoading: Boolean = true,
    val error: ContentErrorConfig? = null,
    val isBottomSheetOpen: Boolean = false,
    val isInitialised: Boolean = false,

    val issuerName: String,
    val screenTitle: String,
    val screenSubtitle: String,
    val documents: List<DocumentItemUi> = emptyList(),
    val noDocument: Boolean = false,
    val txCodeLength: Int? = null
) : ViewState

sealed class Event : ViewEvent {
    data object Init : Event()
    data object Pop : Event()
    data object OnPause : Event()
    data class OnAuthorizationUriReceived(val uri: String) : Event()
    data class OnResume(val savedStateHandle: SavedStateHandle?) : Event()
    data object DismissError : Event()

    data class PrimaryButtonPressed(val context: Context) : Event()
    data object SecondaryButtonPressed : Event()

    sealed class BottomSheet : Event() {
        data class UpdateBottomSheetState(val isOpen: Boolean) : BottomSheet()

        sealed class Cancel : BottomSheet() {
            data object PrimaryButtonPressed : Cancel()
            data object SecondaryButtonPressed : Cancel()
        }
    }
}

sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(
            val screenRoute: String,
            val shouldPopToSelf: Boolean = true
        ) : Navigation()

        data class PopBackStackUpTo(
            val screenRoute: String,
            val inclusive: Boolean
        ) : Navigation()

        data object Pop : Navigation()

        data class DeepLink(
            val link: Uri
        ) : Navigation()
    }

    data object ShowBottomSheet : Effect()
    data object CloseBottomSheet : Effect()
}

@KoinViewModel
class DocumentOfferViewModel(
    private val documentOfferInteractor: DocumentOfferInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
    @InjectedParam private val offerSerializedConfig: String,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        val deserializedOfferUiConfig = uiSerializer.fromBase64(
            offerSerializedConfig,
            OfferUiConfig::class.java,
            OfferUiConfig.Parser
        ) ?: throw RuntimeException("OfferUiConfig:: is Missing or invalid")

        val issuerName = resourceProvider.getString(
            R.string.issuance_document_offer_default_issuer_name
        )

        return State(
            offerUiConfig = deserializedOfferUiConfig,
            issuerName = issuerName,
            screenTitle = calculateScreenTitle(),
            screenSubtitle = resourceProvider.getString(R.string.issuance_document_offer_subtitle),
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                resolveDocumentOffer(offerUri = viewState.value.offerUiConfig.offerURI)
            }

            is Event.Pop -> {
                setState { copy(error = null) }
                setEffect { Effect.Navigation.Pop }
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.PrimaryButtonPressed -> {
                issueDocuments(
                    context = event.context,
                    offerUri = viewState.value.offerUiConfig.offerURI,
                    issuerName = viewState.value.issuerName,
                    onSuccessNavigation = viewState.value.offerUiConfig.onSuccessNavigation,
                    txCodeLength = viewState.value.txCodeLength
                )
            }

            is Event.SecondaryButtonPressed -> {
                showBottomSheet()
            }

            is Event.BottomSheet.UpdateBottomSheetState -> {
                setState {
                    copy(isBottomSheetOpen = event.isOpen)
                }
            }

            is Event.BottomSheet.Cancel.PrimaryButtonPressed -> {
                hideBottomSheet()
            }

            is Event.BottomSheet.Cancel.SecondaryButtonPressed -> {
                hideBottomSheet()
                doNavigation(viewState.value.offerUiConfig.onCancelNavigation)
            }

            is Event.OnPause -> {
                if (viewState.value.isInitialised) {
                    setState { copy(isLoading = false) }
                }
            }

            is Event.OnResume -> {
                val authorizationKeyIfComingFromPinEntry = event.savedStateHandle?.get<String>(AUTHORIZATION_KEY)
                if(authorizationKeyIfComingFromPinEntry != null) {
                    EudiWallet.resumeOpenId4VciWithAuthorization(authorizationKeyIfComingFromPinEntry)
                    setState {
                        copy(isLoading = true)
                    }
                }
            }

            is Event.OnAuthorizationUriReceived -> {
                setEffect {
                    Effect.Navigation.SwitchScreen(
                        askForPinAndAuthorizeTheIssuance(event.uri),
                        shouldPopToSelf = false
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
                                    navigationType = NavigationType.PopTo(IssuanceScreens.DocumentOffer),
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

    private fun resolveDocumentOffer(offerUri: String) {
        setState {
            copy(
                isLoading = documents.isEmpty(),
                error = null
            )
        }
        viewModelScope.launch {
            documentOfferInteractor.resolveDocumentOffer(
                offerUri = offerUri
            ).collect { response ->
                when (response) {
                    is ResolveDocumentOfferInteractorPartialState.Failure -> {
                        setState {
                            copy(
                                isLoading = false,
                                isInitialised = false,
                                error = ContentErrorConfig(
                                    errorSubTitle = response.errorMessage,
                                    onCancel = {
                                        setEvent(Event.DismissError)
                                        doNavigation(viewState.value.offerUiConfig.onCancelNavigation)
                                    }
                                )
                            )
                        }
                    }

                    is ResolveDocumentOfferInteractorPartialState.Success -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = null,
                                documents = response.documents,
                                isInitialised = true,
                                noDocument = false,
                                issuerName = response.issuerName,
                                screenTitle = calculateScreenTitle(),
                                txCodeLength = response.txCodeLength
                            )
                        }
                    }

                    is ResolveDocumentOfferInteractorPartialState.NoDocument -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = null,
                                documents = emptyList(),
                                isInitialised = true,
                                noDocument = true,
                                issuerName = response.issuerName,
                                screenTitle = calculateScreenTitle()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun issueDocuments(
        context: Context,
        offerUri: String,
        issuerName: String,
        onSuccessNavigation: ConfigNavigation,
        txCodeLength: Int?
    ) {
        viewModelScope.launch {

            txCodeLength?.let {
                navigateToOfferCodeScreen(
                    offerUri,
                    issuerName,
                    txCodeLength,
                    onSuccessNavigation
                )
                return@launch
            }

            setState {
                copy(
                    isLoading = true,
                    error = null
                )
            }

            documentOfferInteractor.issueDocuments(
                offerUri = offerUri,
                issuerName = issuerName,
                navigation = onSuccessNavigation
            ).collect { response ->
                when (response) {
                    is IssueDocumentsInteractorPartialState.Failure -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = ContentErrorConfig(
                                    errorSubTitle = response.errorMessage,
                                    onCancel = { setEvent(Event.DismissError) }
                                )
                            )
                        }
                    }

                    is IssueDocumentsInteractorPartialState.Success -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = null,
                            )
                        }

                        goToSuccessScreen(route = response.successRoute)
                    }

                    is IssueDocumentsInteractorPartialState.UserAuthRequired -> {
                        documentOfferInteractor.handleUserAuthentication(
                            context = context,
                            crypto = response.crypto,
                            resultHandler = response.resultHandler
                        )
                    }
                }
            }
        }
    }

    private fun goToSuccessScreen(route: String) {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = route
            )
        }
    }

    private fun doNavigation(navigation: ConfigNavigation) {
        val navigationEffect: Effect.Navigation = when (val nav = navigation.navigationType) {
            is NavigationType.PopTo -> {
                Effect.Navigation.PopBackStackUpTo(
                    screenRoute = nav.screen.screenRoute,
                    inclusive = false
                )
            }

            is NavigationType.PushScreen -> {
                Effect.Navigation.SwitchScreen(
                    generateComposableNavigationLink(
                        screen = nav.screen,
                        arguments = generateComposableArguments(nav.arguments),
                    )
                )
            }

            is NavigationType.Deeplink -> Effect.Navigation.DeepLink(
                nav.link.toUri()
            )

            is NavigationType.Pop, NavigationType.Finish, is NavigationType.PopAndSetResult<*> -> {
                Effect.Navigation.Pop
            }

            is NavigationType.PushRoute -> Effect.Navigation.SwitchScreen(nav.route)
        }

        setEffect {
            navigationEffect
        }
    }

    private fun showBottomSheet() {
        setEffect {
            Effect.ShowBottomSheet
        }
    }

    private fun hideBottomSheet() {
        setEffect {
            Effect.CloseBottomSheet
        }
    }

    private fun calculateScreenTitle(): String {
        return resourceProvider.getString(
            R.string.issuance_document_offer_title)
    }

    private fun navigateToOfferCodeScreen(
        offerUri: String,
        issuerName: String,
        txCodeLength: Int,
        onSuccessNavigation: ConfigNavigation
    ) {
        setEffect {
            Effect.Navigation.SwitchScreen(
                screenRoute = generateComposableNavigationLink(
                    IssuanceScreens.DocumentOfferCode,
                    getNavigateOfferCodeScreenArguments(
                        offerUri,
                        issuerName,
                        txCodeLength,
                        onSuccessNavigation
                    )
                ),
                shouldPopToSelf = false
            )
        }
    }

    private fun getNavigateOfferCodeScreenArguments(
        offerUri: String,
        issuerName: String,
        txCodeLength: Int,
        onSuccessNavigation: ConfigNavigation
    ): String {
        return generateComposableArguments(
            mapOf(
                OfferCodeUiConfig.serializedKeyName to uiSerializer.toBase64(
                    OfferCodeUiConfig(
                        offerURI = offerUri,
                        txCodeLength = txCodeLength,
                        issuerName = issuerName,
                        onSuccessNavigation = onSuccessNavigation
                    ),
                    OfferCodeUiConfig.Parser
                ).orEmpty()
            )
        )
    }

    private companion object {
        const val AUTHORIZATION_KEY = "AUTHORIZATION_KEY"
    }
}