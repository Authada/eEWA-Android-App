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

package eu.europa.ec.commonfeature.util

import androidx.annotation.VisibleForTesting
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.commonfeature.ui.request.Event
import eu.europa.ec.commonfeature.ui.request.model.DocumentItemDomainPayload
import eu.europa.ec.commonfeature.ui.request.model.DocumentItemUi
import eu.europa.ec.commonfeature.ui.request.model.OptionalFieldItemUi
import eu.europa.ec.commonfeature.ui.request.model.RequestDataUi
import eu.europa.ec.commonfeature.ui.request.model.RequestDocumentItemUi
import eu.europa.ec.commonfeature.ui.request.model.RequiredFieldsItemUi
import eu.europa.ec.corelogic.model.DocType
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.eudi.iso18013.transfer.DocItem
import eu.europa.ec.eudi.iso18013.transfer.DocRequest
import eu.europa.ec.eudi.iso18013.transfer.ReaderAuth
import eu.europa.ec.eudi.iso18013.transfer.RequestDocument
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.DrivingPrivilegesData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndImageData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
object TestsData {

    data class TestFieldUi(
        val elementIdentifier: String,
        val value: String,
        val isAvailable: Boolean = true,
    )

    data class TestTransformedRequestDataUi(
        val documentId: String,
        val documentIdentifierUi: DocumentIdentifier,
        val documentTitle: String,
        val optionalFields: List<TestFieldUi>,
        val requiredFields: List<TestFieldUi>
    )

    val NotSupportedDocumentIdentifierException =
        RuntimeException("Currently not supported Document Type")

    const val mockedPidDocName = "EU PID"
    const val mockedMdlDocName = "mDL"
    const val mockedPidId = "000001"
    const val mockedMdlId = "000002"
    const val mockedEmailId = "000003"
    const val mockedUserBase64Portrait = "SE"
    const val mockedDocUiNamePid = "ID Card"
    const val mockedDocUiNameMdl = "Driver's License"
    const val mockedNoExpirationDateFound = "-"
    const val mockedFormattedExpirationDate = "30 Mar 2050"
    const val mockedDocumentHasExpired = false
    const val mockedUserAuthentication = false
    const val mockedVerifierName = "EUDIW Verifier"
    const val mockedRequestRequiredFieldsTitle = "Verification Data"
    const val mockedRequestElementIdentifierNotAvailable = "Not available"

    const val mockedPidDocType = "eu.europa.ec.eudi.pid.1"
    const val mockedPidSdjwtDocType = "https://metadata-8c062a.usercontent.opencode.de/pid.json"
    const val mockedPidNameSpace = "eu.europa.ec.eudi.pid.1"
    const val mockedMdlDocType = "org.iso.18013.5.1.mDL"
    const val mockedMdlNameSpace = "org.iso.18013.5.1"
    const val mockedEmailDocType = "eu.europa.ec.eudi.email.1"
    const val mockedEmailNameSpace = "eu.europa.ec.eudi.email.1"
    const val mockedAgeVerificationDocType = "eu.europa.ec.eudiw.pseudonym.age_over_18.1"
    const val mockedAgeVerificationNameSpace = "eu.europa.ec.eudiw.pseudonym.age_over_18.1"

    val mockedValidReaderAuth = ReaderAuth(
        readerAuth = byteArrayOf(),
        readerSignIsValid = true,
        readerCertificateChain = listOf(),
        readerCertificatedIsTrusted = true,
        readerCommonName = mockedVerifierName
    )

