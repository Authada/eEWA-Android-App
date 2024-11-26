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
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.commonfeature.util.TestsData
import eu.europa.ec.commonfeature.util.TestsData.mockedImage
import eu.europa.ec.commonfeature.util.TestsData.mockedPidSdjwtDocType
import eu.europa.ec.corelogic.controller.DeleteAllDocumentsPartialState
import eu.europa.ec.corelogic.controller.DeleteDocumentPartialState
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.eudi.wallet.document.Document
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.testfeature.MockResourceProviderForStringCalls.mockDocumentTypeUiToUiNameCall
import eu.europa.ec.testfeature.MockResourceProviderForStringCalls.mockTransformToUiItemCall
import eu.europa.ec.testfeature.mockedEmptyPid
import eu.europa.ec.testfeature.mockedExceptionWithMessage
import eu.europa.ec.testfeature.mockedExceptionWithNoMessage
import eu.europa.ec.testfeature.mockedGenericErrorMessage
import eu.europa.ec.testfeature.mockedMdlDocType
import eu.europa.ec.testfeature.mockedMdlId
import eu.europa.ec.testfeature.mockedMdlWithBasicFields
import eu.europa.ec.testfeature.mockedOldestPidId
import eu.europa.ec.testfeature.mockedOldestPidWithBasicFields
import eu.europa.ec.testfeature.mockedPidDocType
import eu.europa.ec.testfeature.mockedPidId
import eu.europa.ec.testfeature.mockedPidNameSpace
import eu.europa.ec.testfeature.mockedPidWithBasicFields
import eu.europa.ec.testfeature.mockedPlainFailureMessage
import eu.europa.ec.testfeature.wrapWithMetaData
import eu.europa.ec.testlogic.extension.runTest
import eu.europa.ec.testlogic.rule.CoroutineTestRule
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class TestDocumentDetailsInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var walletCoreDocumentsController: WalletCoreDocumentsController

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    private lateinit var interactor: DocumentDetailsInteractor

    private lateinit var closeable: AutoCloseable

    @Before
    fun before() {
        closeable = MockitoAnnotations.openMocks(this)

        interactor = DocumentDetailsInteractorImpl(
            walletCoreDocumentsController = walletCoreDocumentsController,
            resourceProvider = resourceProvider,
        )
        whenever(resourceProvider.genericErrorMessage()).thenReturn(mockedGenericErrorMessage)
    }

    @After
    fun after() {
        closeable.close()
    }

    // Case 3:
    // 1. walletCoreDocumentsController.getDocumentById() returns an empty PID document.

    // Case 3 Expected Result:
    // DocumentDetailsInteractorPartialState.Failure state,
    // with the generic error message.
    @Test
    fun `Given Case 3, When getDocumentDetails is called, Then Case 3 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockGetDocumentByIdCall(response = mockedEmptyPid)

            // When
            val actual = interactor.getDocumentDetails(
                documentId = mockedPidId,
                documentType = DocumentIdentifier.PID_SDJWT.docType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorPartialState.Failure(
                    error = mockedGenericErrorMessage
                ),
                actual
            )
        }
    }

    // Case 4:
    // 1. walletCoreDocumentsController.getDocumentById() returns null.

    // Case 4 Expected Result:
    // DocumentDetailsInteractorPartialState.Failure state,
    // with the generic error message.
    @Test
    fun `Given Case 4, When getDocumentDetails is called, Then Case 4 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockGetDocumentByIdCall(response = null)

            // When
            val actual = interactor.getDocumentDetails(
                documentId = mockedPidId,
                documentType = DocumentIdentifier.PID_SDJWT.docType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorPartialState.Failure(
                    error = mockedGenericErrorMessage
                ),
                actual
            )
        }
    }

    // Case 5:
    // 1. walletCoreDocumentsController.getDocumentById() returns a PID document, with:
    // no expiration date,
    // no image, and
    // no user name.

    // Case 5 Expected Result:
    // DocumentDetailsInteractorPartialState.Success state, with a PID document UI item, with:
    // an empty string for documentExpirationDateFormatted,
    // an empty string for documentImage, and
    // an empty string for userFullName,
    @Test
    fun `Given Case 5, When getDocumentDetails is called, Then Case 5 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockTransformToUiItemCall(resourceProvider)
            mockDocumentTypeUiToUiNameCall(resourceProvider)

            mockGetDocumentByIdCall(
                response = mockedPidWithBasicFields.copy(
                    docType = mockedPidDocType,
                    nameSpacedData = mapOf(
                        mockedPidNameSpace to mapOf(
                            "no_data_item" to byteArrayOf(0)
                        )
                    )
                )
            )

            // When
            val actual = interactor.getDocumentDetails(
                documentId = mockedPidId,
                documentType = mockedPidDocType
            )
            // Then

            val expected = DocumentDetailsInteractorPartialState.Success(
                documentUi = DocumentUi(
                    documentId = TestsData.mockedPidId,
                    documentName = TestsData.mockedDocUiNamePid,
                    documentIdentifier = DocumentIdentifier.PID_MDOC,
                    documentExpirationDateFormatted = "",
                    documentHasExpired = TestsData.mockedDocumentHasExpired,
                    base64Image = "",
                    documentDetails = listOf(
                        DocumentDetailsUi.DefaultItem(
                            itemData = InfoTextWithNameAndValueData.create(
                                title = "no_data_item",
                                infoValues = arrayOf("0")
                            )
                        )
                    ),
                    userFullName = "",
                    documentMetaData = DocumentMetaData(
                        uniqueDocumentId = TestsData.mockedPidId,
                        documentName = TestsData.mockedDocUiNamePid,
                        logo = mockedImage,
                        backgroundColor = "#FFFFFF",
                        backgroundImage = mockedImage,
                        textColor = "#FFFFFF"
                    ),
                    documentIssuer = "",
                    highlightedFields = emptyList()
                )
            )
            println(actual)
            println("===========")
            println(expected)

            assertEquals(
                expected,
                actual
            )
        }
    }

    // Case 6:
    // 1. walletCoreDocumentsController.getDocumentById() throws an exception with a message.

    // Case 6 Expected Result:
    // DocumentDetailsInteractorPartialState.Failure state,
    // with the exception's localized message.
    @Test
    fun `Given Case 6, When getDocumentDetails is called, Then Case 6 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            whenever(walletCoreDocumentsController.getDocumentWithMetaDataById(mockedPidId))
                .thenThrow(mockedExceptionWithMessage)

            // When
            val actual = interactor.getDocumentDetails(
                documentId = mockedPidId,
                documentType = DocumentIdentifier.PID_SDJWT.docType
            )
            // Then
            val expected = DocumentDetailsInteractorPartialState.Failure(
                error = mockedExceptionWithMessage.localizedMessage!!
            )

            println(actual)
            println("===========")
            println(expected)

            assertEquals(
                expected,
                actual
            )
        }
    }

    // Case 7:
    // 1. walletCoreDocumentsController.getDocumentById() throws an exception with no message.

    // Case 7 Expected Result:
    // DocumentDetailsInteractorPartialState.Failure state,
    // with the generic error message.
    @Test
    fun `Given Case 7, When getDocumentDetails is called, Then Case 7 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            whenever(walletCoreDocumentsController.getDocumentWithMetaDataById(mockedPidId))
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            val actual = interactor.getDocumentDetails(
                documentId = mockedPidId,
                documentType = DocumentIdentifier.PID_SDJWT.docType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorPartialState.Failure(
                    error = mockedGenericErrorMessage
                ),
                actual
            )
        }
    }

    //endregion

    //region deleteDocument

    // Case 1:

    // 1. A documentId and document is PID.
    // 2. walletCoreDocumentsController.getAllDocuments() returns 1 Document and it is PID.
    // 3. walletCoreDocumentsController.deleteAllDocuments() returns Failure.
    @Test
    fun `Given Case 1, When deleteDocument is called, Then it returns Failure with failure's error message`() {
        coroutineRule.runTest {
            // Given
            mockGetAllDocumentsCall(
                response = listOf(
                    mockedPidWithBasicFields.copy(
                        docType = mockedPidSdjwtDocType
                    )
                )
            )
            mockDeleteAllDocumentsCall(
                response = DeleteAllDocumentsPartialState.Failure(
                    errorMessage = mockedPlainFailureMessage
                )
            )

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedPidId,
                documentType = mockedPidSdjwtDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                    errorMessage = mockedPlainFailureMessage
                ),
                actual
            )
        }
    }

    // Case 2:

    // 1. A documentId and document is PID.
    // 2. walletCoreDocumentsController.getAllDocuments() returns 1 Document and it is PID.
    // 3. walletCoreDocumentsController.deleteAllDocuments() returns Success.
    @Test
    fun `Given Case 2, When deleteDocument is called, Then it returns AllDocumentsDeleted`() {
        coroutineRule.runTest {
            // Given
            mockGetAllDocumentsCall(
                response = listOf(
                    mockedPidWithBasicFields.copy(
                        docType = mockedPidSdjwtDocType,
                    )
                )
            )
            mockDeleteAllDocumentsCall(response = DeleteAllDocumentsPartialState.Success)

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedPidId,
                documentType = mockedPidSdjwtDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.AllDocumentsDeleted,
                actual
            )
        }
    }

    // Case 3:

    // 1. A documentId and document is PID.
    // 2. walletCoreDocumentsController.getAllDocuments() returns more than 1 PIDs
    //      AND the documentId we are about to delete IS the one of the oldest PID.
    // 3. walletCoreDocumentsController.deleteAllDocuments() returns Success.
    @Test
    fun `Given Case 3, When deleteDocument is called, Then it returns AllDocumentsDeleted`() {
        coroutineRule.runTest {
            // Given
            mockGetAllDocumentsCall(
                response = listOf(
                    mockedMdlWithBasicFields,
                    mockedPidWithBasicFields.copy(
                        docType = mockedPidSdjwtDocType,
                    ),
                    mockedOldestPidWithBasicFields.copy(
                        docType = mockedPidSdjwtDocType,
                    )
                )
            )
            mockDeleteAllDocumentsCall(response = DeleteAllDocumentsPartialState.Success)

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedOldestPidId,
                documentType = mockedPidSdjwtDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.AllDocumentsDeleted,
                actual
            )
        }
    }

    // Case 4:

    // 1. A documentId and document is PID.
    // 2. walletCoreDocumentsController.getAllDocuments(documentIdentifier: DocumentIdentifier) returns more than 1 PIDs
    //      AND the documentId we are about to delete is NOT the one of the oldest PID.
    // 3. walletCoreDocumentsController.deleteDocument() returns Success.
    @Test
    fun `Given Case 4, When deleteDocument is called, Then it returns SingleDocumentDeleted`() {
        coroutineRule.runTest {
            // Given
            mockGetAllDocumentsWithTypeCall(
                response = listOf(
                    mockedPidWithBasicFields,
                    mockedOldestPidWithBasicFields
                )
            )
            mockDeleteDocumentCall(response = DeleteDocumentPartialState.Success)
            mockGetMainPidDocument(mockedOldestPidWithBasicFields)

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedPidId,
                documentType = mockedPidDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.SingleDocumentDeleted,
                actual
            )
        }
    }

    // Case 5:

    // 1. A documentId and document is mDL.
    // 2. walletCoreDocumentsController.deleteDocument() returns Failure.
    @Test
    fun `Given Case 5, When deleteDocument is called, Then it returns Failure with failure's error message`() {
        coroutineRule.runTest {
            // Given
            mockDeleteDocumentCall(
                response = DeleteDocumentPartialState.Failure(
                    errorMessage = mockedPlainFailureMessage
                )
            )

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedMdlId,
                documentType = mockedMdlDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                    errorMessage = mockedPlainFailureMessage
                ),
                actual
            )
        }
    }

    // Case 6:

    // 1. A documentId and document is mDL.
    // 2. walletCoreDocumentsController.deleteDocument() returns Success.
    @Test
    fun `Given Case 6, When deleteDocument is called, Then it returns SingleDocumentDeleted`() {
        coroutineRule.runTest {
            // Given
            mockDeleteDocumentCall(response = DeleteDocumentPartialState.Success)

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedMdlId,
                documentType = mockedMdlDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.SingleDocumentDeleted,
                actual
            )
        }
    }

    // Case 7:

    // 1. A documentId and document is mDL.
    // 2. walletCoreDocumentsController.deleteDocument() throws an exception with a message.
    @Test
    fun `Given Case 7, When deleteDocument is called, Then it returns Failure with the exception's localized message`() {
        coroutineRule.runTest {
            // Given
            whenever(walletCoreDocumentsController.deleteDocument(mockedMdlId))
                .thenThrow(mockedExceptionWithMessage)

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedMdlId,
                documentType = mockedMdlDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                    errorMessage = mockedExceptionWithMessage.localizedMessage!!
                ),
                actual
            )
        }
    }

    // Case 8:

    // 1. A documentId and document is mDL.
    // 2. walletCoreDocumentsController.deleteDocument() throws an exception with no message.
    @Test
    fun `Given Case 8, When deleteDocument is called, Then it returns Failure with the generic error message`() {
        coroutineRule.runTest {
            // Given
            whenever(walletCoreDocumentsController.deleteDocument(mockedMdlId))
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            val actual = interactor.deleteDocument(
                documentId = mockedMdlId,
                documentType = mockedMdlDocType
            )
            // Then
            assertEquals(
                DocumentDetailsInteractorDeleteDocumentPartialState.Failure(
                    errorMessage = mockedGenericErrorMessage
                ),
                actual
            )
        }
    }
    //endregion

    //region helper functions
    private suspend fun mockGetAllDocumentsCall(response: List<Document>) {
        whenever(walletCoreDocumentsController.getAllDocumentsWithMetaData())
            .thenReturn(response.map { it.wrapWithMetaData()!! })
    }

    private fun mockGetAllDocumentsWithTypeCall(response: List<Document>) {
        whenever(walletCoreDocumentsController.getAllDocumentsByType(documentIdentifier = any()))
            .thenReturn(response)
    }

    private suspend fun mockGetDocumentByIdCall(response: Document?) {
        whenever(walletCoreDocumentsController.getDocumentWithMetaDataById(anyString()))
            .thenReturn(response.wrapWithMetaData())
    }

    private fun mockGetMainPidDocument(response: Document?) {
        whenever(walletCoreDocumentsController.getMainPidDocument())
            .thenReturn(response)
    }

    private suspend fun mockDeleteAllDocumentsCall(response: DeleteAllDocumentsPartialState) {
        whenever(walletCoreDocumentsController.deleteAllDocuments(anyString()))
            .thenReturn(response)
    }

    private suspend fun mockDeleteDocumentCall(response: DeleteDocumentPartialState) {
        whenever(walletCoreDocumentsController.deleteDocument(anyString()))
            .thenReturn(response)
    }
    //endregion
}