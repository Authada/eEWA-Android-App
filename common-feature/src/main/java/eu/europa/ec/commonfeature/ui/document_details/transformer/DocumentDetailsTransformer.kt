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

package eu.europa.ec.commonfeature.ui.document_details.transformer

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import eu.europa.ec.businesslogic.util.toDateFormatted
import eu.europa.ec.businesslogic.util.toList
import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.model.toUiDescription
import eu.europa.ec.commonfeature.model.toUiName
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentMdocKeys
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentSdJwtKeys
import eu.europa.ec.commonfeature.ui.document_details.model.DrivingPrivilege
import eu.europa.ec.commonfeature.ui.document_details.model.Priority
import eu.europa.ec.commonfeature.ui.document_details.model.toIcon
import eu.europa.ec.commonfeature.util.documentHasExpired
import eu.europa.ec.commonfeature.util.extractFullNameFromDocumentOrEmpty
import eu.europa.ec.commonfeature.util.extractValueFromDocumentOrEmpty
import eu.europa.ec.commonfeature.util.parseKeyValueUi
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.corelogic.model.toDocumentIdentifier
import eu.europa.ec.eudi.wallet.document.Document
import eu.europa.ec.eudi.wallet.document.Format
import eu.europa.ec.eudi.wallet.document.nameSpacedDataJSONObject
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.DrivingPrivilegesData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndImageData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData
import org.json.JSONObject

object DocumentDetailsTransformer {

    private val hightlightsFieldsProvider = HighlightedFieldsProvider()

    fun transformToUiItem(
        document: Document,
        resourceProvider: ResourceProvider,
    ): DocumentUi? {

        val documentIdentifierUi = document.toDocumentIdentifier()

        // Get the JSON Object from EudiWallerCore.
        val documentJson =
            (document.nameSpacedDataJSONObject[documentIdentifierUi.nameSpace] as JSONObject)

        val documentValuesJsonArray = documentJson.extractJsonArrayOfValues() ?: return null

        val fullName = extractFullNameFromDocumentOrEmpty(document)

        val detailsItems = documentValuesJsonArray.getDetailItems(resourceProvider)
        val highlightedFields = documentValuesJsonArray.getHighlightItems(
            documentIdentifier = documentIdentifierUi,
            resourceProvider = resourceProvider,
            fullName = fullName
        )

        val documentImage = extractValueFromDocumentOrEmpty(
            document = document,
            key = DocumentMdocKeys.PORTRAIT
        )

        val documentExpirationDate = extractValueFromDocumentOrEmpty(
            document = document,
            key = when (document.format) {
                Format.MSO_MDOC -> DocumentMdocKeys.EXPIRY_DATE
                else -> DocumentSdJwtKeys.EXPIRY_DATE
            }
        )

        val docHasExpired = documentHasExpired(documentExpirationDate)

        val issuer = extractValueFromDocumentOrEmpty(
            document = document,
            key = DocumentMdocKeys.ISSUER
        )

        val documentName =
            document.metaData?.documentName ?: documentIdentifierUi.toUiName(resourceProvider)


        return DocumentUi(
            documentId = document.id,
            documentName = documentName,
            description = documentIdentifierUi.toUiDescription(resourceProvider),
            documentIdentifier = documentIdentifierUi,
            documentExpirationDateFormatted = documentExpirationDate.toDateFormatted() ?: "",
            documentHasExpired = docHasExpired,
            base64Image = documentImage,
            documentIssuer = issuer,
            highlightedFields = highlightedFields,
            documentDetails = detailsItems,
            userFullName = fullName,
            documentMetaData = document.metaData
        )
    }

    private fun Map<Any, IndexedValue<Any>>.getDetailItems(resourceProvider: ResourceProvider): List<DocumentDetailsUi> {
        return this
            // Now that we have both the keys and the values, transform them to UI items.
            .map {
                val value = it.value.value
                val key = it.key.toString()
                transformToDocumentDetailsUi(
                    key = key,
                    item = value,
                    resourceProvider = resourceProvider
                )
            }.sortedBy { it.priority.sortingPriority }
    }

    private fun Map<Any, IndexedValue<Any>>.getHighlightItems(
        documentIdentifier: DocumentIdentifier,
        resourceProvider: ResourceProvider,
        fullName: String?
    ): List<DocumentDetailsUi> {
        val highlights = hightlightsFieldsProvider.getHighlightsToBeShownInDetailsCard(
            documentIdentifier = documentIdentifier,
            json = this
        ).map {
            val value = it.value.value
            val key = it.key.toString()
            transformToDocumentDetailsUi(
                key = key,
                item = value,
                resourceProvider = resourceProvider
            )
        }.toMutableList()

        if (documentIdentifier == DocumentIdentifier.MDL && !fullName.isNullOrEmpty()) {
            highlights.add(
                0,
                DocumentDetailsUi.DefaultItem(
                    itemData = InfoTextWithNameAndValueData.create(
                        title = resourceProvider.getString(R.string.document_details_mdl_user_full_name),
                        infoValues = arrayOf(fullName)
                    )
                )
            )
        }
        return highlights
    }
}