    val mockedPidWithBasicFieldsDocRequest = DocRequest(
        docType = mockedPidDocType,
        requestItems = listOf(
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "family_name"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "given_name"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "age_over_18"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "age_over_65"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "age_birth_year"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "birth_city"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "gender"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "expiry_date"
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "portrait",
            ),
            DocItem(
                namespace = mockedPidNameSpace,
                elementIdentifier = "issuing_country",
            ),
        ),
        readerAuth = mockedValidReaderAuth
    )

    val mockedVerifiedEmailWithBasicFieldsDocRequest = DocRequest(
        docType = mockedEmailDocType,
        requestItems = emptyList(),
        readerAuth = mockedValidReaderAuth
    )
    val mockedMdlWithBasicFieldsDocRequest = DocRequest(
        docType = mockedMdlDocType,
        requestItems = listOf(
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "family_name"
            ),
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "given_name"
            ),
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "birth_place"
            ),
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "expiry_date"
            ),
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "portrait"
            ),
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "driving_privileges"
            ),
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "signature_usual_mark"
            ),
            DocItem(
                namespace = mockedMdlNameSpace,
                elementIdentifier = "sex"
            )
        ),
        readerAuth = mockedValidReaderAuth
    )

    val mockedAgeVerificationWithBasicFieldsDocRequest = DocRequest(
        docType = mockedAgeVerificationDocType,
        requestItems = listOf(
            DocItem(
                namespace = mockedAgeVerificationNameSpace,
                elementIdentifier = "age_over_18"
            ),
            DocItem(
                namespace = mockedAgeVerificationNameSpace,
                elementIdentifier = "expiry_date"
            ),
            DocItem(
                namespace = mockedAgeVerificationNameSpace,
                elementIdentifier = "issuing_country",
            )
        ),
        readerAuth = mockedValidReaderAuth
    )

    val mockedValidPidWithBasicFieldsRequestDocument = RequestDocument(
        documentId = mockedPidId,
        docType = mockedPidDocType,
        docName = mockedPidDocName,
        userAuthentication = mockedUserAuthentication,
        docRequest = mockedPidWithBasicFieldsDocRequest
    )

    val mockedValidMdlWithBasicFieldsRequestDocument = RequestDocument(
        documentId = mockedMdlId,
        docType = mockedMdlDocType,
        docName = mockedMdlDocName,
        userAuthentication = mockedUserAuthentication,
        docRequest = mockedMdlWithBasicFieldsDocRequest
    )

    val mockedImage = DocumentMetaData.Image(
        url = "https://someUrl.png",
        contentDescription = "image description"
    )

    val mockedFullPidUi = DocumentUi(
        documentId = mockedPidId,
        documentName = mockedDocUiNamePid,
        documentIdentifier = DocumentIdentifier.PID_MDOC,
        documentExpirationDateFormatted = mockedFormattedExpirationDate,
        documentHasExpired = mockedDocumentHasExpired,
        base64Image = "",
        documentIssuer = "",
        documentDetails = emptyList(),
        highlightedFields = emptyList(),
        userFullName = "JAN ANDERSSON",
        documentMetaData = DocumentMetaData(
            uniqueDocumentId = mockedPidId,
            documentName = mockedDocUiNamePid,
            logo = mockedImage,
            backgroundColor = "#FFFFFF",
            backgroundImage = mockedImage,
            textColor = "#FFFFFF"
        )
    )

    val mockedBasicPidUi = mockedFullPidUi.copy(
        documentDetails = listOf(
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "family_name",
                        infoValues = arrayOf("ANDERSSON")
                    )
            ),
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "given_name",
                        infoValues = arrayOf("JAN")
                    )
            ),

            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "age_over_18",
                        infoValues = arrayOf("yes")
                    )
            ),
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "age_over_65",
                        infoValues = arrayOf("no")
                    )
            ),
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "age_birth_year",
                        infoValues = arrayOf("1985")
                    )
            ),

            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "birth_city",
                        infoValues = arrayOf("KATRINEHOLM")
                    )
            ),
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "gender",
                        infoValues = arrayOf("male")
                    )
            ),


            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData
                    .create(
                        title = "expiry_date",
                        infoValues = arrayOf("30 Mar 2050")
                    )
            ),


            ),
        userFullName = "JAN ANDERSSON"
    )

    val mockedFullMdlUi = DocumentUi(
        documentId = mockedMdlId,
        documentName = mockedDocUiNameMdl,
        documentIdentifier = DocumentIdentifier.MDL,
        documentExpirationDateFormatted = mockedFormattedExpirationDate,
        documentHasExpired = mockedDocumentHasExpired,
        base64Image = "",
        documentIssuer = "",
        documentDetails = emptyList(),
        highlightedFields = emptyList(),
        userFullName = "JAN ANDERSSON",
        documentMetaData = DocumentMetaData(
            uniqueDocumentId = mockedMdlId,
            documentName = mockedDocUiNameMdl,
            logo = mockedImage,
            backgroundColor = "#FFFFFF",
            backgroundImage = mockedImage,
            textColor = "#FFFFFF"
        )
    )


    private val mockedMdlDetails = listOf(
        DocumentDetailsUi.DrivingPrivilegesItem(
            nameOfTheSection = "Vehicle categories",
            itemData = listOf(
                DrivingPrivilegesData(
                    vehicleCategoryCode = "A",
                    icon = AppIcons.DrivingCategory.TwoWheeler,
                    values = listOf(mapOf("null" to "2050-03-30"))
                ), DrivingPrivilegesData(
                    vehicleCategoryCode = "B",
                    icon = AppIcons.DrivingCategory.Car,
                    values = listOf(mapOf("null" to "2050-03-30"))
                )
            )
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "expiry_date",
                    infoValues = arrayOf("30 Mar 2050")
                )
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "sex",
                    infoValues = arrayOf("male")
                )
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "birth_place",
                    infoValues = arrayOf("SWEDEN")
                )
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "given_name",
                    infoValues = arrayOf("JAN")
                )
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "portrait",
                    infoValues = arrayOf("Shown above")
                )
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "family_name",
                    infoValues = arrayOf("ANDERSSON")
                )
        ),
        DocumentDetailsUi.SignatureItem(
            itemData = InfoTextWithNameAndImageData(
                title = "signature_usual_mark",
                base64Image = "SE"
            )
        ),
    )


    private val mockedMdlHighlights = listOf(
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "Name",
                    infoValues = arrayOf("JAN ANDERSSON")
                )
        ),
        DocumentDetailsUi.DefaultItem(
            itemData = InfoTextWithNameAndValueData
                .create(
                    title = "expiry_date",
                    infoValues = arrayOf("30 Mar 2050")
                )
        ),
    )

    val mockedBasicMdlUi = mockedFullMdlUi.copy(
        documentDetails = mockedMdlDetails,
        userFullName = "JAN ANDERSSON",
        highlightedFields = mockedMdlHighlights
    )

    val mockedMdlUiWithNoExpirationDate: DocumentUi = mockedFullMdlUi.copy(
        documentExpirationDateFormatted = mockedNoExpirationDateFound
    )

    val mockedFullDocumentsUi: List<DocumentUi> = listOf(
        mockedFullPidUi, mockedFullMdlUi
    )

    val mockedOptionalFieldsForPidWithBasicFields = listOf(
        TestFieldUi(
            elementIdentifier = "family_name",
            value = "ANDERSSON",
        ),
        TestFieldUi(
            elementIdentifier = "given_name",
            value = "JAN",
        ),
        TestFieldUi(
            elementIdentifier = "age_over_18",
            value = "yes",
        ),
        TestFieldUi(
            elementIdentifier = "age_over_65",
            value = "no",
        ),
        TestFieldUi(
            elementIdentifier = "age_birth_year",
            value = "1985",
        ),
        TestFieldUi(
            elementIdentifier = "birth_city",
            value = "KATRINEHOLM",
        ),
        TestFieldUi(
            elementIdentifier = "gender",
            value = "male",
        ),
    )

    val mockedRequiredFieldsForPidWithBasicFields = listOf(
        TestFieldUi(
            elementIdentifier = "expiry_date",
            value = mockedFormattedExpirationDate,
            isAvailable = true
        ),
        TestFieldUi(
            elementIdentifier = "portrait",
            value = mockedRequestElementIdentifierNotAvailable,
            isAvailable = false
        ),
        TestFieldUi(
            elementIdentifier = "issuing_country",
            value = mockedRequestElementIdentifierNotAvailable,
            isAvailable = false
        ),
    )

    val mockedOptionalFieldsForMdlWithBasicFields = listOf(
        TestFieldUi(
            elementIdentifier = "family_name",
            value = "ANDERSSON",
        ),
        TestFieldUi(
            elementIdentifier = "given_name",
            value = "JAN",
        ),
        TestFieldUi(
            elementIdentifier = "birth_place",
            value = "SWEDEN",
        ),
        TestFieldUi(
            elementIdentifier = "expiry_date",
            value = mockedFormattedExpirationDate,
        ),
        TestFieldUi(
            elementIdentifier = "portrait",
            value = "SE",
        ),
        TestFieldUi(
            elementIdentifier = "driving_privileges",
            value = "issue_date: 1 Jul 2010\n" +
                    "expiry_date: 30 Mar 2050\n" +
                    "vehicle_category_code: A\n" +
                    "issue_date: 19 May 2008\n" +
                    "expiry_date: 30 Mar 2050\n" +
                    "vehicle_category_code: B",
        ),
        TestFieldUi(
            elementIdentifier = "signature_usual_mark",
            value = "SE",
        ),
        TestFieldUi(
            elementIdentifier = "sex",
            value = "male",
        ),
    )

    val mockedTransformedRequestDataUiForPidWithBasicFields = TestTransformedRequestDataUi(
        documentId = mockedPidId,
        documentIdentifierUi = DocumentIdentifier.PID_SDJWT,
        documentTitle = mockedDocUiNamePid,
        optionalFields = mockedOptionalFieldsForPidWithBasicFields,
        requiredFields = mockedRequiredFieldsForPidWithBasicFields
    )

    fun createTransformedRequestDataUi(
        items: List<TestTransformedRequestDataUi>
    ): List<RequestDataUi<Event>> {
        val resultList = mutableListOf<RequestDataUi<Event>>()

        items.forEachIndexed { itemsIndex, transformedRequestDataUi ->
            resultList.add(
                RequestDataUi.Document(
                    documentItemUi = DocumentItemUi(
                        title = transformedRequestDataUi.documentTitle
                    ),
                    isProxy = false
                )
            )
            resultList.add(RequestDataUi.Space())

            transformedRequestDataUi.optionalFields.forEachIndexed { index, testFieldUi ->
                val optionalField = when (transformedRequestDataUi.documentIdentifierUi) {
                    is DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> mockCreateOptionalFieldForPid(
                        docId = transformedRequestDataUi.documentId,
                        elementIdentifier = testFieldUi.elementIdentifier,
                        value = testFieldUi.value
                    )

                    is DocumentIdentifier.MDL -> mockCreateOptionalFieldForMdl(
                        docId = transformedRequestDataUi.documentId,
                        elementIdentifier = testFieldUi.elementIdentifier,
                        value = testFieldUi.value,
                    )

                    is DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> mockCreateOptionalFieldForVerifiedEmail(
                        docId = transformedRequestDataUi.documentId,
                        elementIdentifier = testFieldUi.elementIdentifier,
                        value = testFieldUi.value,
                    )

                    is DocumentIdentifier.SAMPLE, is DocumentIdentifier.OTHER -> throw NotSupportedDocumentIdentifierException
                    is DocumentIdentifier.PID_ISSUING -> throw NotSupportedDocumentIdentifierException
                }

                resultList.add(RequestDataUi.Space())
                resultList.add(optionalField)

                if (index != (transformedRequestDataUi.optionalFields.size + transformedRequestDataUi.requiredFields.size) - 1) {
                    resultList.add(RequestDataUi.Space())
                    resultList.add(RequestDataUi.Divider())
                }
            }

            resultList.add(RequestDataUi.Space())

            if (transformedRequestDataUi.requiredFields.isNotEmpty()) {
                resultList.add(
                    mockCreateRequiredFieldsForPid(
                        docId = transformedRequestDataUi.documentId,
                        requiredFieldsWholeSectionId = itemsIndex,
                        requiredFields = transformedRequestDataUi.requiredFields
                    )
                )
                resultList.add(RequestDataUi.Space())
            }
        }

        return resultList
    }

    val mockedTransformedRequestDataUiForMdlWithBasicFields = TestTransformedRequestDataUi(
        documentId = mockedMdlId,
        documentIdentifierUi = DocumentIdentifier.MDL,
        documentTitle = mockedDocUiNameMdl,
        optionalFields = mockedOptionalFieldsForMdlWithBasicFields,
        requiredFields = emptyList()
    )

    private fun mockCreateOptionalFieldForPid(
        docId: String,
        elementIdentifier: String,
        value: String,
        checked: Boolean = true,
        enabled: Boolean = true,
    ): RequestDataUi.OptionalField<Event> {
        val uniqueId = mockedPidDocType + elementIdentifier + docId
        return mockCreateOptionalField(
            documentIdentifierUi = DocumentIdentifier.PID_SDJWT,
            uniqueId = uniqueId,
            elementIdentifier = elementIdentifier,
            value = value,
            checked = checked,
            enabled = enabled,
            event = Event.UserIdentificationClicked(itemId = uniqueId)
        )
    }

    private fun mockCreateOptionalFieldForMdl(
        docId: String,
        elementIdentifier: String,
        value: String,
        checked: Boolean = true,
        enabled: Boolean = true,
    ): RequestDataUi.OptionalField<Event> {
        val uniqueId = mockedMdlDocType + elementIdentifier + docId
        return mockCreateOptionalField(
            documentIdentifierUi = DocumentIdentifier.MDL,
            uniqueId = uniqueId,
            elementIdentifier = elementIdentifier,
            value = value,
            checked = checked,
            enabled = enabled,
            event = Event.UserIdentificationClicked(itemId = uniqueId)
        )
    }

    private fun mockCreateOptionalFieldForVerifiedEmail(
        docId: String,
        elementIdentifier: String,
        value: String,
        checked: Boolean = true,
        enabled: Boolean = true,
    ): RequestDataUi.OptionalField<Event> {
        val uniqueId = mockedEmailDocType + elementIdentifier + docId
        return mockCreateOptionalField(
            documentIdentifierUi = DocumentIdentifier.EMAIL,
            uniqueId = uniqueId,
            elementIdentifier = elementIdentifier,
            value = value,
            checked = checked,
            enabled = enabled,
            event = Event.UserIdentificationClicked(itemId = uniqueId)
        )
    }

    private fun mockCreateOptionalField(
        documentIdentifierUi: DocumentIdentifier,
        uniqueId: String,
        elementIdentifier: String,
        value: String,
        checked: Boolean,
        enabled: Boolean,
        event: Event,
    ): RequestDataUi.OptionalField<Event> {
        return RequestDataUi.OptionalField(
            optionalFieldItemUi = OptionalFieldItemUi(
                requestDocumentItemUi = mockCreateRequestDocumentItemUi(
                    documentIdentifierUi = documentIdentifierUi,
                    uniqueId = uniqueId,
                    elementIdentifier = elementIdentifier,
                    value = value,
                    checked = checked,
                    enabled = enabled,
                    event = event
                )
            )
        )
    }

    private fun mockCreateRequestDocumentItemUi(
        documentIdentifierUi: DocumentIdentifier,
        uniqueId: String,
        elementIdentifier: String,
        value: String,
        checked: Boolean,
        enabled: Boolean,
        event: Event?,
    ): RequestDocumentItemUi<Event> {
        val namespace: String
        val docId: String
        val docType: DocType
        val docRequest: DocRequest

        when (documentIdentifierUi) {
            is DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> {
                namespace = mockedPidNameSpace
                docId = mockedPidId
                docType = mockedPidDocType
                docRequest = mockedPidWithBasicFieldsDocRequest
            }

            is DocumentIdentifier.MDL -> {
                namespace = mockedMdlNameSpace
                docId = mockedMdlId
                docType = mockedMdlDocType
                docRequest = mockedMdlWithBasicFieldsDocRequest
            }

            DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> {
                namespace = mockedEmailNameSpace
                docId = mockedEmailId
                docType = mockedEmailDocType
                docRequest = mockedVerifiedEmailWithBasicFieldsDocRequest
            }

            is DocumentIdentifier.SAMPLE, is DocumentIdentifier.OTHER, DocumentIdentifier.PID_ISSUING -> throw NotSupportedDocumentIdentifierException
        }

        return RequestDocumentItemUi(
            id = uniqueId,
            domainPayload = DocumentItemDomainPayload(
                docId = docId,
                docType = docType,
                docRequest = docRequest,
                namespace = namespace,
                elementIdentifier = elementIdentifier
            ),
            readableName = elementIdentifier,
            value = value,
            checked = checked,
            enabled = enabled,
            docItem = DocItem(
                namespace = namespace,
                elementIdentifier = elementIdentifier
            ),
            event = event
        )
    }

    private fun mockCreateRequiredFieldsForPid(
        docId: String,
        requiredFieldsWholeSectionId: Int,
        requiredFields: List<TestFieldUi>,
    ): RequestDataUi.RequiredFields<Event> {
        val requestDocumentItemsUi: MutableList<RequestDocumentItemUi<Event>> = mutableListOf()
        requiredFields.forEach {
            val uniqueId = mockedPidDocType + it.elementIdentifier + docId
            requestDocumentItemsUi.add(
                mockCreateRequestDocumentItemUi(
                    documentIdentifierUi = DocumentIdentifier.PID_SDJWT,
                    uniqueId = uniqueId,
                    elementIdentifier = it.elementIdentifier,
                    value = it.value,
                    checked = it.isAvailable,
                    enabled = false,
                    event = null
                )
            )
        }

        return RequestDataUi.RequiredFields(
            requiredFieldsItemUi = RequiredFieldsItemUi(
                id = requiredFieldsWholeSectionId,
                requestDocumentItemsUi = requestDocumentItemsUi,
                expanded = false,
                title = mockedRequestRequiredFieldsTitle,
                event = Event.ExpandOrCollapseRequiredDataList(id = requiredFieldsWholeSectionId)
            )
        )
    }

    fun DocumentUi.wrapWithMetaData(): DocumentUi {
        val fakeMetaData = DocumentMetaData(
            uniqueDocumentId = this.documentId,
            documentName = this.documentName,
            logo = DocumentMetaData.Image(
                url = "https://someUrl.png",
                contentDescription = "image description"
            ),
            backgroundColor = "#FFFFFF",
            backgroundImage = DocumentMetaData.Image(
                url = "https://someUrl.png",
                contentDescription = "image description"
            ),
            textColor = "#FFFFFF"
        )
        return this.copy(
            documentMetaData = fakeMetaData
        )
    }
}