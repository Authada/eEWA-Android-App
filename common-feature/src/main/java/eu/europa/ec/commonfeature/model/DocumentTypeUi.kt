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

import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.corelogic.model.DocumentType
import eu.europa.ec.eudi.wallet.document.Document.Companion.PROXY_ID_PREFIX
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.IconData

data class DocumentUi(
    val documentId: String,
    val documentName: String,
    val documentType: DocumentType,
    val documentExpirationDateFormatted: String,
    val documentHasExpired: Boolean,
    val documentImage: String,
    val documentDetails: List<DocumentDetailsUi>,
    val userFullName: String? = null,
    val showTooltipOnCard: Boolean = false
)

fun DocumentType.toUiName(resourceProvider: ResourceProvider): String {
    return when (this) {
        DocumentType.PID -> resourceProvider.getString(R.string.pid)
        DocumentType.MDL -> resourceProvider.getString(R.string.mdl)
        DocumentType.SAMPLE_DOCUMENTS -> resourceProvider.getString(R.string.load_sample_data)
        DocumentType.OTHER -> ""
        DocumentType.PID_ISSUING -> throw IllegalArgumentException("document type ${DocumentType.PID_ISSUING.docType} only used for secure element provisioning")
    }
}


fun DocumentType.toIcon(): IconData {
    return when (this) {
        DocumentType.PID -> AppIcons.Id
        DocumentType.MDL -> AppIcons.DriversLicense
        DocumentType.SAMPLE_DOCUMENTS -> AppIcons.OtherId
        DocumentType.OTHER -> AppIcons.OtherId
        DocumentType.PID_ISSUING -> throw IllegalArgumentException("document type ${DocumentType.PID_ISSUING.docType} only used for secure element provisioning")
    }
}

val DocumentUi.isProxy
    get() = documentId.startsWith(PROXY_ID_PREFIX)
