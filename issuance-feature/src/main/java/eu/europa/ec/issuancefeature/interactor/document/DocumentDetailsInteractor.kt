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

import eu.europa.ec.businesslogic.extension.safeAsync
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.ui.document_details.transformer.DocumentDetailsTransformer
import eu.europa.ec.corelogic.controller.DeleteAllDocumentsPartialState
import eu.europa.ec.corelogic.controller.DeleteDocumentPartialState
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DocumentType
import eu.europa.ec.corelogic.model.toDocumentType
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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
    fun getDocumentDetails(
        documentId: String,
        documentType: String,
    ): Flow<DocumentDetailsInteractorPartialState>

    fun deleteDocument(
        documentId: String,
        documentType: String
    ): Flow<DocumentDetailsInteractorDeleteDocumentPartialState>
}

class DocumentDetailsInteractorImpl(
    private val walletCoreDocumentsController: WalletCoreDocumentsController,
    private val resourceProvider: ResourceProvider,
) : DocumentDetailsInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getDocumentDetails(
        documentId: String,
        documentType: String
    ): Flow<DocumentDetailsInteractorPartialState> =
        flow {
            val document = walletCoreDocumentsController.getDocumentById(id = documentId)
            document?.let {
                val itemUi = DocumentDetailsTransformer.transformToUiItem(
                    document = it,
                    resourceProvider = resourceProvider,
                    docType = documentType
                )
                itemUi?.let { documentUi ->
                    emit(
                        DocumentDetailsInteractorPartialState.Success(
                            documentUi = documentUi
                        )
                    )
                } ?: emit(DocumentDetailsInteractorPartialState.Failure(error = genericErrorMsg))
            } ?: emit(DocumentDetailsInteractorPartialState.Failure(error = genericErrorMsg))
        }.safeAsync {
            DocumentDetailsInteractorPartialState.Failure(
                error = it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun deleteDocument(
        documentId: String,
        documentType: String
    ): Flow<DocumentDetailsInteractorDeleteDocumentPartialState> =
        flow {

            val shouldDeleteAllDocuments: Boolean =
                if (documentType.toDocumentType() == DocumentType.PID) {

                    val allPidDocuments =
                        walletCoreDocumentsController.getAllDocumentsByType(docType = DocumentType.PID)

                    if (allPidDocuments.count() > 1) {
                        walletCoreDocumentsController.getMainPidDocument()?.id == documentId
                    } else {
                        true
                    }
                } else {
                    false
                }

            if (shouldDeleteAllDocuments) {
                walletCoreDocumentsController.deleteAllDocuments(mainPidDocumentId = documentId)
                    .map {
                        when (it) {
                            is DeleteAllDocumentsPartialState.Failure -> DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                                errorMessage = it.errorMessage
                            )

                            is DeleteAllDocumentsPartialState.Success -> DocumentDetailsInteractorDeleteDocumentPartialState.AllDocumentsDeleted
                        }
                    }
            } else {
                walletCoreDocumentsController.deleteDocument(documentId = documentId).map {
                    when (it) {
                        is DeleteDocumentPartialState.Failure -> DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                            errorMessage = it.errorMessage
                        )

                        is DeleteDocumentPartialState.Success -> DocumentDetailsInteractorDeleteDocumentPartialState.SingleDocumentDeleted
                    }
                }
            }.collect {
                emit(it)
            }
        }.safeAsync {
            DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                errorMessage = it.localizedMessage ?: genericErrorMsg
            )
        }
}