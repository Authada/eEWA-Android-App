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

package eu.europa.ec.dashboardfeature.ui.dashboard.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.dashboardfeature.DashboardDocumentModel
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData

open class DocumentDashboardUiPreviewParameter : PreviewParameterProvider<DashboardDocumentModel> {
    override val values: Sequence<DashboardDocumentModel> = sequenceOf(
        DRIVERS_LICENSE_NO_METADATA,
        VERIFIED_EMAIL,
        VERIFIED_PHONE,
        GENERIC_EAA
    )


    companion object {

        val DRIVERS_LICENSE_NO_METADATA = DashboardDocumentModel(
            documentId = "1",
            documentName = "Drivers License",
            documentIdentifier = DocumentIdentifier.MDL,
            bottomDetail = InfoTextWithNameAndValueData.create(
                "Owner",
                "Max Mustermann"
            ),
            documentMetaData = null
        )

        val GENERIC_EAA: DashboardDocumentModel = DashboardDocumentModel(
            documentId = "1",
            documentName = "AUTHADA Card",
            documentIdentifier = DocumentIdentifier.OTHER("some namespace", "some doctype"),
            bottomDetail = InfoTextWithNameAndValueData.create(
                "Owner",
                "Max Mustermann"
            ),
            documentMetaData = DocumentMetaData(
                uniqueDocumentId = "1",
                documentName = "AUTHADA Card",
                logo = null,
                backgroundColor = "#BB2929",
                backgroundImage = null,
                textColor = "#FFFFFF"
            )
        )

        val VERIFIED_EMAIL = DashboardDocumentModel(
            documentId = "1",
            documentName = "E-mail address",
            documentIdentifier = DocumentIdentifier.EMAIL,
            bottomDetail = InfoTextWithNameAndValueData.create(
                "",
                "authadauser@authada.de"
            ),
            documentMetaData = DocumentMetaData(
                uniqueDocumentId = "1",
                documentName = "E-mail address",
                logo = null,
                backgroundColor = "#bff6ec",
                backgroundImage = null,
                textColor = "#FFFFFF"
            )
        )

        val VERIFIED_PHONE = DashboardDocumentModel(
            documentId = "1",
            documentName = "Phone number",
            documentIdentifier = DocumentIdentifier.EMAIL,
            bottomDetail = InfoTextWithNameAndValueData.create(
                "",
                "+49123456789"
            ),
            documentMetaData = DocumentMetaData(
                uniqueDocumentId = "1",
                documentName = "Phone number",
                logo = null,
                backgroundColor = "#ffe0bf",
                backgroundImage = null,
                textColor = "#FFFFFF"
            )
        )
    }
}