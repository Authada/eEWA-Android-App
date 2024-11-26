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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.europa.ec.proximityfeature.ui.qr.Event
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.wrap.DialogBottomSheet

@Composable
internal fun ProximityQrBluetoothBottomSheet(
    onEventSent: (event: Event) -> Unit
) {

    DialogBottomSheet(
        title = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_title),
        message = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_subtitle),
        positiveButtonText = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_primary_button_text),
        negativeButtonText = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_secondary_button_text),
        onPositiveClick = {
            onEventSent(
                Event.BottomSheet.OnEnableBluetoothClick
            )
        },
        onNegativeClick = { onEventSent(Event.BottomSheet.OnCancelled) }
    )
}


@ThemeModePreviews
@Composable
private fun ProximityQrBluetoothBottomSheetPreview() {
    PreviewTheme {
        ProximityQrBluetoothBottomSheet(
            onEventSent = {}
        )
    }
}