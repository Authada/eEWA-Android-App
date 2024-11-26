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

package eu.europa.ec.proximityfeature.ui.qr

import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import eu.europa.ec.commonfeature.config.PresentationMode
import eu.europa.ec.commonfeature.config.RequestUriConfig
import eu.europa.ec.corelogic.di.getOrCreatePresentationScope
import eu.europa.ec.proximityfeature.interactor.ProximityQRInteractor
import eu.europa.ec.proximityfeature.interactor.ProximityQRPartialState
import eu.europa.ec.uilogic.component.content.ContentErrorConfig
import eu.europa.ec.uilogic.mvi.MviViewModel
import eu.europa.ec.uilogic.mvi.ViewEvent
import eu.europa.ec.uilogic.mvi.ViewSideEffect
import eu.europa.ec.uilogic.mvi.ViewState
import eu.europa.ec.uilogic.navigation.ProximityScreens
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.uilogic.serializer.UiSerializer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

data class BleState(
    val bleEnabled: Boolean,
    val blePermissionsState: BlePermissionsState,
    val isBleCentralClientModeEnabled: Boolean = false
)

enum class BlePermissionsState {
    GRANTED, NEED_TO_CHECK, WAIT_FOR_EVENT
}

enum class BottomSheetType {
    BLUETOOTH_PERMISSIONS, BLUETOOTH_ENABLING
}

data class BottomSheetState(
    val isOpen: Boolean,
    val sheetType: BottomSheetType
)

data class State(
    val isLoading: Boolean = true,
    val qrGenerationHappened: Boolean = false,
    val error: ContentErrorConfig? = null,
    val qrCode: String = "",
    val bottomSheetState: BottomSheetState = BottomSheetState(
        isOpen = false,
        sheetType = BottomSheetType.BLUETOOTH_PERMISSIONS
    ),
    val bleState: BleState
) : ViewState

sealed class Event : ViewEvent {
    data object Init : Event()
    data object GoBack : Event()
    data class NfcEngagement(
        val componentActivity: ComponentActivity,
        val enable: Boolean
    ) : Event()

    data object OnShowPermissionsRational : Event()
    data class OnPermissionStateChanged(val blePermissionsState: BlePermissionsState) : Event()

    sealed class BottomSheet : Event() {
        data object OnEnableBluetoothClick : BottomSheet()
        data object OnGrantPermissionsFromRationale : BottomSheet()
        data object OnCancelled : BottomSheet()

        data class UpdateBottomSheetState(val bottomSheetState: BottomSheetState) : BottomSheet()
        data object Close : BottomSheet()
    }
}

sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(
            val screenRoute: String
        ) : Navigation()

        data object Pop : Navigation()

        data object OnAppSettings : Navigation()
        data object OnSystemSettings : Navigation()
    }

    data object CloseBottomSheet : Effect()
}

