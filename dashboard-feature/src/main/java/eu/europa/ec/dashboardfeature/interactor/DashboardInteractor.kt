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

package eu.europa.ec.dashboardfeature.interactor

import android.bluetooth.BluetoothManager
import android.content.Context
import eu.europa.ec.businesslogic.config.ConfigLogic
import eu.europa.ec.businesslogic.extension.safeAsync
import eu.europa.ec.businesslogic.util.toDateFormatted
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.model.isProxy
import eu.europa.ec.commonfeature.model.toUiName
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentJsonKeys
import eu.europa.ec.commonfeature.util.documentHasExpired
import eu.europa.ec.commonfeature.util.extractValueFromDocumentOrEmpty
import eu.europa.ec.corelogic.config.WalletCoreConfig
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DocumentType
import eu.europa.ec.corelogic.model.toDocumentType
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class DashboardInteractorPartialState {
    data class Success(
        val documents: List<DocumentUi>,
        val lastAction: String,
        val userBase64Portrait: String,
    ) : DashboardInteractorPartialState()

    data class Failure(val error: String) : DashboardInteractorPartialState()
}

interface DashboardInteractor {
    fun getDocuments(): Flow<DashboardInteractorPartialState>
    fun isBleAvailable(): Boolean
    fun isBleCentralClientModeEnabled(): Boolean
    fun getAppVersion(): String
}

class DashboardInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val walletCoreDocumentsController: WalletCoreDocumentsController,
    private val walletCoreConfig: WalletCoreConfig,
    private val configLogic: ConfigLogic
) : DashboardInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun isBleAvailable(): Boolean {
        val bluetoothManager: BluetoothManager? = resourceProvider.provideContext()
            .getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return bluetoothManager?.adapter?.isEnabled == true
    }

    override fun isBleCentralClientModeEnabled(): Boolean =
        walletCoreConfig.config.bleCentralClientModeEnabled

    override fun getDocuments(): Flow<DashboardInteractorPartialState> = flow {
        var lastAction = ""
        var userImage = ""
        val documents = walletCoreDocumentsController.getAllDocuments()

        if (lastAction.isBlank()) {
            documents.minByOrNull { it.createdAt.epochSecond }?.let { newestDocument ->
                val issueDate = extractValueFromDocumentOrEmpty(
                    document = newestDocument,
                    key = DocumentJsonKeys.EXPIRY_DATE
                )
                lastAction = issueDate.toDateFormatted() ?: ""
            }
        }

        val documentsUi = documents.map { document ->

            var documentExpirationDate = extractValueFromDocumentOrEmpty(
                document = document,
                key = DocumentJsonKeys.EXPIRY_DATE
            )

            val docHasExpired = documentHasExpired(documentExpirationDate)

            documentExpirationDate = if (documentExpirationDate.isNotBlank()) {
                documentExpirationDate.toDateFormatted().toString()
            } else {
                resourceProvider.getString(R.string.dashboard_document_no_expiration_found)
            }

            if (userImage.isBlank()) {
                userImage = extractValueFromDocumentOrEmpty(
                    document = document,
                    key = DocumentJsonKeys.PORTRAIT
                )
            }

            val documentType = document.docType.toDocumentType()

            return@map DocumentUi(
                documentId = document.id,
                documentName = documentType.toUiName(resourceProvider),
                documentType = documentType,
                documentImage = "",
                documentExpirationDateFormatted = documentExpirationDate,
                documentHasExpired = docHasExpired,
                documentDetails = emptyList()
            )
        }
        emit(
            DashboardInteractorPartialState.Success(
                documents = documentsUi.makeSureOnlyOneProxyPidUiElementPresent(),
                lastAction = lastAction,
                userBase64Portrait = userImage
            )
        )
    }.safeAsync {
        DashboardInteractorPartialState.Failure(
            error = it.localizedMessage ?: genericErrorMsg
        )
    }

    override fun getAppVersion(): String = configLogic.appVersion.replace("-Staging", "")


    private fun List<DocumentUi>.makeSureOnlyOneProxyPidUiElementPresent(): List<DocumentUi> {
        val firstProxyPid: DocumentUi? = firstOrNull { it.isProxy }
        val documentsWithoutProxy = this.filter { !it.isProxy }.toMutableList()
        if (firstProxyPid != null) {
            documentsWithoutProxy.add(
                firstProxyPid
            )
        }
        return documentsWithoutProxy
    }
}