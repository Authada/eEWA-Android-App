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

package eu.europa.ec.dashboardfeature.ui.proxy

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.WalletTheme
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.resourceslogic.theme.values.textSecondary
import eu.europa.ec.uilogic.component.ActionTopBarOnlyRightIcon
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.content.InfoRoundIcon
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.component.utils.NOT_IMPLEMENTED_MESSAGE
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.WrapPrimaryButton

@Composable
internal fun ProxyPidExplanationScreen(
    navController: NavController
) {
    val context = LocalContext.current

    ContentScreen(
        isLoading = false,
        onBack = { },
        navigatableAction = ScreenNavigateAction.NONE,
        topBar = {
            ActionTopBarOnlyRightIcon(
                contentColor = MaterialTheme.colorScheme.background,
                iconData = AppIcons.Help.copy(
                    tint = MaterialTheme.colorScheme.primary
                ),
                onClick = {
                    Toast.makeText(context, NOT_IMPLEMENTED_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            )
        },
    ) { paddingValues ->
        ScreenContent(
            onCloseClick = {
                navController.popBackStack()
            },
            onMoreInfoClick = {
                Toast.makeText(context, NOT_IMPLEMENTED_MESSAGE, Toast.LENGTH_SHORT).show()
            },
            paddingValues = paddingValues
        )
    }
}


@Composable
private fun ScreenContent(
    onCloseClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SPACING_LARGE.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.proxy_pid_explanation_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.textPrimary
                ),
                textAlign = TextAlign.Center
            )
            InfoRoundIcon(icon = AppIcons.ProxyIdentityIcon, fitInsideTheCircle = false)

            InstructionsColumn(
                modifier = Modifier
                    .padding(horizontal = SPACING_LARGE.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
            )
        }

        VSpacer.Medium()
        StickyBottomColumn(
            onCloseClick = onCloseClick, onMoreInfoClick = onMoreInfoClick,
        )
    }
}

@Composable
private fun InstructionsColumn(modifier: Modifier = Modifier) {
    val instructions =
        stringArrayResource(id = R.array.proxy_pid_explanation_instructions)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)) {
        Text(
            text = stringResource(id = R.string.proxy_pid_explanation_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.textPrimary
            )
        )

        Text(
            text = stringResource(id = R.string.proxy_pid_explanation_instructions_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.textSecondary
            )
        )



        instructions.forEachIndexed { index, instruction ->
            Row {
                Text(
                    text = "${index + 1}. ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.textPrimary
                    )
                )
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.textPrimary
                    )
                )
            }

        }

    }
}

@Composable
private fun StickyBottomColumn(
    onCloseClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.clickable {
                onMoreInfoClick()
            },
            text = stringResource(id = R.string.proxy_pid_explanation_more_info),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary
            ),
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center
        )
        WrapPrimaryButton(
            onClick = onCloseClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(id = R.string.generic_close_capitalized),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewProxyPidExplanationScreen(modifier: Modifier = Modifier) {
    WalletTheme {
        ProxyPidExplanationScreen(navController = rememberNavController())
    }
}
