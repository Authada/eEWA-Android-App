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

import eu.europa.ec.commonfeature.config.PresentationMode
import eu.europa.ec.commonfeature.config.RequestUriConfig
import eu.europa.ec.commonfeature.ui.request.Event
import eu.europa.ec.commonfeature.ui.request.model.RequestDataUi
import eu.europa.ec.commonfeature.util.TestsData.createTransformedRequestDataUi
import eu.europa.ec.commonfeature.util.TestsData.mockedRequestElementIdentifierNotAvailable
import eu.europa.ec.commonfeature.util.TestsData.mockedRequestRequiredFieldsTitle
import eu.europa.ec.commonfeature.util.TestsData.mockedTransformedRequestDataUiForMdlWithBasicFields
import eu.europa.ec.commonfeature.util.TestsData.mockedTransformedRequestDataUiForPidWithBasicFields
import eu.europa.ec.commonfeature.util.TestsData.mockedValidMdlWithBasicFieldsRequestDocument
import eu.europa.ec.commonfeature.util.TestsData.mockedValidPidWithBasicFieldsRequestDocument
import eu.europa.ec.commonfeature.util.TestsData.mockedValidReaderAuth
import eu.europa.ec.commonfeature.util.TestsData.mockedVerifierName
import eu.europa.ec.corelogic.controller.PresentationControllerConfig
import eu.europa.ec.corelogic.controller.TransferEventPartialState
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.controller.WalletCorePresentationController
import eu.europa.ec.eudi.iso18013.transfer.DocRequest
import eu.europa.ec.eudi.wallet.document.Document
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.testfeature.MockResourceProviderForStringCalls.mockTransformToUiItemsCall
import eu.europa.ec.testfeature.mockedExceptionWithMessage
import eu.europa.ec.testfeature.mockedExceptionWithNoMessage
import eu.europa.ec.testfeature.mockedGenericErrorMessage
import eu.europa.ec.testfeature.mockedMdlWithBasicFields
import eu.europa.ec.testfeature.mockedPidDocType
import eu.europa.ec.testfeature.mockedPidWithBasicFields
import eu.europa.ec.testfeature.mockedPlainFailureMessage
import eu.europa.ec.testfeature.mockedVerifierIsTrusted
import eu.europa.ec.testlogic.extension.expectNoEvents
import eu.europa.ec.testlogic.extension.runFlowTest
import eu.europa.ec.testlogic.extension.runTest
import eu.europa.ec.testlogic.extension.toFlow
import eu.europa.ec.testlogic.rule.CoroutineTestRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI

class TestProximityRequestInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var walletCorePresentationController: WalletCorePresentationController

    @Mock
    private lateinit var walletCoreDocumentsController: WalletCoreDocumentsController

    private lateinit var interactor: ProximityRequestInteractor

    private lateinit var closeable: AutoCloseable

    @Before
    fun before() {
        closeable = MockitoAnnotations.openMocks(this)

        interactor = ProximityRequestInteractorImpl(
            resourceProvider = resourceProvider,
            walletCorePresentationController = walletCorePresentationController,
            walletCoreDocumentsController = walletCoreDocumentsController,
            configLogic = mock()
        )

        whenever(resourceProvider.genericErrorMessage()).thenReturn(mockedGenericErrorMessage)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getRequestDocuments

    // Case 1:
    // 1. walletCorePresentationController.events emits an event that we do not want to react to, i.e:
    // TransferEventPartialState
    //  .Connected, or
    //  .Connecting, or
    //  .QrEngagementReady, or
    //  .Redirect, or
    //  .ResponseSent

    // Case 1 Expected Result:
    // No new partial state/flow emission.
    @Test
    fun `Given Case 1, When getRequestDocuments is called, Then Case 1 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockEmissionOfIntentionallyNotHandledEvents()

            // When
            interactor.getRequestDocuments()
                // Then
                .expectNoEvents()
        }
    }

    // Case 2:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.Error, with an error message.

    // Case 2 Expected Result:
    // ProximityRequestInteractorPartialState.Failure state, with the same error message.
    @Test
    fun `Given Case 2, When getRequestDocuments is called, Then Case 2 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.Error(
                    error = mockedPlainFailureMessage
                )
            )

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.Failure(
                            error = mockedPlainFailureMessage
                        ),
                        awaitItem()
                    )
                }
        }
    }

    // Case 3:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.Disconnected.

    // Case 3 Expected Result:
    // ProximityRequestInteractorPartialState.Disconnect state.
    @Test
    fun `Given Case 3, When getRequestDocuments is called, Then Case 3 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.Disconnected
            )

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.Disconnect,
                        awaitItem()
                    )
                }
        }
    }

    // Case 4:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.RequestReceived, with:
    // 1. an emptyList() for requestData,
    // 2. a not null String for verifier name,
    // 3. true for verifierIsTrusted.

    // Case 4 Expected Result:
    // ProximityRequestInteractorPartialState.NoData state, with:
    // 1. the same not null String for verifier name,
    // 2. true for verifierIsTrusted.
    @Test
    fun `Given Case 4, When getRequestDocuments is called, Then Case 4 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.RequestReceived(
                    requestData = emptyList(),
                    verifierName = mockedVerifierName,
                    verifierIsTrusted = mockedVerifierIsTrusted
                )
            )

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.NoData(
                            verifierName = mockedVerifierName,
                            verifierIsTrusted = mockedVerifierIsTrusted
                        ),
                        awaitItem()
                    )
                }
        }
    }

    // Case 5:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.RequestReceived, with:
    // 1. an emptyList() for requestData,
    // 2. a null String for verifier name,
    // 3. false for verifierIsTrusted.

    // Case 5 Expected Result:
    // ProximityRequestInteractorPartialState.NoData state, with:
    // 1. null String for verifier name,
    // 2. false for verifierIsTrusted.
    @Test
    fun `Given Case 5, When getRequestDocuments is called, Then Case 5 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.RequestReceived(
                    requestData = emptyList(),
                    verifierName = null,
                    verifierIsTrusted = false
                )
            )

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.NoData(
                            verifierName = null,
                            verifierIsTrusted = false
                        ),
                        awaitItem()
                    )
                }
        }
    }

    // Case 6:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.RequestReceived, with:
    // 1. a list of a PID RequestDocument, with empty list for docRequest.requestItems, for requestData,
    // 2. a not null String for verifier name,
    // 3. true for verifierIsTrusted.

    // Case 6 Expected Result:
    // ProximityRequestInteractorPartialState.NoData state, with:
    // 1. the same not null String for verifier name,
    // 2. true for verifierIsTrusted.
    @Test
    fun `Given Case 6, When getRequestDocuments is called, Then Case 6 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            whenever(resourceProvider.getString(R.string.request_required_fields_title))
                .thenReturn(mockedRequestRequiredFieldsTitle)

            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.RequestReceived(
                    requestData = listOf(
                        mockedValidPidWithBasicFieldsRequestDocument
                            .copy(
                                docRequest = DocRequest(
                                    docType = mockedPidDocType,
                                    requestItems = emptyList(),
                                    readerAuth = mockedValidReaderAuth
                                )
                            )
                    ),
                    verifierName = mockedVerifierName,
                    verifierIsTrusted = mockedVerifierIsTrusted
                )
            )

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.NoData(
                            verifierName = mockedVerifierName,
                            verifierIsTrusted = mockedVerifierIsTrusted
                        ),
                        awaitItem()
                    )
                }
        }
    }

    // Case 7:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.RequestReceived, with:
    // 1. a list of a PID RequestDocument, with some basic fields,
    // 2. a not null String for verifier name,
    // 3. true for verifierIsTrusted.

    // Case 7 Expected Result:
    // ProximityRequestInteractorPartialState.Success state, with:
    // 1. a list with the transformed basic fields to RequestDataUi items,
    // 2. the same not null String for verifier name,
    // 3. true for verifierIsTrusted.
    @Test
    fun `Given Case 7, When getRequestDocuments is called, Then Case 7 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockGetAllDocumentsCall(
                response = listOf(mockedPidWithBasicFields)
            )
            whenever(resourceProvider.getString(R.string.request_required_fields_title))
                .thenReturn(mockedRequestRequiredFieldsTitle)
            mockTransformToUiItemsCall(
                resourceProvider = resourceProvider,
                notAvailableString = mockedRequestElementIdentifierNotAvailable
            )

            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.RequestReceived(
                    requestData = listOf(
                        mockedValidPidWithBasicFieldsRequestDocument
                    ),
                    verifierName = mockedVerifierName,
                    verifierIsTrusted = mockedVerifierIsTrusted
                )
            )

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.Success(
                            verifierName = mockedVerifierName,
                            verifierIsTrusted = mockedVerifierIsTrusted,
                            requestDocuments = createTransformedRequestDataUi(
                                items = listOf(
                                    mockedTransformedRequestDataUiForPidWithBasicFields
                                )
                            )
                        ),
                        awaitItem()
                    )
                }
        }
    }

    // Case 8:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.RequestReceived, with:
    // 1. a list of an mDL RequestDocument, with some basic fields,
    // 2. a not null String for verifier name,
    // 3. true for verifierIsTrusted.

    // Case 8 Expected Result:
    // ProximityRequestInteractorPartialState.Success state, with:
    // 1. a list with the transformed basic fields to RequestDataUi items,
    // 2. the same not null String for verifier name,
    // 3. true for verifierIsTrusted.
    @Test
    fun `Given Case 8, When getRequestDocuments is called, Then Case 8 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockGetAllDocumentsCall(
                response = listOf(mockedMdlWithBasicFields)
            )
            whenever(resourceProvider.getString(R.string.request_required_fields_title))
                .thenReturn(mockedRequestRequiredFieldsTitle)
            mockTransformToUiItemsCall(
                resourceProvider = resourceProvider,
                notAvailableString = mockedRequestElementIdentifierNotAvailable
            )

            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.RequestReceived(
                    requestData = listOf(
                        mockedValidMdlWithBasicFieldsRequestDocument
                    ),
                    verifierName = mockedVerifierName,
                    verifierIsTrusted = mockedVerifierIsTrusted
                )
            )

            val expected = ProximityRequestInteractorPartialState.Success(
                verifierName = mockedVerifierName,
                verifierIsTrusted = mockedVerifierIsTrusted,
                requestDocuments = createTransformedRequestDataUi(
                    items = listOf(
                        mockedTransformedRequestDataUiForMdlWithBasicFields
                    )
                )
            )

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        expected,
                        awaitItem()
                    )
                }
        }
    }

    // Case 11:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.RequestReceived, with:
    // 1. a list of a PID RequestDocument, with some basic fields,
    // 2. a not null String for verifier name,
    // 3. true for verifierIsTrusted,
    // 4. walletCoreDocumentsController.getAllDocuments() throws an exception with a message.

    // Case 11 Expected Result:
    // ProximityRequestInteractorPartialState.Failure state, with:
    // 1. exception's localized message.
    @Test
    fun `Given Case 11, When getRequestDocuments is called, Then Case 11 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.RequestReceived(
                    requestData = listOf(
                        mockedValidPidWithBasicFieldsRequestDocument
                    ),
                    verifierName = mockedVerifierName,
                    verifierIsTrusted = mockedVerifierIsTrusted
                )
            )
            whenever(walletCoreDocumentsController.getAllDocuments())
                .thenThrow(mockedExceptionWithMessage)

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.Failure(
                            error = mockedExceptionWithMessage.localizedMessage!!
                        ),
                        awaitItem()
                    )
                }
        }
    }

    // Case 12:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.RequestReceived, with:
    // 1. a list of a PID RequestDocument, with some basic fields,
    // 2. a not null String for verifier name,
    // 3. true for verifierIsTrusted,
    // 4. walletCoreDocumentsController.getAllDocuments() throws an exception with no message.

    // Case 12 Expected Result:
    // ProximityRequestInteractorPartialState.Failure state, with:
    // 1. the generic error message.
    @Test
    fun `Given Case 12, When getRequestDocuments is called, Then Case 12 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.RequestReceived(
                    requestData = listOf(
                        mockedValidPidWithBasicFieldsRequestDocument
                    ),
                    verifierName = mockedVerifierName,
                    verifierIsTrusted = mockedVerifierIsTrusted
                )
            )
            whenever(walletCoreDocumentsController.getAllDocuments())
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            interactor.getRequestDocuments()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityRequestInteractorPartialState.Failure(
                            error = mockedGenericErrorMessage
                        ),
                        awaitItem()
                    )
                }
        }
    }
    //endregion

    //region stopPresentation
    @Test
    fun `Verify that stopPresentation calls walletCorePresentationController#stopPresentation`() {
        interactor.stopPresentation()

        verify(walletCorePresentationController, times(1))
            .stopPresentation()
    }
    //endregion

    //region updateRequestedDocuments
    @Test
    fun `Verify that updateRequestedDocuments calls walletCorePresentationController#updateRequestedDocuments`() {
        val uiItems: List<RequestDataUi<Event>> = createTransformedRequestDataUi(
            items = listOf(
                mockedTransformedRequestDataUiForMdlWithBasicFields
            )
        )

        interactor.updateRequestedDocuments(items = uiItems)

        verify(walletCorePresentationController, times(1))
            .updateRequestedDocuments(disclosedDocuments = any())
    }
    //endregion

    //region setConfig
    @Test
    fun `Given a RequestUriConfig with Ble mode, When setConfig is called, Then it calls walletCorePresentationController#setConfig with PresentationControllerConfig_Ble`() {
        // Given
        val config = RequestUriConfig(
            mode = PresentationMode.Ble
        )

        // When
        interactor.setConfig(config = config)

        // Then
        verify(walletCorePresentationController, times(1))
            .setConfig(PresentationControllerConfig.Ble)
    }

    @Test
    fun `Given a RequestUriConfig with OpenId4Vp mode, When setConfig is called, Then it calls walletCorePresentationController#setConfig with PresentationControllerConfig_OpenId4Vp`() {
        // Given
        val config = RequestUriConfig(
            mode = PresentationMode.OpenId4Vp(uri = "")
        )

        // When
        interactor.setConfig(config = config)

        // Then
        verify(walletCorePresentationController, times(1))
            .setConfig(PresentationControllerConfig.OpenId4VP(uri = ""))
    }
    //endregion

    //region helper functions
    private fun mockWalletCorePresentationControllerEventEmission(event: TransferEventPartialState) {
        whenever(walletCorePresentationController.events)
            .thenReturn(event.toFlow())
    }

    private fun mockEmissionOfIntentionallyNotHandledEvents() {
        whenever(walletCorePresentationController.events)
            .thenReturn(
                flow {
                    emit(TransferEventPartialState.Connected)
                    emit(TransferEventPartialState.Connecting)
                    emit(TransferEventPartialState.QrEngagementReady(""))
                    emit(TransferEventPartialState.Redirect(uri = URI("")))
                    emit(TransferEventPartialState.ResponseSent)
                }
            )
    }

    private suspend fun mockGetAllDocumentsCall(response: List<Document>) {
        whenever(walletCoreDocumentsController.getAllDocumentsWithMetaData())
            .thenReturn(response)

        whenever(walletCoreDocumentsController.getAllDocuments())
            .thenReturn(response)
    }
    //endregion
}