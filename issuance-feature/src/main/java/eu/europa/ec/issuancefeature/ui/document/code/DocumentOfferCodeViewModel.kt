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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import eu.europa.ec.commonfeature.config.BiometricUiConfig
import eu.europa.ec.commonfeature.config.OfferCodeUiConfig
import eu.europa.ec.commonfeature.config.OnBackNavigationConfig
import eu.europa.ec.issuancefeature.interactor.document.DocumentOfferInteractor
import eu.europa.ec.issuancefeature.interactor.document.IssueDocumentsInteractorPartialState
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.content.ContentErrorConfig
import eu.europa.ec.uilogic.component.content.LoadingType
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

private typealias PinCode = String

data class State(
    val offerCodeUiConfig: OfferCodeUiConfig,

    val isLoading: LoadingType = LoadingType.NONE,
    val error: ContentErrorConfig? = null,

    val screenTitle: String,
    val screenSubtitle: String
) : ViewState

sealed class Event : ViewEvent {
    data object Pop : Event()
    data class OnResume(val savedStateHandle: SavedStateHandle?, val context: Context) : Event()
    data object DismissError : Event()
    data class OnPinChange(val code: PinCode, val context: Context) : Event()
}

sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(
            val screenRoute: String,
            val inclusive: Boolean
        ) : Navigation()

        data object Pop : Navigation()
    }
}

@KoinViewModel
class DocumentOfferCodeViewModel(
    private val documentOfferInteractor: DocumentOfferInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
    @InjectedParam private val offerCodeSerializedConfig: String
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        val deserializedOfferCodeUiConfig = uiSerializer.fromBase64(
            offerCodeSerializedConfig,
            OfferCodeUiConfig::class.java,
            OfferCodeUiConfig.Parser
        ) ?: throw RuntimeException("OfferCodeUiConfig:: is Missing or invalid")
        return State(
            offerCodeUiConfig = deserializedOfferCodeUiConfig,
            screenTitle = calculateScreenTitle(),
            screenSubtitle = calculateScreenCaption(txCodeLength = deserializedOfferCodeUiConfig.txCodeLength)
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Pop -> {
                setState { copy(error = null) }
                setEffect { Effect.Navigation.Pop }
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.OnResume -> {
                val txCode = event.savedStateHandle?.get<String>(
                    AUTHORIZATION_KEY
                )
                if (txCode != null) {
                    issueDocuments(
                        event.context,
                        txCode
                    )
                }
            }

            is Event.OnPinChange -> {
                if (event.code.isPinValid()) {
                    setEffect {
                        Effect.Navigation.SwitchScreen(
                            screenRoute = askForPinAndConfirmIssuance(event.code),
                            inclusive = false
                        )
                    }
                }
            }
        }
    }

    private fun askForPinAndConfirmIssuance(txCode: String): String {
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
                                    value = txCode,
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


    private fun issueDocuments(context: Context, pinCode: PinCode) {
        viewModelScope.launch {

            setState {
                copy(
                    isLoading = LoadingType.NORMAL,
                    error = null
                )
            }

            documentOfferInteractor.issueDocuments(
                offerUri = viewState.value.offerCodeUiConfig.offerURI,
                issuerName = viewState.value.offerCodeUiConfig.issuerName,
                navigation = viewState.value.offerCodeUiConfig.onSuccessNavigation,
                txCode = pinCode
            ).collect { response ->
                when (response) {
                    is IssueDocumentsInteractorPartialState.Failure -> setState {
                        copy(
                            isLoading = LoadingType.NONE,
                            error = ContentErrorConfig(
                                errorSubTitle = response.errorMessage,
                                onCancel = { setEvent(Event.DismissError) }
                            )
                        )
                    }

                    is IssueDocumentsInteractorPartialState.Success -> {
                        setState {
                            copy(
                                isLoading = LoadingType.NONE,
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
                screenRoute = route,
                inclusive = true
            )
        }
    }

    private fun calculateScreenTitle(): String = resourceProvider.getString(
        R.string.issuance_code_title
    )

    private fun calculateScreenCaption(txCodeLength: Int): String =
        resourceProvider.getString(R.string.issuance_code_caption, txCodeLength)

    private fun PinCode.isPinValid(): Boolean =
        this.length == viewState.value.offerCodeUiConfig.txCodeLength

    private companion object {
        const val AUTHORIZATION_KEY = "AUTHORIZATION_KEY"
    }
}