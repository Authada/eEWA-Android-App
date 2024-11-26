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

package eu.europa.ec.issuancefeature.ui.document.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.commonfeature.ui.document_details.model.Priority
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.textSecondary
import eu.europa.ec.uilogic.component.InfoTextWithNameAndImage
import eu.europa.ec.uilogic.component.InfoTextWithNameAndImageData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValue
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData
import eu.europa.ec.uilogic.component.defaultInfoNameTextStyle
import eu.europa.ec.uilogic.component.defaultInfoValueTextStyle
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM

@Composable
internal fun DetailItem(documentDetailsUi: DocumentDetailsUi) {
    val nameStyle = when (documentDetailsUi.priority) {
        Priority.NORMAL -> defaultInfoNameTextStyle
        Priority.LOW -> defaultInfoNameTextStyle.copy(
            color = MaterialTheme.colorScheme.textSecondary,
            fontSize = 10.sp
        )
    }

    val valueStyle = when (documentDetailsUi.priority) {
        Priority.NORMAL -> defaultInfoValueTextStyle
        Priority.LOW -> defaultInfoValueTextStyle.copy(
            color = MaterialTheme.colorScheme.textSecondary,
            fontSize = 12.sp
        )
    }

    when (documentDetailsUi) {
        is DocumentDetailsUi.DefaultItem -> {
            documentDetailsUi.itemData.infoValues
                ?.toTypedArray()
                ?.let { infoValues ->
                    val itemData = InfoTextWithNameAndValueData.create(
                        title = documentDetailsUi.itemData.title,
                        *infoValues
                    )
                    InfoTextWithNameAndValue(
                        modifier = Modifier.fillMaxWidth(),
                        itemData = itemData,
                        infoNameTextStyle = nameStyle,
                        infoValueTextStyle = valueStyle
                    )
                }
        }

        is DocumentDetailsUi.SignatureItem -> {
            InfoTextWithNameAndImage(
                modifier = Modifier.fillMaxWidth(),
                itemData = documentDetailsUi.itemData,
                infoNameTextStyle = nameStyle,
                contentDescription = stringResource(id = R.string.content_description_user_signature)
            )
        }

        is DocumentDetailsUi.Unknown -> {

        }

        is DocumentDetailsUi.DrivingPrivilegesItem -> {
            DrivingPrivilegesDetailItem(
                title = documentDetailsUi.nameOfTheSection,
                items = documentDetailsUi.itemData
            )
        }
    }
}

@ThemeModePreviews
@Composable
private fun DetailItemPreview() {
    val defaultItemData = InfoTextWithNameAndValueData.create(
        title = "Name",
        "John Smith"
    )
    val signatureItemData = InfoTextWithNameAndImageData(
        title = "Signature",
        base64Image = ""
    )
    val data = listOf(
        DocumentDetailsUi.DefaultItem(
            itemData = defaultItemData
        ),
        DocumentDetailsUi.SignatureItem(
            itemData = signatureItemData
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = defaultItemData,
            priority = Priority.LOW
        ),
        DocumentDetailsUi.Unknown
    )
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp)
        ) {
            data.map {
                DetailItem(it)
            }
        }
    }
}