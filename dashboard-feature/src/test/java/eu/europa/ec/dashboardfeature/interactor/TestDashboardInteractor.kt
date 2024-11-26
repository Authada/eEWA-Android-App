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
import eu.europa.ec.commonfeature.util.TestsData.mockedFullDocumentsUi
import eu.europa.ec.commonfeature.util.TestsData.mockedMdlUiWithNoExpirationDate
import eu.europa.ec.commonfeature.util.TestsData.mockedNoExpirationDateFound
import eu.europa.ec.commonfeature.util.TestsData.mockedUserBase64Portrait
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.dashboardfeature.interactor.DocumentToDashboardModelMapper.mapToDashboardUi
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.testfeature.MockResourceProviderForStringCalls.mockDocumentTypeUiToUiNameCall
import eu.europa.ec.testfeature.MockResourceProviderForStringCalls.mockTransformToUiItemCall
import eu.europa.ec.testfeature.mockedExceptionWithMessage
import eu.europa.ec.testfeature.mockedExceptionWithNoMessage
import eu.europa.ec.testfeature.mockedFullDocumentsWithMetadata
import eu.europa.ec.testfeature.mockedGenericErrorMessage
import eu.europa.ec.testfeature.mockedMdlWithNoExpirationDate
import eu.europa.ec.testfeature.wrapWithMetaData
import eu.europa.ec.testlogic.base.TestApplication
import eu.europa.ec.testlogic.extension.runTest
import eu.europa.ec.testlogic.rule.CoroutineTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class TestDashboardInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var walletDocumentsController: WalletCoreDocumentsController

    @Mock
    private lateinit var configLogic: ConfigLogic

    private lateinit var interactor: DashboardInteractor

    private lateinit var closeable: AutoCloseable

    @Before
    fun before() {
        closeable = MockitoAnnotations.openMocks(this)

        interactor = DashboardInteractorImpl(
            resourceProvider = resourceProvider,
            walletCoreDocumentsController = walletDocumentsController,
            configLogic = configLogic
        )

        whenever(resourceProvider.genericErrorMessage()).thenReturn(mockedGenericErrorMessage)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getDocuments

    // Case 1:
    // walletDocumentsController.getAllDocumentsWithMetaData() returns
    // a full PID and a full mDL.

    // Case 1 Expected Result:
    // DashboardInteractorPartialState.Success state, with:
    // 1. the list of Documents transformed to DocumentUi objects,
    // 2. an actual lastAction
    // 3. an actual (base64 encoded) user image.
    @Test
    fun `Given Case 1, When getDocuments is called, Then Case 1 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockTransformToUiItemCall(resourceProvider)
            mockGetStringForDocumentsCall(resourceProvider)

            whenever(walletDocumentsController.getAllDocumentsWithMetaData())
                .thenReturn(mockedFullDocumentsWithMetadata)

            // When
            val actual = interactor.getStoredDocumentsWithMetadata()

            val expectedDocuments = mockedFullDocumentsUi.mapToDashboardUi(
                resourceProvider
            )
            // Then
            assertEquals(
                DashboardInteractorPartialState.Success(
                    documents = expectedDocuments,
                    userBase64Portrait = ""
                ),
                actual
            )
        }
    }

    // Case 3:
    // walletDocumentsController.getAllDocumentsWithMetaData() returns
    // an mDL with no expiration date.

    // Case 3 Expected Result:
    // DashboardInteractorPartialState.Success state, with:
    // 1. the Document transformed to DocumentUi object,
    // 2. and without lastAction
    // 3. an actual (base64 encoded) user image.
    @Test
    fun `Given Case 3, When getDocuments is called, Then Case 3 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockTransformToUiItemCall(resourceProvider)
            mockGetStringForDocumentsCall(resourceProvider)

            whenever(walletDocumentsController.getAllDocumentsWithMetaData())
                .thenReturn(listOf(mockedMdlWithNoExpirationDate.wrapWithMetaData()!!))

            // When
            val actual = interactor.getStoredDocumentsWithMetadata()
            val expected = DashboardInteractorPartialState.Success(
                documents = listOf(mockedMdlUiWithNoExpirationDate).mapToDashboardUi(
                    resourceProvider
                ),
                userBase64Portrait = ""
            )
            // Then
            assertEquals(actual, expected)
        }
    }

    // Case 4:
    // walletDocumentsController.getAllDocumentsWithMetaData() throws an exception with a message.
    @Test
    fun `Given Case 4, When getDocuments is called, Then it returns Failed with exception's localized message`() {
        coroutineRule.runTest {
            // Given
            whenever(walletDocumentsController.getAllDocumentsWithMetaData())
                .thenThrow(mockedExceptionWithMessage)

            // When
            val actual = interactor.getStoredDocumentsWithMetadata()

            // Then
            assertEquals(
                DashboardInteractorPartialState.Failure(
                    error = mockedExceptionWithMessage.localizedMessage!!
                ),
                actual
            )
        }
    }

    // Case 5:
    // walletDocumentsController.getAllDocumentsWithMetaData() throws an exception with no message.
    @Test
    fun `Given Case 5, When getDocuments is called, Then it returns Failed with the generic error message`() {
        coroutineRule.runTest {
            // Given
            whenever(walletDocumentsController.getAllDocumentsWithMetaData())
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            val actual = interactor.getStoredDocumentsWithMetadata()

            // Then
            assertEquals(
                DashboardInteractorPartialState.Failure(
                    error = mockedGenericErrorMessage
                ),
                actual
            )
        }
    }
    //endregion

    //region getAppVersion
    @Test
    fun `Given an App Version, When getAppVersion is called, Then it returns the Apps Version`() {
        // Given
        val expectedAppVersion = "2024.01.1"
        whenever(configLogic.appVersion)
            .thenReturn(expectedAppVersion)

        // When
        val actualAppVersion = interactor.getAppVersion()

        // Then
        assertEquals(expectedAppVersion, actualAppVersion)
        verify(configLogic, times(1))
            .appVersion
    }
    //endregion

    //region Mock Calls of the Dependencies

    private fun mockGetStringForDocumentsCall(resourceProvider: ResourceProvider) {
        mockDocumentTypeUiToUiNameCall(resourceProvider)

        whenever(resourceProvider.getString(R.string.dashboard_document_no_expiration_found))
            .thenReturn(mockedNoExpirationDateFound)
    }
    //endregion
}