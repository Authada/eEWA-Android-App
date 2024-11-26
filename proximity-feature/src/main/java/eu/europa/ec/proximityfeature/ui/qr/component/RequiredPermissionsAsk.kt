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
 * Modified by AUTHADA GmbH November 2024
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

package eu.europa.ec.proximityfeature.ui.qr.component

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import eu.europa.ec.proximityfeature.ui.qr.BlePermissionsState
import eu.europa.ec.proximityfeature.ui.qr.BleState
import eu.europa.ec.proximityfeature.ui.qr.Event

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RequiredPermissionsAsk(
    bleState: BleState,
    onEventSend: (event: Event) -> Unit
) {
    var performPermissionCheck by remember {
        mutableStateOf(true)
    }
    val permissions = getRequiredPermissions(bleState.isBleCentralClientModeEnabled)

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions) { _ ->
        performPermissionCheck = true
    }

    if (performPermissionCheck) {
        val areAllGranted = permissionsState.allPermissionsGranted

        when {
            areAllGranted -> {
                onEventSend(Event.OnPermissionStateChanged(BlePermissionsState.GRANTED))
            }
            !areAllGranted && permissionsState.shouldShowRationale -> {
                onEventSend(Event.OnShowPermissionsRational)
            }
            else -> {
                LaunchedEffect(Unit) {
                    permissionsState.launchMultiplePermissionRequest()
                }
                performPermissionCheck = false
            }
        }
    }
}

private fun getRequiredPermissions(isBleCentralClientModeEnabled: Boolean): List<String> {
    val permissions: MutableList<String> = mutableListOf()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && isBleCentralClientModeEnabled) {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    return permissions
}
