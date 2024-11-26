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

package eu.europa.ec.issuancefeature.ui.document.preparation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.ActionTopBar
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.content.InfoContent
import eu.europa.ec.uilogic.component.content.InfoRoundIcon
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.component.content.ToolbarAction
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.WrapPrimaryButton
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import eu.europa.ec.issuancefeature.ui.document.preparation.AddDocumentInfoViewModel.*


@Composable
fun AddDocumentInfoScreen(
    navController: NavController,
    viewModel: AddDocumentInfoViewModel
) {
    val context = LocalContext.current

    ContentScreen(
        navigatableAction = ScreenNavigateAction.NONE,
        isLoading = false,
        topBar = {
            ActionTopBar(
                contentColor = MaterialTheme.colorScheme.background,
                iconColor = MaterialTheme.colorScheme.primary,
                iconData = AppIcons.Close,
                toolbarActions = listOf(
                    ToolbarAction(
                        icon = AppIcons.Help.copy(
                            tint = MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            viewModel.setEvent(Event.ShowHelp)
                        }
                    )
                ),
                onClick = {
                    viewModel.setEvent(Event.Pop)
                })
        },
        onBack = {
            viewModel.setEvent(Event.Pop)
        }
    ) { paddingValues ->
        Content(
            onEventSend = { viewModel.setEvent(it) },
            paddingValues = paddingValues
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> {
                    when (effect) {
                        is Effect.Navigation.Pop -> navController.popBackStack()
                        is Effect.Navigation.SwitchScreen -> {
                            navController.navigate(effect.screenRoute) {
                                popUpTo(IssuanceScreens.AddDocument.screenRoute)
                            }
                        }
                    }
                }

                is Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.collect()
    }
}

@Composable
private fun Content(
    onEventSend: (Event) -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        InfoContent(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.issuance_add_document_info_title),
            subtitle = stringResource(id = R.string.issuance_add_document_info_subtitle),
            infoRoundIcon = {
                InfoRoundIcon(icon = AppIcons.StoreId, fitInsideTheCircle = false)
            }
        )

        VSpacer.Large()

        StickyBottomSection(
            onEventSend = onEventSend
        )
    }
}

@Composable
private fun StickyBottomSection(onEventSend: (Event) -> Unit) {
    Column {
        WrapPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onEventSend(Event.OnNextPressed) }
        ) {
            Text(text = stringResource(id = R.string.issuance_add_document_info_primary_button_text))
        }
    }
}

@ThemeModePreviews
@Composable
private fun AddDocumentInfoScreenPreview() {
    PreviewTheme {
        AddDocumentInfoScreen(
            navController = rememberNavController(),
            viewModel = viewModel()
        )
    }
}

