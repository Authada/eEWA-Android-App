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

package eu.europa.ec.commonfeature.ui.request.transformer

import eu.europa.ec.commonfeature.model.toUiName
import eu.europa.ec.commonfeature.ui.request.Event
import eu.europa.ec.commonfeature.ui.request.model.DocumentItemDomainPayload
import eu.europa.ec.commonfeature.ui.request.model.DocumentItemUi
import eu.europa.ec.commonfeature.ui.request.model.OptionalFieldItemUi
import eu.europa.ec.commonfeature.ui.request.model.RequestDataUi
import eu.europa.ec.commonfeature.ui.request.model.RequestDocumentItemUi
import eu.europa.ec.commonfeature.ui.request.model.RequiredFieldsItemUi
import eu.europa.ec.commonfeature.ui.request.model.produceDocUID
import eu.europa.ec.commonfeature.ui.request.model.toRequestDocumentItemUi
import eu.europa.ec.commonfeature.util.parseKeyValueUi
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.corelogic.model.toDocumentIdentifier
import eu.europa.ec.eudi.iso18013.transfer.DisclosedDocument
import eu.europa.ec.eudi.iso18013.transfer.DisclosedDocuments
import eu.europa.ec.eudi.iso18013.transfer.DocItem
import eu.europa.ec.eudi.iso18013.transfer.RequestDocument
import eu.europa.ec.eudi.wallet.document.Document
import eu.europa.ec.eudi.wallet.document.Document.Companion.PROXY_ID_PREFIX
import eu.europa.ec.eudi.wallet.document.nameSpacedDataJSONObject
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import org.json.JSONObject

private fun getMandatoryFields(documentIdentifier: DocumentIdentifier): List<String> = when (documentIdentifier) {

    DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> listOf(
        "issuance_date",
        "expiry_date",
        "issuing_authority",
        "document_number",
        "administrative_number",
        "issuing_country",
        "issuing_jurisdiction",
        "portrait",
        "portrait_capture_date"
    )

    else -> emptyList()
}

object RequestTransformer {

    private const val TAG = "RequestTransformer"

    fun transformToUiItems(
        storageDocuments: List<Document> = emptyList(),
        resourceProvider: ResourceProvider,
        requestDocuments: List<RequestDocument>,
        requiredFieldsTitle: String,
    ): List<RequestDataUi<Event>> {
        val items = mutableListOf<RequestDataUi<Event>>()
        val firstDocumentIdThatIsGoingToBePreSelected = requestDocuments.first().documentId

        requestDocuments.forEachIndexed { docIndex, requestDocument ->
            // Add document item.
            items += RequestDataUi.Document(
                documentItemUi = DocumentItemUi(
                    title = requestDocument.toDocumentIdentifier()
                        .toUiName(resourceProvider)
                ),
                isProxy = requestDocument.documentId.startsWith(PROXY_ID_PREFIX)
            )
            items += RequestDataUi.Space()

            val required = mutableListOf<RequestDocumentItemUi<Event>>()
            val storageDocument =
                storageDocuments.firstOrNull { it.id == requestDocument.documentId }
            addFields(
                requestDocument = requestDocument,
                storageDocument = storageDocument,
                resourceProvider = resourceProvider,
                required = required,
                items = items,
                preselectFields = requestDocument.documentId == firstDocumentIdThatIsGoingToBePreSelected
            )

            items += RequestDataUi.Space()

            // Add required fields item.
            if (required.isNotEmpty()) {
                items += RequestDataUi.RequiredFields(
                    requiredFieldsItemUi = RequiredFieldsItemUi(
                        id = docIndex,
                        requestDocumentItemsUi = required,
                        expanded = false,
                        title = requiredFieldsTitle,
                        event = Event.ExpandOrCollapseRequiredDataList(id = docIndex)
                    )
                )
                items += RequestDataUi.Space()
            }
        }

        return items
    }