private fun JSONObject.extractJsonArrayOfValues(): Map<Any, IndexedValue<Any>>? {
    // Create a JSON Array with all its keys (i.e. given_name, family_name, etc.) keeping their original order.
    val documentKeysJsonArray = this.names() ?: return null

    // Create a JSON Array with all its values (i.e. John, Smith, etc.) keeping their original order.
    val documentValuesJsonArray = this.toJSONArray(documentKeysJsonArray)
        ?.toList()
        ?.withIndex()
        // Create a connection between keys and values using their index--original order.
        ?.associateBy {
            documentKeysJsonArray.get(it.index)
        } ?: return null

    return documentValuesJsonArray
}

private val TAG = "DocumentDetTrans"

private fun transformToDocumentDetailsUi(
    key: String,
    item: Any,
    resourceProvider: ResourceProvider
): DocumentDetailsUi {


    val uiKey = runCatching {
        resourceProvider.getReadableElementIdentifier(key)
    }.getOrElse {
        Log.w(TAG, "Failed to get ElementIdentifier for document attribute with key '$key'")
        key
    }

    val priority = if (LowPriorityKeys.contains(uiKey)) Priority.LOW else Priority.NORMAL

    if (key == DocumentMdocKeys.DRIVING_PRIVILEGES) {
        parseDrivingPrivileges(jsonString = item.toString(), resourceProvider).run {
            if (isNotEmpty()) {
                return DocumentDetailsUi.DrivingPrivilegesItem(
                    nameOfTheSection = resourceProvider.getString(R.string.document_details_mdl_category_section),
                    itemData = this
                )
            }
        }
    }

    val values = StringBuilder()
    parseKeyValueUi(
        json = item,
        groupIdentifier = key,
        resourceProvider = resourceProvider,
        allItems = values
    )
    val groupedValues = values.toString()

    return when (key) {
        DocumentMdocKeys.SIGNATURE -> {
            DocumentDetailsUi.SignatureItem(
                itemData = InfoTextWithNameAndImageData(
                    title = uiKey,
                    base64Image = groupedValues
                ),
                priority = priority
            )
        }

        DocumentMdocKeys.PORTRAIT -> {
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData.create(
                    title = uiKey,
                    infoValues = arrayOf(resourceProvider.getString(R.string.document_details_portrait_readable_identifier))
                ),
                priority = priority
            )
        }

        else -> {
            DocumentDetailsUi.DefaultItem(
                itemData = InfoTextWithNameAndValueData.create(
                    title = uiKey,
                    infoValues = arrayOf(groupedValues)
                ),
                priority = priority
            )
        }
    }
}

private fun parseDrivingPrivileges(
    jsonString: Any?,
    resourceProvider: ResourceProvider
): List<DrivingPrivilegesData> {
    fun JsonObject.codesToValueMap(): Map<String, String> {
        val jsonObject = JSONObject(toString())
        val mapOfValues = jsonObject.extractJsonArrayOfValues()?.map { codeAndValueMap ->
            val value = codeAndValueMap.value.value
            val key = codeAndValueMap.key.toString()

            val uiKey = runCatching {
                resourceProvider.getReadableElementIdentifier(key)
            }.getOrElse {
                Log.w(
                    TAG,
                    "Failed to get mDL code identifier $key"
                )
                key
            }
            Pair(uiKey, value.toString())
        }?.toMap()

        return mapOfValues ?: emptyMap()
    }


    val privileges = try {
        Gson().fromJson(
            jsonString.toString(),
            Array<DrivingPrivilege>::class.java
        )
    } catch (e: Exception) {
        return emptyList()
    }


    val data: List<DrivingPrivilegesData> = privileges.map {
        val values = it.codes?.map { codesList ->
            codesList.codesToValueMap()
        }?.toMutableList() ?: mutableListOf()

        val dates = mapOf(
            resourceProvider.getString(eu.europa.ec.eudi.wallet.document.R.string.issue_date) to it.issueDate,
            resourceProvider.getString(eu.europa.ec.eudi.wallet.document.R.string.expiry_date) to it.expiryDate,
        )

        values.add(0, dates)

        DrivingPrivilegesData(
            vehicleCategoryCode = it.vehicleCategoryCode.name,
            values = values,
            icon = it.vehicleCategoryCode.toIcon()
        )
    }

    return data
}