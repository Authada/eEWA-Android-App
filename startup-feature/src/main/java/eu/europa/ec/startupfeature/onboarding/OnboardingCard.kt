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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.europa.ec.commonfeature.ui.InformationCard
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.InfoRoundIcon
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE

@Composable
internal fun OnboardingCard(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {

    InformationCard(
        modifier = modifier,
        title = stringResource(id = page.titleRes),
        subtitle = stringResource(id = page.subtitleRes),
        infoRoundIcon = {
            InfoRoundIcon(icon = page.icon, fitInsideTheCircle = false)
        }
    )
}


@ThemeModePreviews
@Composable
private fun OnboardingCardPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(SPACING_LARGE.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OnboardingCard(
                modifier = Modifier.fillMaxHeight(0.6f),
                page = OnboardingPage(
                    pageIndex = 0,
                    titleRes = R.string.onboarding_title_0,
                    subtitleRes = R.string.onboarding_subtitle_0,
                    icon = AppIcons.StoreId,
                    primaryButtonTextRes = R.string.onboarding_primary_button_0,
                    secondaryButtonTextRes = R.string.onboarding_secondary_button_0
                )
            )
        }
    }
}