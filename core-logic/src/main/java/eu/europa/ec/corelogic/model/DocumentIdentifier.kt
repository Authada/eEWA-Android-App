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

package eu.europa.ec.corelogic.model

import eu.europa.ec.eudi.iso18013.transfer.RequestDocument
import eu.europa.ec.eudi.wallet.document.Document

typealias DocType = String

sealed interface DocumentIdentifier {
    val nameSpace: String
    val docType: DocType

    data object PID_SDJWT : DocumentIdentifier {
        override val nameSpace: String
            get() = "https://metadata-8c062a.usercontent.opencode.de/pid.json"
        override val docType: DocType
            get() = "https://metadata-8c062a.usercontent.opencode.de/pid.json"
    }

    data object PID_MDOC : DocumentIdentifier {
        override val nameSpace: String
            get() = "eu.europa.ec.eudi.pid.1"
        override val docType: DocType
            get() = "eu.europa.ec.eudi.pid.1"
    }

    data object PID_ISSUING : DocumentIdentifier {
        override val nameSpace: String
            get() = "eu.europa.ec.eudi.pid.1_issuing"
        override val docType: DocType
            get() = "eu.europa.ec.eudi.pid.1_issuing"
    }

    data object MDL : DocumentIdentifier {
        override val nameSpace: String
            get() = "org.iso.18013.5.1"
        override val docType: DocType
            get() = "org.iso.18013.5.1.mDL"
    }

    data object EMAIL : DocumentIdentifier {
        override val nameSpace: String
            get() = "eu.europa.ec.eudi.email.1"
        override val docType: DocType
            get() = "eu.europa.ec.eudi.email.1"
    }

    data object EMAIL_URN : DocumentIdentifier {
        override val nameSpace: String
            get() = "urn:eu.europa.ec.eudi:email:1"
        override val docType: DocType
            get() = "urn:eu.europa.ec.eudi:email:1"
    }

    data object SAMPLE : DocumentIdentifier {
        override val nameSpace: String
            get() = "load_sample_documents"
        override val docType: DocType
            get() = "load_sample_documents"
    }

    data class OTHER(
        override val nameSpace: String,
        override val docType: DocType,
    ) : DocumentIdentifier
}

fun DocumentIdentifier.isSupported(): Boolean {
    return when (this) {
        DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC,
        DocumentIdentifier.MDL,
        DocumentIdentifier.EMAIL, DocumentIdentifier.EMAIL_URN -> true

        DocumentIdentifier.SAMPLE, is DocumentIdentifier.OTHER -> false
        DocumentIdentifier.PID_ISSUING -> throw IllegalArgumentException("document type ${DocumentIdentifier.PID_ISSUING.docType} only used for secure element provisioning")

    }
}

/**
 * @return A [DocumentIdentifier] from a DocType.
 * This function should ONLY be called on docType and NOT on nameSpace.
 */
fun DocType.toDocumentIdentifier(): DocumentIdentifier = when (this) {
    DocumentIdentifier.PID_SDJWT.docType -> DocumentIdentifier.PID_SDJWT
    DocumentIdentifier.PID_MDOC.docType -> DocumentIdentifier.PID_MDOC
    DocumentIdentifier.MDL.docType -> DocumentIdentifier.MDL
    DocumentIdentifier.EMAIL.docType -> DocumentIdentifier.EMAIL
    DocumentIdentifier.EMAIL_URN.docType -> DocumentIdentifier.EMAIL_URN
    DocumentIdentifier.SAMPLE.docType -> DocumentIdentifier.SAMPLE
    else -> DocumentIdentifier.OTHER(
        nameSpace = this,
        docType = this
    )
}

fun Document.toDocumentIdentifier(): DocumentIdentifier {
    val nameSpace = this.nameSpaces.keys.first()
    val docType = this.docType

    return createDocumentIdentifier(nameSpace, docType)
}

fun RequestDocument.toDocumentIdentifier(): DocumentIdentifier {
    val nameSpace = this.docRequest.requestItems.first().namespace
    val docType = this.docType

    return createDocumentIdentifier(nameSpace, docType)
}

private fun createDocumentIdentifier(nameSpace: String, docType: DocType): DocumentIdentifier {
    return when {
        nameSpace == DocumentIdentifier.PID_SDJWT.nameSpace
                && docType == DocumentIdentifier.PID_SDJWT.docType -> DocumentIdentifier.PID_SDJWT

        nameSpace == DocumentIdentifier.PID_MDOC.nameSpace
                && docType == DocumentIdentifier.PID_MDOC.docType -> DocumentIdentifier.PID_MDOC

        nameSpace == DocumentIdentifier.MDL.nameSpace
                && docType == DocumentIdentifier.MDL.docType -> DocumentIdentifier.MDL

        nameSpace == DocumentIdentifier.EMAIL.nameSpace
                && docType == DocumentIdentifier.EMAIL.docType -> DocumentIdentifier.EMAIL

        nameSpace == DocumentIdentifier.EMAIL_URN.nameSpace
                && docType == DocumentIdentifier.EMAIL_URN.docType -> DocumentIdentifier.EMAIL_URN

        nameSpace == DocumentIdentifier.SAMPLE.nameSpace
                && docType == DocumentIdentifier.SAMPLE.docType -> DocumentIdentifier.SAMPLE

        else -> DocumentIdentifier.OTHER(
            nameSpace = nameSpace,
            docType = docType
        )
    }
}