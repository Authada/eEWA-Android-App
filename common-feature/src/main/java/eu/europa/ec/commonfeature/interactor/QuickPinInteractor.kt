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

package eu.europa.ec.commonfeature.interactor

import de.authada.eewa.wallet.PinCheckResponse
import eu.europa.ec.authenticationlogic.controller.storage.PinStorageController
import eu.europa.ec.businesslogic.extension.safeAsync
import eu.europa.ec.businesslogic.validator.FormValidator
import eu.europa.ec.eudi.wallet.EudiWallet
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface QuickPinInteractor : FormValidator {
    fun setPin(newPin: String, initialPin: String): Flow<QuickPinInteractorSetPinPartialState>
    fun changePin(
        newPin: String
    ): Flow<QuickPinInteractorSetPinPartialState>

    fun isCurrentPinValid(pin: String): Flow<QuickPinInteractorPinValidPartialState>
    fun isPinMatched(
        currentPin: String,
        newPin: String
    ): Flow<QuickPinInteractorPinValidPartialState>

    fun hasPin(): Boolean
}

class QuickPinInteractorImpl(
    private val formValidator: FormValidator,
    private val pinStorageController: PinStorageController,
    private val resourceProvider: ResourceProvider,
) : FormValidator by formValidator, QuickPinInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun hasPin(): Boolean = EudiWallet.secureElementPidLib?.isPinSet() ?:
    pinStorageController.retrievePin().isNotBlank()

    override fun setPin(
        newPin: String,
        initialPin: String
    ): Flow<QuickPinInteractorSetPinPartialState> =
        flow {
            isPinMatched(initialPin, newPin).collect {
                when (it) {
                    is QuickPinInteractorPinValidPartialState.Failed -> {
                        emit(
                            QuickPinInteractorSetPinPartialState.Failed(
                                resourceProvider.getString(R.string.quick_pin_non_match)
                            )
                        )
                    }

                    is QuickPinInteractorPinValidPartialState.Success -> {
                        if (EudiWallet.secureElementPidLib != null) {
                            val errorMessage = handleSetPinSecureElement(newPin)
                            errorMessage?.let { message ->
                                emit(QuickPinInteractorSetPinPartialState.Failed(message))
                            }
                        } else {
                            pinStorageController.setPin(newPin)
                        }
                        emit(QuickPinInteractorSetPinPartialState.Success)
                    }
                }
            }
        }.safeAsync {
            QuickPinInteractorSetPinPartialState.Failed(
                it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun changePin(
        newPin: String
    ): Flow<QuickPinInteractorSetPinPartialState> =
        flow {
            if (EudiWallet.secureElementPidLib != null) {
                val errorMessage = handleSetPinSecureElement(newPin)
                errorMessage?.let { message ->
                    emit(QuickPinInteractorSetPinPartialState.Failed(message))
                }
            } else {
                pinStorageController.setPin(newPin)
            }
            emit(QuickPinInteractorSetPinPartialState.Success)
        }.safeAsync {
            QuickPinInteractorSetPinPartialState.Failed(
                it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun isCurrentPinValid(pin: String): Flow<QuickPinInteractorPinValidPartialState> =
        flow {
            val isPinValid = EudiWallet.secureElementPidLib?.let { sePidLib ->
                    sePidLib.unlockViaPin(pin.pinStringToIntArray()) == PinCheckResponse.SUCCESSFUL
                } ?: pinStorageController.isPinValid(pin)
            if (isPinValid) {
                emit(QuickPinInteractorPinValidPartialState.Success)
            } else {
                emit(
                    QuickPinInteractorPinValidPartialState.Failed(
                        resourceProvider.getString(
                            R.string.quick_pin_invalid_error
                        )
                    )
                )
            }
        }.safeAsync {
            QuickPinInteractorPinValidPartialState.Failed(
                it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun isPinMatched(
        currentPin: String,
        newPin: String
    ): Flow<QuickPinInteractorPinValidPartialState> =
        flow {
            if (currentPin == newPin) {
                emit(QuickPinInteractorPinValidPartialState.Success)
            } else {
                emit(
                    QuickPinInteractorPinValidPartialState.Failed(
                        resourceProvider.getString(
                            R.string.quick_pin_invalid_error
                        )
                    )
                )
            }
        }.safeAsync {
            QuickPinInteractorPinValidPartialState.Failed(
                it.localizedMessage ?: genericErrorMsg
            )
        }

    private fun handleSetPinSecureElement(pin: String): String? {
        return EudiWallet.secureElementPidLib?.let { sePidLib ->
            if (sePidLib.setPin(pin.pinStringToIntArray())) {
                val pinCheckResponse = sePidLib.unlockViaPin(pin.pinStringToIntArray())
                if (pinCheckResponse != PinCheckResponse.SUCCESSFUL) {
                    resourceProvider.getString(R.string.quick_pin_se_unlock_error)
                }
                null
            }
            else {
                resourceProvider.getString(R.string.quick_pin_se_pin_set_error)
            }
        }
    }
}

sealed class QuickPinInteractorSetPinPartialState {
    data object Success : QuickPinInteractorSetPinPartialState()
    data class Failed(val errorMessage: String) : QuickPinInteractorSetPinPartialState()
}

sealed class QuickPinInteractorPinValidPartialState {
    data object Success : QuickPinInteractorPinValidPartialState()
    data class Failed(val errorMessage: String) : QuickPinInteractorPinValidPartialState()
}

fun String.pinStringToIntArray(): IntArray = this.take(6).map { it.digitToInt() }.toIntArray()