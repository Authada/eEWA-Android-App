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

package eu.europa.ec.issuancefeature.ui.document.details.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import eu.europa.ec.commonfeature.config.IssuanceFlowUiConfig
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.issuancefeature.ui.document.details.State
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction

internal class DocumentDetailsPreviewParameter : PreviewParameterProvider<State> {
    override val values: Sequence<State> = sequenceOf(
        DRIVERS_LICENSE_NO_METADATA,
        VERIFIED_EMAIL,
        VERIFIED_PHONE,
        GENERIC_EAA,
        EXPIRED
    )


    companion object {
        private val documentDetails = listOf(
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData.create(
                    title = "key",
                    infoValues = arrayOf("value 1", "value 2")
                )
            )
        )

        val DRIVERS_LICENSE_NO_METADATA = State(
            detailsType = IssuanceFlowUiConfig.EXTRA_DOCUMENT,
            navigatableAction = ScreenNavigateAction.CANCELABLE,
            hasCustomTopBar = true,
            detailsHaveBottomGradient = false,
            document = DocumentUi(
                documentId = "1",
                documentName = "Drivers License",
                documentIdentifier = DocumentIdentifier.MDL,
                documentExpirationDateFormatted = "30 Mar 2050",
                documentHasExpired = false,
                base64Image = "",
                documentIssuer = "Bundesrepublik Deutschland",
                documentDetails = documentDetails,
                highlightedFields = listOf(
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Name",
                            infoValues = arrayOf("Vorname Nachname")
                        )
                    ),
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Valid until",
                            infoValues = arrayOf("25.10.2026")
                        )
                    )
                )
            )
        )

        val GENERIC_EAA: State = State(
            detailsType = IssuanceFlowUiConfig.EXTRA_DOCUMENT,
            navigatableAction = ScreenNavigateAction.CANCELABLE,
            hasCustomTopBar = true,
            detailsHaveBottomGradient = false,
            document = DocumentUi(
                documentId = "1",
                documentName = "AUTHADA Card",
                documentIdentifier = DocumentIdentifier.OTHER("some namespace", "some doctype"),
                documentExpirationDateFormatted = "30 Mar 2050",
                documentHasExpired = false,
                base64Image = "image3",
                documentIssuer = "Supermarket GmbH",
                documentDetails = documentDetails,
                highlightedFields = listOf(
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Date of issue",
                            infoValues = arrayOf("20.10.1995")
                        )
                    ),
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Valid until",
                            infoValues = arrayOf("25.10.2026")
                        )
                    )
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
        )

        val VERIFIED_EMAIL: State = State(
            detailsType = IssuanceFlowUiConfig.EXTRA_DOCUMENT,
            navigatableAction = ScreenNavigateAction.CANCELABLE,
            hasCustomTopBar = true,
            detailsHaveBottomGradient = false,
            document = DocumentUi(
                documentId = "1",
                documentName = "E-mail address",
                documentIdentifier = DocumentIdentifier.EMAIL,
                documentExpirationDateFormatted = "30 Mar 2050",
                documentHasExpired = false,
                base64Image = "image3",
                documentIssuer = "Bundesrepublik Deutschland",
                documentDetails = documentDetails,
                highlightedFields = listOf(
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Email address",
                            infoValues = arrayOf("max.mustermann@gmail.com")
                        )
                    ),
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Date of issue",
                            infoValues = arrayOf("20.10.1995")
                        )
                    ),
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Valid until",
                            infoValues = arrayOf("25.10.2026")
                        )
                    )
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
        )

        val VERIFIED_PHONE: State = State(
            detailsType = IssuanceFlowUiConfig.EXTRA_DOCUMENT,
            navigatableAction = ScreenNavigateAction.CANCELABLE,
            hasCustomTopBar = true,
            detailsHaveBottomGradient = false,
            document = DocumentUi(
                documentId = "1",
                documentName = "Phone number",
                documentIdentifier = DocumentIdentifier.EMAIL,
                documentExpirationDateFormatted = "30 Mar 2050",
                documentHasExpired = false,
                base64Image = "image3",
                documentIssuer = "Bundesrepublik Deutschland",
                documentDetails = documentDetails,
                highlightedFields = listOf(
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Phone number",
                            infoValues = arrayOf("+49 120 12345678")
                        )
                    ),
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Date of issue",
                            infoValues = arrayOf("20.10.1995")
                        )
                    ),
                    DocumentDetailsUi.DefaultItem(
                        itemData = InfoTextWithNameAndValueData.create(
                            title = "Valid until",
                            infoValues = arrayOf("25.10.2026")
                        )
                    )
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
        )

        val EXPIRED: State = GENERIC_EAA.copy(
            document = GENERIC_EAA.document!!.copy(
                documentHasExpired = true
            )
        )
    }
}