    private fun addFields(
        requestDocument: RequestDocument,
        storageDocument: Document?,
        resourceProvider: ResourceProvider,
        required: MutableList<RequestDocumentItemUi<Event>>,
        items: MutableList<RequestDataUi<Event>>,
        preselectFields: Boolean,
    ) {
        // Add optional field items.
        requestDocument.docRequest.requestItems.forEachIndexed { itemIndex, docItem ->
            val (value, isAvailable) = try {
                val values = StringBuilder()
                val json = if (storageDocument != null) {
                    storageDocument.nameSpacedDataJSONObject.getDocObject(requestDocument.docType)[docItem.elementIdentifier]
                } else {
                    docItem.elementIdentifier
                }
                parseKeyValueUi(
                    json = json,
                    groupIdentifier = docItem.elementIdentifier,
                    resourceProvider = resourceProvider,
                    allItems = values
                )
                (values.toString() to true)
            } catch (ex: Exception) {
                (resourceProvider.getString(R.string.request_element_identifier_not_available) to false)
            }

            val isMandatory = getMandatoryFields(documentIdentifier = requestDocument.toDocumentIdentifier())
                .contains(docItem.elementIdentifier)
            if (isMandatory) {
                val newRequired = docItem.toRequestDocumentItemUi<Event>(
                    uID = requestDocument.docRequest.produceDocUID(
                        docItem.elementIdentifier,
                        requestDocument.documentId
                    ),
                    docPayload = DocumentItemDomainPayload(
                        docId = requestDocument.documentId,
                        docRequest = requestDocument.docRequest,
                        docType = requestDocument.docType,
                        namespace = docItem.namespace,
                        elementIdentifier = docItem.elementIdentifier,
                    ),
                    optional = false,
                    isChecked = isAvailable,
                    event = null,
                    readableName = resourceProvider.getReadableElementIdentifier(docItem.elementIdentifier),
                    value = value
                )
                required.add(newRequired)
            } else {

                if(docItem.elementIdentifier != "vct") {
                    val uID = requestDocument.docRequest.produceDocUID(
                        docItem.elementIdentifier,
                        requestDocument.documentId
                    )
                    items += RequestDataUi.Space()
                    items += RequestDataUi.OptionalField(
                        optionalFieldItemUi = OptionalFieldItemUi(
                            requestDocumentItemUi = docItem.toRequestDocumentItemUi(
                                uID = uID,
                                docPayload = DocumentItemDomainPayload(
                                    docId = requestDocument.documentId,
                                    docRequest = requestDocument.docRequest,
                                    docType = requestDocument.docType,
                                    namespace = docItem.namespace,
                                    elementIdentifier = docItem.elementIdentifier,
                                ),
                                optional = isAvailable,
                                isChecked = isAvailable && preselectFields,
                                event = Event.UserIdentificationClicked(itemId = uID),
                                readableName = resourceProvider.getReadableElementIdentifier(docItem.elementIdentifier),
                                value = value
                            )
                        )
                    )

                    if (itemIndex != requestDocument.docRequest.requestItems.lastIndex) {
                        items += RequestDataUi.Space()
                        items += RequestDataUi.Divider()
                    }
                }
            }
        }

    }

    fun transformToDomainItems(uiItems: List<RequestDataUi<Event>>): DisclosedDocuments {
        val selectedUiItems = uiItems
            .flatMap {
                when (it) {
                    is RequestDataUi.RequiredFields -> {
                        it.requiredFieldsItemUi.requestDocumentItemsUi
                    }

                    is RequestDataUi.OptionalField -> {
                        listOf(it.optionalFieldItemUi.requestDocumentItemUi)
                    }

                    else -> {
                        emptyList()
                    }
                }
            }
            // Get selected
            .filter { it.checked }
            // Create a Map with document as a key
            .groupBy {
                it.domainPayload
            }

        return DisclosedDocuments(
            selectedUiItems.map { entry ->
                val (document, selectedDocumentItems) = entry
                DisclosedDocument(
                    documentId = document.docId,
                    docType = document.docType,
                    selectedDocItems = selectedDocumentItems.map {
                        DocItem(
                            it.domainPayload.namespace,
                            it.domainPayload.elementIdentifier
                        )
                    },
                    docRequest = document.docRequest
                )
            }
        )
    }

    // TODO Provide proper docType from Core
    private fun JSONObject.getDocObject(docType: String): JSONObject =
        this[docType.replace(".mDL", "")] as JSONObject
}