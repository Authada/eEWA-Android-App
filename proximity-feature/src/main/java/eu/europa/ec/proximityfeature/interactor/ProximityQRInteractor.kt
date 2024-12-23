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

package eu.europa.ec.proximityfeature.interactor

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.activity.ComponentActivity
import eu.europa.ec.businesslogic.extension.safeAsync
import eu.europa.ec.commonfeature.config.RequestUriConfig
import eu.europa.ec.commonfeature.config.toDomainConfig
import eu.europa.ec.corelogic.config.WalletCoreConfig
import eu.europa.ec.corelogic.controller.TransferEventPartialState
import eu.europa.ec.corelogic.controller.WalletCorePresentationController
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

sealed class ProximityQRPartialState {
    data class QrReady(val qrCode: String) : ProximityQRPartialState()
    data class Error(val error: String) : ProximityQRPartialState()
    data object Connected : ProximityQRPartialState()
    data object Disconnected : ProximityQRPartialState()
}

interface ProximityQRInteractor {
    fun startQrEngagement(): Flow<ProximityQRPartialState>
    fun toggleNfcEngagement(
        componentActivity: ComponentActivity,
        toggle: Boolean
    )

    fun cancelTransfer()
    fun setConfig(config: RequestUriConfig)

    fun isBleAvailable(): Boolean
    fun isBleCentralClientModeEnabled(): Boolean
}

class ProximityQRInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val walletCorePresentationController: WalletCorePresentationController,
    private val walletCoreConfig: WalletCoreConfig
) : ProximityQRInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun setConfig(config: RequestUriConfig) {
        walletCorePresentationController.setConfig(config.toDomainConfig())
    }

    override fun startQrEngagement(): Flow<ProximityQRPartialState> = flow {
        walletCorePresentationController.startQrEngagement()
        walletCorePresentationController.events.mapNotNull {
            when (it) {
                is TransferEventPartialState.Connected -> {
                    ProximityQRPartialState.Connected
                }

                is TransferEventPartialState.Error -> {
                    ProximityQRPartialState.Error(error = it.error)
                }

                is TransferEventPartialState.QrEngagementReady -> {
                    ProximityQRPartialState.QrReady(qrCode = it.qrCode)
                }

                is TransferEventPartialState.Disconnected -> {
                    ProximityQRPartialState.Disconnected
                }

                else -> null
            }
        }.collect {
            emit(it)
        }
    }.safeAsync {
        ProximityQRPartialState.Error(error = it.localizedMessage ?: genericErrorMsg)
    }

    override fun toggleNfcEngagement(
        componentActivity: ComponentActivity,
        toggle: Boolean
    ) {
        walletCorePresentationController.toggleNfcEngagement(componentActivity, toggle)
    }

    override fun cancelTransfer() {
        walletCorePresentationController.stopPresentation()
    }

    override fun isBleAvailable(): Boolean {
        val bluetoothManager: BluetoothManager? = resourceProvider.provideContext()
            .getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return bluetoothManager?.adapter?.isEnabled == true
    }

    override fun isBleCentralClientModeEnabled(): Boolean =
        walletCoreConfig.config.bleCentralClientModeEnabled
}