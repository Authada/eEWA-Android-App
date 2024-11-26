/*
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

package eu.europa.ec.proximityfeature.ui.qr

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class ProximityQrPreviewParam : PreviewParameterProvider<State> {
    override val values: Sequence<State>
        get() = sequenceOf(
            STATE_NORMAL,
            STATE_NO_PERMISSIONS,
            STATE_NO_BLUETOOTH
        )

    private companion object {
        val STATE_NO_PERMISSIONS = State(
            bottomSheetState = BottomSheetState(isOpen = true, BottomSheetType.BLUETOOTH_PERMISSIONS),
            isLoading = false,
            error = null,
            qrCode = "some qr code",
            bleState = BleState(
                bleEnabled = true,
                blePermissionsState = BlePermissionsState.NEED_TO_CHECK
            )
        )

        val STATE_NO_BLUETOOTH = State(
            bottomSheetState = BottomSheetState(isOpen = true, BottomSheetType.BLUETOOTH_ENABLING),
            isLoading = false,
            error = null,
            qrCode = "some qr code",
            bleState = BleState(
                bleEnabled = false,
                blePermissionsState = BlePermissionsState.GRANTED
            )
        )

        val STATE_NORMAL = State(
            isLoading = false,
            error = null,
            qrCode = "some qr code",
            bleState = BleState(
                bleEnabled = true,
                blePermissionsState = BlePermissionsState.GRANTED
            )
        )
    }
}
