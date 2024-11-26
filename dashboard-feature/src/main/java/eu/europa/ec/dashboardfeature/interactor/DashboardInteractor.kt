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

import eu.europa.ec.businesslogic.config.ConfigLogic
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.model.isProxy
import eu.europa.ec.commonfeature.ui.document_details.transformer.DocumentDetailsTransformer
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.dashboardfeature.DashboardDocumentModel
import eu.europa.ec.dashboardfeature.interactor.DocumentToDashboardModelMapper.mapToDashboardUi
import eu.europa.ec.resourceslogic.provider.ResourceProvider

sealed class DashboardInteractorPartialState {
    data class Success(
        val documents: List<DashboardDocumentModel>,
        val userBase64Portrait: String,
    ) : DashboardInteractorPartialState()

    data class Failure(val error: String) : DashboardInteractorPartialState()
}

interface DashboardInteractor {
    fun getAppVersion(): String
    suspend fun getStoredDocumentsWithMetadata(): DashboardInteractorPartialState
}

class DashboardInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val walletCoreDocumentsController: WalletCoreDocumentsController,
    private val configLogic: ConfigLogic
) : DashboardInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override suspend fun getStoredDocumentsWithMetadata(): DashboardInteractorPartialState {
        var userImage = ""
        val documents = try {
            walletCoreDocumentsController.getAllDocumentsWithMetaData()
        } catch (exception: Exception) {
            return DashboardInteractorPartialState.Failure(
                error = exception.localizedMessage ?: genericErrorMsg
            )
        }

        val documentsUi = documents.mapNotNull { document ->
            return@mapNotNull DocumentDetailsTransformer.transformToUiItem(
                document = document,
                resourceProvider = resourceProvider,
            )
        }
            .makeSureOnlyOneProxyPidUiElementPresent().mapToDashboardUi(resourceProvider)

        return DashboardInteractorPartialState.Success(
            documents = documentsUi,
            userBase64Portrait = userImage
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