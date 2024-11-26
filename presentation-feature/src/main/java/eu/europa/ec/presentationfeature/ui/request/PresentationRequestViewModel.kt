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

package eu.europa.ec.presentationfeature.ui.request

import androidx.lifecycle.viewModelScope
import eu.europa.ec.commonfeature.config.BiometricUiConfig
import eu.europa.ec.commonfeature.config.OnBackNavigationConfig
import eu.europa.ec.commonfeature.config.RequestUriConfig
import eu.europa.ec.commonfeature.ui.request.Event
import eu.europa.ec.commonfeature.ui.request.RequestViewModel
import eu.europa.ec.commonfeature.ui.request.model.RequestDataUi
import eu.europa.ec.presentationfeature.interactor.PresentationRequestInteractor
import eu.europa.ec.presentationfeature.interactor.PresentationRequestInteractorPartialState
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.content.ContentErrorConfig
import eu.europa.ec.uilogic.config.ConfigNavigation
import eu.europa.ec.uilogic.config.NavigationType
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.PresentationScreens
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.uilogic.serializer.UiSerializer
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class PresentationRequestViewModel(
    private val interactor: PresentationRequestInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
    @InjectedParam private val requestUriConfigRaw: String
) : RequestViewModel() {

    private var isProxyDocumentAboutToBeShared = false
    override fun getScreenSubtitle(): String {
        return resourceProvider.getString(R.string.request_subtitle_one)
    }

    override fun getScreenClickableSubtitle(): String {
        return resourceProvider.getString(R.string.request_subtitle_two)
    }

    override fun getWarningTextFieldsDeselected(): String {
        return resourceProvider.getString(R.string.request_warning_text_fields_deselected)
    }

    override fun getWarningTextMultiDocumentSending(): String {
        return resourceProvider.getString(R.string.request_warning_text_multi_document_sending)
    }

    override fun getScreenTitle(): String {
        return resourceProvider.getString(R.string.request_title)
    }

    override fun getNextScreen(): String {
        return if(isProxyDocumentAboutToBeShared) {
            // proxy PID required eID PIN anyway in the process, no need for additional wallet PIN
            PresentationScreens.PresentationLoading.screenRoute
        } else {
            generateComposableNavigationLink(
                screen = CommonScreens.Biometric,
                arguments = generateComposableArguments(
                    mapOf(
                        BiometricUiConfig.serializedKeyName to uiSerializer.toBase64(
                            BiometricUiConfig(
                                title = resourceProvider.getString(R.string.loading_quick_pin_share_title),
                                subTitle = resourceProvider.getString(R.string.loading_biometry_share_subtitle),
                                quickPinOnlySubTitle = resourceProvider.getString(R.string.loading_quick_pin_share_subtitle),
                                isPreAuthorization = false,
                                shouldInitializeBiometricAuthOnCreate = true,
                                onSuccessNavigation = ConfigNavigation(
                                    navigationType = NavigationType.PushRoute(
                                        route = PresentationScreens.PresentationLoading.screenRoute
                                    )
                                ),
                                onBackNavigationConfig = OnBackNavigationConfig(
                                    onBackNavigation = ConfigNavigation(
                                        navigationType = NavigationType.PopTo(PresentationScreens.PresentationRequest),
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
    }

    override fun doWork() {
        setState {
            copy(
                isLoading = true,
                error = null
            )
        }

        val requestUriConfig = uiSerializer.fromBase64(
            requestUriConfigRaw,
            RequestUriConfig::class.java,
            RequestUriConfig.Parser
        ) ?: throw RuntimeException("RequestUriConfig:: is Missing or invalid")

        interactor.setConfig(requestUriConfig)

        viewModelJob = viewModelScope.launch {
            interactor.getRequestDocuments().collect { response ->
                when (response) {
                    is PresentationRequestInteractorPartialState.Failure -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = ContentErrorConfig(
                                    onRetry = { setEvent(Event.DoWork) },
                                    errorSubTitle = response.error,
                                    onCancel = { setEvent(Event.GoBack) }
                                )
                            )
                        }
                    }

                    is PresentationRequestInteractorPartialState.Success -> {
                        updateData(response.requestDocuments)
                        isProxyDocumentAboutToBeShared =
                            (response.requestDocuments.find { it is RequestDataUi.Document } as? RequestDataUi.Document)?.isProxy ?: true
                        setState {
                            copy(
                                isLoading = false,
                                error = null,
                                verifierName = response.verifierName,
                                items = response.requestDocuments,
                                showVisibilitySwitchIcon = !isProxyDocumentAboutToBeShared,
                                isShowingFullUserInfo = !isProxyDocumentAboutToBeShared
                            )
                        }
                    }

                    is PresentationRequestInteractorPartialState.Disconnect -> {
                        setEvent(Event.GoBack)
                    }

                    is PresentationRequestInteractorPartialState.NoData -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = null,
                                verifierName = response.verifierName,
                                noItems = true,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun updateData(updatedItems: List<RequestDataUi<Event>>, allowShare: Boolean?) {
        super.updateData(updatedItems, allowShare)
        interactor.updateRequestedDocuments(updatedItems)
    }

    override fun cleanUp() {
        super.cleanUp()
        interactor.stopPresentation()
    }
}