@KoinViewModel
class ProximityQRViewModel(
    private val interactor: ProximityQRInteractor,
    private val uiSerializer: UiSerializer,
    @InjectedParam private val requestUriConfigRaw: String,
) : MviViewModel<Event, State, Effect>() {

    private var interactorJob: Job? = null

    override fun setInitialState(): State = State(
        bleState = getInitialBleState(),
    )

    private fun getInitialBleState(): BleState {
        return BleState(
            bleEnabled = interactor.isBleAvailable(),
            blePermissionsState = BlePermissionsState.WAIT_FOR_EVENT
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                initializeConfig()
                setState {
                    copy(
                        bleState = viewState.value.bleState.copy(
                            bleEnabled = interactor.isBleAvailable(),
                            blePermissionsState = BlePermissionsState.NEED_TO_CHECK,
                            isBleCentralClientModeEnabled = interactor.isBleCentralClientModeEnabled()
                        )
                    )
                }
            }

            is Event.GoBack -> {
                cleanUp()
                setState { copy(error = null) }
                setEffect { Effect.Navigation.Pop }
            }

            is Event.NfcEngagement -> {
                //This happens based on the Lifecycle, therefore permissions are not taking into considerations
                if (event.enable) {
                    generateQrCodeIfBluetoothIsEnabledOrAskUserToEnable()
                }
                interactor.toggleNfcEngagement(
                    event.componentActivity,
                    event.enable
                )
            }

            is Event.OnShowPermissionsRational -> {
                val newBottomSheetState = viewState.value.bottomSheetState.copy(
                    isOpen = true,
                    sheetType = BottomSheetType.BLUETOOTH_PERMISSIONS
                )

                setEvent(Event.BottomSheet.UpdateBottomSheetState(newBottomSheetState))
            }

            is Event.OnPermissionStateChanged -> {
                val newBleState = viewState.value.bleState.copy(
                    blePermissionsState = event.blePermissionsState
                )
                setState { copy(bleState = newBleState) }
            }

            is Event.BottomSheet -> handleBottomSheetEvents(event)
        }
    }

    private fun handleBottomSheetEvents(bottomSheetEvent: Event.BottomSheet) {
        when (bottomSheetEvent) {
            is Event.BottomSheet.UpdateBottomSheetState -> {
                setState {
                    copy(bottomSheetState = bottomSheetEvent.bottomSheetState)
                }
            }

            Event.BottomSheet.OnEnableBluetoothClick -> {
                if (interactor.isBleAvailable()) {
                    hideBottomSheet()
                    generateQrCode()
                } else {
                    setEffect { Effect.Navigation.OnSystemSettings }
                }
            }

            Event.BottomSheet.OnGrantPermissionsFromRationale -> {
                setEffect { Effect.Navigation.OnAppSettings }
            }

            is Event.BottomSheet.OnCancelled -> {
                setEvent(Event.GoBack)
            }

            Event.BottomSheet.Close -> {
                hideBottomSheet()
            }
        }
    }

    private fun generateQrCodeIfBluetoothIsEnabledOrAskUserToEnable() {
        if (interactor.isBleAvailable()) {
            generateQrCode()
            hideBottomSheet()
        } else {
            askToEnableBluetooth()
        }
    }

    private fun askToEnableBluetooth() {
        val newBottomSheetState = viewState.value.bottomSheetState.copy(
            isOpen = true,
            sheetType = BottomSheetType.BLUETOOTH_ENABLING
        )

        setState {
            copy(bottomSheetState = newBottomSheetState)
        }
    }


    private fun hideBottomSheet() {
        setEffect {
            Effect.CloseBottomSheet
        }
    }

    private fun initializeConfig() {
        val requestUriConfig = uiSerializer.fromBase64(
            requestUriConfigRaw,
            RequestUriConfig::class.java,
            RequestUriConfig.Parser
        ) ?: throw RuntimeException("RequestUriConfig:: is Missing or invalid")

        interactor.setConfig(requestUriConfig)
    }

    private fun generateQrCode() {
        if (viewState.value.qrGenerationHappened) {
            return
        }
        setState {
            copy(
                isLoading = true,
                qrGenerationHappened = true,
                error = null,
                bleState = viewState.value.bleState.copy(
                    bleEnabled = true,
                    blePermissionsState = BlePermissionsState.GRANTED
                ),
                bottomSheetState = viewState.value.bottomSheetState.copy(isOpen = false)
            )
        }

        interactorJob = viewModelScope.launch {
            interactor.startQrEngagement().collect { response ->
                when (response) {
                    is ProximityQRPartialState.Error -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = ContentErrorConfig(
                                    onRetry = { setEvent(Event.Init) },
                                    errorSubTitle = response.error,
                                    onCancel = { setEvent(Event.GoBack) }
                                )
                            )
                        }
                    }

                    is ProximityQRPartialState.QrReady -> {
                        setState {
                            copy(
                                isLoading = false,
                                error = null,
                                qrCode = response.qrCode
                            )
                        }
                    }

                    is ProximityQRPartialState.Connected -> {
                        unsubscribe()
                        setEffect {
                            Effect.Navigation.SwitchScreen(
                                screenRoute = generateComposableNavigationLink(
                                    screen = ProximityScreens.Request,
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

                    is ProximityQRPartialState.Disconnected -> {
                        unsubscribe()
                        setEvent(Event.GoBack)
                    }
                }
            }
        }
    }

    /**
     * Required in order to stop receiving emissions from interactor Flow
     * */
    private fun unsubscribe() {
        interactorJob?.cancel()
    }

    /**
     * Stop presentation and remove scope/listeners
     * */
    private fun cleanUp() {
        unsubscribe()
        getOrCreatePresentationScope().close()
        interactor.cancelTransfer()
    }
}