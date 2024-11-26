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

package eu.europa.ec.issuancefeature.interactor.document

import android.content.Context
import eu.europa.ec.authenticationlogic.controller.authentication.BiometricsAvailability
import eu.europa.ec.authenticationlogic.controller.authentication.DeviceAuthenticationResult
import eu.europa.ec.authenticationlogic.model.BiometricCrypto
import eu.europa.ec.commonfeature.config.IssuanceFlowUiConfig
import eu.europa.ec.commonfeature.interactor.DeviceAuthenticationInteractor
import eu.europa.ec.corelogic.controller.AddSampleDataPartialState
import eu.europa.ec.corelogic.controller.IssuanceMethod
import eu.europa.ec.corelogic.controller.IssueDocumentPartialState
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DocType
import kotlinx.coroutines.flow.Flow


interface AddDocumentInteractor {
    fun issueDocument(
        issuanceMethod: IssuanceMethod,
        documentType: DocType
    ): Flow<IssueDocumentPartialState>

    fun addSampleData(): Flow<AddSampleDataPartialState>

    fun handleUserAuth(
        context: Context,
        crypto: BiometricCrypto,
        resultHandler: DeviceAuthenticationResult
    )
}

class AddDocumentInteractorImpl(
    private val walletCoreDocumentsController: WalletCoreDocumentsController,
    private val deviceAuthenticationInteractor: DeviceAuthenticationInteractor,
) : AddDocumentInteractor {

    override fun issueDocument(
        issuanceMethod: IssuanceMethod,
        documentType: DocType
    ): Flow<IssueDocumentPartialState> =
        walletCoreDocumentsController.issueDocument(
            issuanceMethod = issuanceMethod,
            documentType = documentType
        )

    override fun addSampleData(): Flow<AddSampleDataPartialState> =
        walletCoreDocumentsController.addSampleData()

    override fun handleUserAuth(
        context: Context,
        crypto: BiometricCrypto,
        resultHandler: DeviceAuthenticationResult
    ) {
        deviceAuthenticationInteractor.getBiometricsAvailability {
            when (it) {
                is BiometricsAvailability.CanAuthenticate -> {
                    deviceAuthenticationInteractor.authenticateWithBiometrics(
                        context,
                        crypto,
                        resultHandler
                    )
                }

                is BiometricsAvailability.NonEnrolled -> {
                    deviceAuthenticationInteractor.authenticateWithBiometrics(
                        context,
                        crypto,
                        resultHandler
                    )
                }

                is BiometricsAvailability.Failure -> {
                    resultHandler.onAuthenticationFailure()
                }
            }
        }
    }

    private fun canCreateMdl(flowType: IssuanceFlowUiConfig): Boolean =
        flowType != IssuanceFlowUiConfig.NO_DOCUMENT
}