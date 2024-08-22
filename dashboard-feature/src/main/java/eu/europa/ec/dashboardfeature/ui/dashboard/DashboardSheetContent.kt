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

package eu.europa.ec.dashboardfeature.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.allCorneredShapeSmall
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.resourceslogic.theme.values.textSecondary
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.IconData
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.HSpacer
import eu.europa.ec.uilogic.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.DialogBottomSheet
import eu.europa.ec.uilogic.component.wrap.SheetContent
import eu.europa.ec.uilogic.component.wrap.WrapIcon
import eu.europa.ec.uilogic.component.wrap.WrapIconButton
import eu.europa.ec.uilogic.extension.throttledClickable

@Composable
internal fun DashboardSheetContent(
    sheetContent: DashboardBottomSheetContent,
    state: State,
    onEventSent: (event: Event) -> Unit
) {
    when (sheetContent) {
        is DashboardBottomSheetContent.OPTIONS -> {
            SheetContent(
                titleContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.dashboard_bottom_sheet_options_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.textPrimary
                        )
                        WrapIconButton(
                            iconData = AppIcons.Close,
                            customTint = MaterialTheme.colorScheme.primary,
                            onClick = { onEventSent(Event.BottomSheet.Close) }
                        )
                    }
                },
                bodyContent = {
                    BottomSheetOption(
                        text = stringResource(id = R.string.dashboard_bottom_sheet_options_action_1),
                        iconData = AppIcons.Edit,
                        onClick = {
                            onEventSent(Event.BottomSheet.Options.OpenChangeQuickPin)
                        })
                    VSpacer.Medium()

                    BottomSheetOption(
                        text = stringResource(id = R.string.dashboard_bottom_sheet_options_action_3),
                        iconData = AppIcons.OpenInBrowser.copy(
                            tint = MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            onEventSent(Event.BottomSheet.Options.OpenVerifierWebsite)
                        })


                    VSpacer.Medium()

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = state.appVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            )
        }

        is DashboardBottomSheetContent.BLUETOOTH -> {
            DialogBottomSheet(
                title = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_title),
                message = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_subtitle),
                positiveButtonText = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_primary_button_text),
                negativeButtonText = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_secondary_button_text),
                onPositiveClick = {
                    onEventSent(
                        Event.BottomSheet.Bluetooth.PrimaryButtonPressed(
                            sheetContent.availability
                        )
                    )
                },
                onNegativeClick = { onEventSent(Event.BottomSheet.Bluetooth.SecondaryButtonPressed) }
            )
        }
    }
}

@Composable
private fun BottomSheetOption(text: String, iconData: IconData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.allCorneredShapeSmall)
            .throttledClickable(
                onClick = onClick
            )
            .padding(
                vertical = SPACING_SMALL.dp,
                horizontal = SPACING_EXTRA_SMALL.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        WrapIcon(
            iconData = iconData,
            customTint = MaterialTheme.colorScheme.primary
        )
        HSpacer.Medium()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.textPrimary
        )
    }
}

@ThemeModePreviews
@Composable
private fun SheetContentPreview() {
    PreviewTheme {
        DashboardSheetContent(
            sheetContent = DashboardBottomSheetContent.OPTIONS,
            state = State(
                appVersion = "1.0.0"
            ),
            onEventSent = {}
        )
    }
}