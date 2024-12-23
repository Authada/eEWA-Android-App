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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.activity.ComponentActivity
import eu.europa.ec.commonfeature.config.PresentationMode
import eu.europa.ec.commonfeature.config.RequestUriConfig
import eu.europa.ec.corelogic.config.WalletCoreConfig
import eu.europa.ec.corelogic.controller.PresentationControllerConfig
import eu.europa.ec.corelogic.controller.TransferEventPartialState
import eu.europa.ec.corelogic.controller.WalletCorePresentationController
import eu.europa.ec.eudi.wallet.EudiWalletConfig
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.testfeature.mockedExceptionWithMessage
import eu.europa.ec.testfeature.mockedExceptionWithNoMessage
import eu.europa.ec.testfeature.mockedGenericErrorMessage
import eu.europa.ec.testfeature.mockedPlainFailureMessage
import eu.europa.ec.testfeature.walletcore.getMockedEudiWalletConfig
import eu.europa.ec.testlogic.base.TestApplication
import eu.europa.ec.testlogic.base.createActivity
import eu.europa.ec.testlogic.base.getMockedContext
import eu.europa.ec.testlogic.extension.expectNoEvents
import eu.europa.ec.testlogic.extension.runFlowTest
import eu.europa.ec.testlogic.extension.runTest
import eu.europa.ec.testlogic.extension.toFlow
import eu.europa.ec.testlogic.rule.CoroutineTestRule
import eu.europa.ec.uilogic.container.EudiComponentActivity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flow
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
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.net.URI
import org.robolectric.shadows.ShadowBluetoothAdapter

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class TestProximityQrInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var walletCorePresentationController: WalletCorePresentationController

    @Mock
    private lateinit var walletCoreConfig: WalletCoreConfig

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var shadowBluetoothAdapter: ShadowBluetoothAdapter

    private lateinit var interactor: ProximityQRInteractor

    private lateinit var closeable: AutoCloseable

    @Before
    fun before() {
        closeable = MockitoAnnotations.openMocks(this)

        interactor = ProximityQRInteractorImpl(
            resourceProvider = resourceProvider,
            walletCorePresentationController = walletCorePresentationController,
            walletCoreConfig = walletCoreConfig
        )

        whenever(resourceProvider.genericErrorMessage()).thenReturn(mockedGenericErrorMessage)
        whenever(resourceProvider.provideContext()).thenReturn(getMockedContext())

        bluetoothManager =
            getMockedContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        shadowBluetoothAdapter = Shadows.shadowOf(bluetoothManager.adapter)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region startQrEngagement

    // Case 1:
    // 1. walletCorePresentationController.events emits an event that we do not want to react to, i.e:
    // TransferEventPartialState
    //  .Connecting, or
    //  .RequestReceived, or
    //  .ResponseSent, or
    //  .Redirect

    // Case 1 Expected Result:
    // No new partial state/flow emission.
    // walletCorePresentationController.startQrEngagement() was called exactly once.
    @Test
    fun `Given Case 1, When startQrEngagement is called, Then Case 1 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockEmissionOfIntentionallyNotHandledEvents()

            // When
            interactor.startQrEngagement()
                // Then
                .expectNoEvents()

            verifyStartQrEngagementWasCalledExactlyOnce()
        }
    }

    // Case 2:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.Connected.

    // Case 2 Expected Result:
    // ProximityQRPartialState.Connected state.
    // walletCorePresentationController.startQrEngagement() was called exactly once.
    @Test
    fun `Given Case 2, When startQrEngagement is called, Then Case 2 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.Connected
            )

            // When
            interactor.startQrEngagement()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityQRPartialState.Connected,
                        awaitItem()
                    )
                }

            verifyStartQrEngagementWasCalledExactlyOnce()
        }
    }

    // Case 3:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.Error, with an error message.

    // Case 3 Expected Result:
    // ProximityQRPartialState.Error state, with the same error message.
    // walletCorePresentationController.startQrEngagement() was called exactly once.
    @Test
    fun `Given Case 3, When startQrEngagement is called, Then Case 3 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.Error(
                    error = mockedPlainFailureMessage
                )
            )

            // When
            interactor.startQrEngagement()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityQRPartialState.Error(
                            error = mockedPlainFailureMessage
                        ),
                        awaitItem()
                    )
                }

            verifyStartQrEngagementWasCalledExactlyOnce()
        }
    }

    // Case 4:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.QrEngagementReady, with a QR code.

    // Case 4 Expected Result:
    // ProximityQRPartialState.QrReady state, with the same QR code.
    // walletCorePresentationController.startQrEngagement() was called exactly once.
    @Test
    fun `Given Case 4, When startQrEngagement is called, Then Case 4 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            val mockedQrCode = "some qr code"
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.QrEngagementReady(
                    qrCode = mockedQrCode
                )
            )

            // When
            interactor.startQrEngagement()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityQRPartialState.QrReady(
                            qrCode = mockedQrCode
                        ),
                        awaitItem()
                    )
                }

            verifyStartQrEngagementWasCalledExactlyOnce()
        }
    }

    // Case 5:
    // 1. walletCorePresentationController.events emits:
    // TransferEventPartialState.Disconnected.

    // Case 5 Expected Result:
    // ProximityQRPartialState.Disconnected state.
    // walletCorePresentationController.startQrEngagement() was called exactly once.
    @Test
    fun `Given Case 5, When startQrEngagement is called, Then Case 5 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            mockWalletCorePresentationControllerEventEmission(
                event = TransferEventPartialState.Disconnected
            )

            // When
            interactor.startQrEngagement()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityQRPartialState.Disconnected,
                        awaitItem()
                    )
                }

            verifyStartQrEngagementWasCalledExactlyOnce()
        }
    }

    // Case 6:
    // 1. walletCorePresentationController.startQrEngagement() throws an exception with a message.

    // Case 6 Expected Result:
    // ProximityQRPartialState.Error state, with:
    // 1. exception's localized message.
    @Test
    fun `Given Case 6, When startQrEngagement is called, Then Case 6 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            whenever(walletCorePresentationController.startQrEngagement())
                .thenThrow(mockedExceptionWithMessage)

            // When
            interactor.startQrEngagement()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityQRPartialState.Error(
                            error = mockedExceptionWithMessage.localizedMessage!!
                        ),
                        awaitItem()
                    )
                }
        }
    }

    // Case 7:
    // 1. walletCorePresentationController.startQrEngagement() throws an exception with no message.

    // Case 7 Expected Result:
    // ProximityQRPartialState.Error state, with:
    // 1. the generic error message.
    @Test
    fun `Given Case 7, When startQrEngagement is called, Then Case 7 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            whenever(walletCorePresentationController.startQrEngagement())
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            interactor.startQrEngagement()
                .runFlowTest {
                    // Then
                    assertEquals(
                        ProximityQRPartialState.Error(
                            error = mockedGenericErrorMessage
                        ),
                        awaitItem()
                    )
                }
        }
    }
    //endregion

    //region cancelTransfer
    @Test
    fun `Verify that cancelTransfer calls walletCorePresentationController#stopPresentation`() {
        interactor.cancelTransfer()

        verify(walletCorePresentationController, times(1))
            .stopPresentation()
    }
    //endregion

    //region toggleNfcEngagement

    // Case 1:
    // 1. a Component Activity,
    // 2. true for toggle.

    // Case 1 Expected Result:
    // walletCorePresentationController.toggleNfcEngagement() was called exactly once,
    // with those exact arguments.
    @Test
    fun `Given Case 1, When toggleNfcEngagement is called, Then Case 1 Expected Result is returned`() {
        val componentActivity =
            createActivity(EudiComponentActivity::class.java) as ComponentActivity

        interactor.toggleNfcEngagement(
            componentActivity = componentActivity,
            toggle = true
        )

        verify(walletCorePresentationController, times(1))
            .toggleNfcEngagement(
                componentActivity = componentActivity,
                toggle = true
            )
    }

    // Case 2:
    // 1. a Component Activity,
    // 2. false for toggle.

    // Case 2 Expected Result:
    // walletCorePresentationController.toggleNfcEngagement() was called exactly once,
    // with those exact arguments.
    @Test
    fun `Given Case 2, When toggleNfcEngagement is called, Then Case 2 Expected Result is returned`() {
        val componentActivity =
            createActivity(EudiComponentActivity::class.java) as ComponentActivity

        interactor.toggleNfcEngagement(
            componentActivity = componentActivity,
            toggle = false
        )

        verify(walletCorePresentationController, times(1))
            .toggleNfcEngagement(
                componentActivity = componentActivity,
                toggle = false
            )
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
                    emit(TransferEventPartialState.Connecting)
                    emit(
                        TransferEventPartialState.RequestReceived(
                            requestData = emptyList(),
                            verifierName = null,
                            verifierIsTrusted = false
                        )
                    )
                    emit(TransferEventPartialState.ResponseSent)
                    emit(TransferEventPartialState.Redirect(uri = URI("")))
                }
            )
    }

    private fun verifyStartQrEngagementWasCalledExactlyOnce() {
        verify(walletCorePresentationController, times(1))
            .startQrEngagement()
    }
    //endregion

    //region isBleAvailable

    // Case 1:
    // BluetoothAdapter.getDefaultAdapter()?.isEnabled returns true.
    @Test
    fun `Given Case 1, When isBleAvailable is called, Then it returns true`() {
        // Given
        val expectedBluetoothAdapterEnabled = true
        mockBluetoothAdapterEnabledState(enabled = expectedBluetoothAdapterEnabled)

        // When
        val actual = interactor.isBleAvailable()

        // Then
        kotlin.test.assertEquals(expectedBluetoothAdapterEnabled, actual)
    }

    // Case 2:
    // BluetoothAdapter.getDefaultAdapter()?.isEnabled returns false.
    @Test
    fun `Given Case 2, When isBleAvailable is called, Then it returns false`() {
        // Given
        val expectedBluetoothAdapterEnabled = false
        mockBluetoothAdapterEnabledState(enabled = expectedBluetoothAdapterEnabled)

        // When
        val actual = interactor.isBleAvailable()

        // Then
        kotlin.test.assertEquals(expectedBluetoothAdapterEnabled, actual)
    }
    //endregion

    //region isBleCentralClientModeEnabled

    // Case 1:
    // Configuration of Wallet Core has BLE_CLIENT_CENTRAL_MODE for its bleTransferMode.
    @Test
    fun `Given Case 1, When isBleCentralClientModeEnabled is called, Then it returns true`() {
        // Given
        val expectedBleCentralClientModeEnabled = true

        val mockedConfig = getMockedEudiWalletConfig {
            bleTransferMode(EudiWalletConfig.BLE_CLIENT_CENTRAL_MODE)
        }

        whenever(walletCoreConfig.config).thenReturn(mockedConfig)

        // When
        val actual = interactor.isBleCentralClientModeEnabled()

        // Then
        kotlin.test.assertEquals(expectedBleCentralClientModeEnabled, actual)
    }

    // Case 2:
    // Configuration of Wallet Core has BLE_SERVER_PERIPHERAL_MODE for its bleTransferMode.
    @Test
    fun `Given Case 2, When isBleCentralClientModeEnabled is called, Then it returns false`() {
        // Given
        val expectedBleCentralClientModeEnabled = false

        val mockedConfig = getMockedEudiWalletConfig {
            bleTransferMode(EudiWalletConfig.BLE_SERVER_PERIPHERAL_MODE)
        }

        whenever(walletCoreConfig.config).thenReturn(mockedConfig)

        // When
        val actual = interactor.isBleCentralClientModeEnabled()

        // Then
        kotlin.test.assertEquals(expectedBleCentralClientModeEnabled, actual)
    }
    //endregion

    private fun mockBluetoothAdapterEnabledState(enabled: Boolean) {
        val newBluetoothAdapterState = if (enabled) {
            BluetoothAdapter.STATE_ON
        } else {
            BluetoothAdapter.STATE_OFF
        }
        shadowBluetoothAdapter.setState(newBluetoothAdapterState)
    }
}