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

package eu.europa.ec.commonfeature.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.eudi.wallet.document.Document.Companion.PROXY_ID_PREFIX
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.resourceslogic.theme.values.green100
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.IconData

data class DocumentUi(
    val documentId: String,
    val documentName: String,
    val documentIssuer: String,
    val documentIdentifier: DocumentIdentifier,
    val documentExpirationDateFormatted: String,
    val documentHasExpired: Boolean,
    val base64Image: String,
    val highlightedFields: List<DocumentDetailsUi>,
    val documentDetails: List<DocumentDetailsUi>,
    val description: String? = null,
    val userFullName: String? = null,
    val showTooltipOnCard: Boolean = false,
    val documentMetaData: DocumentMetaData? = null
)

fun DocumentIdentifier.toUiName(resourceProvider: ResourceProvider): String {
    return when (this) {
        is DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> resourceProvider.getString(R.string.dashboard_document_pid_title)
        is DocumentIdentifier.MDL -> resourceProvider.getString(R.string.mdl)
        is DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> resourceProvider.getString(R.string.email)
        is DocumentIdentifier.SAMPLE -> resourceProvider.getString(R.string.load_sample_data)
        is DocumentIdentifier.OTHER -> docType
        DocumentIdentifier.PID_ISSUING -> throw IllegalArgumentException("document type ${DocumentIdentifier.PID_ISSUING.docType} only used for secure element provisioning")
    }
}

fun DocumentIdentifier.toUiDescription(resourceProvider: ResourceProvider): String? {
    return when (this) {
        DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> resourceProvider.getString(R.string.email_description)
        else -> null
    }
}


fun DocumentIdentifier.toIcon(): IconData {
    return when (this) {
        DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> AppIcons.Id
        DocumentIdentifier.MDL -> AppIcons.DriversLicense
        DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> AppIcons.Email
        DocumentIdentifier.SAMPLE -> AppIcons.OtherId
        is DocumentIdentifier.OTHER -> AppIcons.OtherId
        DocumentIdentifier.PID_ISSUING -> throw IllegalArgumentException("document type ${DocumentIdentifier.PID_ISSUING.docType} only used for secure element provisioning")
    }
}

@Composable
fun DocumentIdentifier.toCardBackgroundColor(): Color {
    return when (this) {
        DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> green100
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
}

val DocumentUi.isProxy
    get() = documentId.startsWith(PROXY_ID_PREFIX)
