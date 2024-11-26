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

package eu.europa.ec.issuancefeature.ui.document.preparation.typeselection


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.resourceslogic.theme.values.textSecondary
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.ScalableText
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.utils.VSpacer
import eu.europa.ec.uilogic.component.wrap.WrapCard
import eu.europa.ec.uilogic.component.wrap.WrapImage

@Composable
internal fun TypeItem(
    option: DocumentOptionItemUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    WrapCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            WrapImage(
                option.icon.copy(
                    tint = if(option.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textSecondary
                ), modifier = Modifier.size(64.dp)
            )

            VSpacer.Small()

            ScalableText(
                text = option.text,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = if(option.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.textSecondary
                )
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun TypeItemPreview() {
    PreviewTheme {
        val option = DocumentOptionItemUi(
            text = "Drivers License",
            icon = AppIcons.DriversLicense,
            type = DocumentIdentifier.MDL,
            available = true
        )

        TypeItem(option = option, modifier = Modifier.size(300.dp), onClick = {})
    }
}