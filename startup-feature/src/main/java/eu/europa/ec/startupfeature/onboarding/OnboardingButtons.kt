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

package eu.europa.ec.startupfeature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.uilogic.component.wrap.WrapPrimaryButton
import eu.europa.ec.uilogic.component.wrap.WrapSecondaryButton

@Composable
internal fun OnboardingButtons(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
    onEventSend: (Event) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)) {
        page.secondaryButtonTextRes?.run {
            WrapSecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEventSend(Event.OnSecondaryButtonPressed) }
            ) {
                Text(text = stringResource(id = page.secondaryButtonTextRes))
            }
        }

        WrapPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onEventSend(Event.OnPrimaryButtonPressed(page.pageIndex))
            }
        ) {
            Text(text = stringResource(id = page.primaryButtonTextRes))
        }


    }
}

@ThemeModePreviews
@Composable
private fun OnboardingButtonsPreview() {
    PreviewTheme {
        OnboardingButtons(
            modifier = Modifier,
            page = OnboardingPage(
                pageIndex = 0,
                titleRes = R.string.onboarding_title_0,
                subtitleRes = R.string.onboarding_subtitle_0,
                icon = AppIcons.StoreId,
                primaryButtonTextRes = R.string.onboarding_primary_button_0,
                secondaryButtonTextRes = R.string.onboarding_secondary_button_0
            ),
            onEventSend = {

            }
        )
    }
}