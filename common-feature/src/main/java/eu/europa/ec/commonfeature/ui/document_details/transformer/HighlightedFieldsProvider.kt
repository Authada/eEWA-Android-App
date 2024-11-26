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
 */

package eu.europa.ec.commonfeature.ui.document_details.transformer

import eu.europa.ec.commonfeature.ui.document_details.model.DocumentMdocKeys
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentSdJwtKeys
import eu.europa.ec.corelogic.model.DocumentIdentifier

internal class HighlightedFieldsProvider {

    fun getHighlightsToBeShownInDetailsCard(
        documentIdentifier: DocumentIdentifier,
        json: Map<Any, IndexedValue<Any>>
    ): Map<Any, IndexedValue<Any>> {
        val list = when (documentIdentifier) {
            DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> json.entries.filter {
                KEYS_FOR_EMAIL.contains(
                    it.key.toString()
                )
            }

            DocumentIdentifier.MDL -> {
                json.entries.filter { KEYS_FOR_DRIVERS_LICENSE.contains(it.key.toString()) }
            }

            DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC, DocumentIdentifier.PID_ISSUING, DocumentIdentifier.SAMPLE -> emptyList()
            else -> json.entries.take(3)
        }

        return list.associate { it.toPair() }
    }

    companion object {
        private val KEYS_FOR_DRIVERS_LICENSE = listOf(
            DocumentMdocKeys.EXPIRY_DATE
        )
        private val KEYS_FOR_EMAIL = listOf(
            DocumentMdocKeys.EMAIL,
            DocumentMdocKeys.ISSUE_DATE,
            DocumentMdocKeys.ISSUANCE_DATE,
            DocumentMdocKeys.EXPIRY_DATE,
            DocumentSdJwtKeys.ISSUE_DATE,
            DocumentSdJwtKeys.EXPIRY_DATE
        )
    }
}