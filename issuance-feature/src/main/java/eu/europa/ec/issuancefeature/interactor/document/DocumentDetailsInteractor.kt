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

import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.ui.document_details.transformer.DocumentDetailsTransformer
import eu.europa.ec.corelogic.controller.DeleteAllDocumentsPartialState
import eu.europa.ec.corelogic.controller.DeleteDocumentPartialState
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DocType
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.corelogic.model.toDocumentIdentifier
import eu.europa.ec.resourceslogic.provider.ResourceProvider

sealed class DocumentDetailsInteractorPartialState {
    data class Success(val documentUi: DocumentUi) : DocumentDetailsInteractorPartialState()
    data class Failure(val error: String) : DocumentDetailsInteractorPartialState()
}

sealed class DocumentDetailsInteractorDeleteDocumentPartialState {
    data object SingleDocumentDeleted : DocumentDetailsInteractorDeleteDocumentPartialState()
    data object AllDocumentsDeleted : DocumentDetailsInteractorDeleteDocumentPartialState()
    data class Failure(val errorMessage: String) :
        DocumentDetailsInteractorDeleteDocumentPartialState()
}

interface DocumentDetailsInteractor {
    suspend fun getDocumentDetails(
        documentId: String,
        documentType: DocType
    ): DocumentDetailsInteractorPartialState

    suspend fun deleteDocument(
        documentId: String,
        documentType: DocType
    ): DocumentDetailsInteractorDeleteDocumentPartialState
}

class DocumentDetailsInteractorImpl(
    private val walletCoreDocumentsController: WalletCoreDocumentsController,
    private val resourceProvider: ResourceProvider,
) : DocumentDetailsInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override suspend fun getDocumentDetails(
        documentId: String,
        documentType: DocType
    ): DocumentDetailsInteractorPartialState {
        val document = try {
            walletCoreDocumentsController.getDocumentWithMetaDataById(id = documentId)
        } catch (exception: Exception) {
            return DocumentDetailsInteractorPartialState.Failure(
                error = exception.localizedMessage ?: genericErrorMsg
            )
        }

        return document?.let {
            val itemUi = DocumentDetailsTransformer.transformToUiItem(
                document = it,
                resourceProvider = resourceProvider,
            )
            itemUi?.let { documentUi ->
                DocumentDetailsInteractorPartialState.Success(
                    documentUi = documentUi
                )
            } ?: DocumentDetailsInteractorPartialState.Failure(error = genericErrorMsg)
        } ?: DocumentDetailsInteractorPartialState.Failure(error = genericErrorMsg)
    }

    override suspend fun deleteDocument(
        documentId: String,
        documentType: DocType
    ): DocumentDetailsInteractorDeleteDocumentPartialState {
        val shouldDeleteAllDocuments: Boolean =
            if (documentType.toDocumentIdentifier() == DocumentIdentifier.PID_SDJWT) {
                val allPidDocuments =
                    walletCoreDocumentsController.getAllDocumentsByType(documentIdentifier = DocumentIdentifier.PID_SDJWT)

                if (allPidDocuments.count() > 1) {
                    walletCoreDocumentsController.getMainPidDocument()?.id == documentId
                } else {
                    true
                }
            } else {
                false
            }

        return if (shouldDeleteAllDocuments) {
            deleteAllDocuments(mainPidId = documentId)
        } else {
            deleteADocument(documentId = documentId)
        }
    }

    private suspend fun deleteAllDocuments(mainPidId: String): DocumentDetailsInteractorDeleteDocumentPartialState {
        val result =
            try {
                walletCoreDocumentsController.deleteAllDocuments(mainPidDocumentId = mainPidId)
            } catch (exception: Exception) {
                DeleteAllDocumentsPartialState.Failure(
                    errorMessage = exception.localizedMessage ?: genericErrorMsg
                )
            }
        return when (result) {
            is DeleteAllDocumentsPartialState.Failure -> DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                errorMessage = result.errorMessage
            )

            is DeleteAllDocumentsPartialState.Success -> DocumentDetailsInteractorDeleteDocumentPartialState.AllDocumentsDeleted
        }
    }

    private suspend fun deleteADocument(documentId: String): DocumentDetailsInteractorDeleteDocumentPartialState {
        val result =
            try {
                walletCoreDocumentsController.deleteDocument(documentId = documentId)
            } catch (exception: Exception) {
                DeleteDocumentPartialState.Failure(
                    errorMessage = exception.localizedMessage ?: genericErrorMsg
                )
            }
        return when (result) {
            is DeleteDocumentPartialState.Failure -> DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                errorMessage = result.errorMessage
            )

            is DeleteDocumentPartialState.Success -> DocumentDetailsInteractorDeleteDocumentPartialState.SingleDocumentDeleted
        }
    }
}