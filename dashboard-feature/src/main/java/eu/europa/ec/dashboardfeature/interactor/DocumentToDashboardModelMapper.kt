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

package eu.europa.ec.dashboardfeature.interactor

import eu.europa.ec.commonfeature.model.DocumentUi
import eu.europa.ec.commonfeature.ui.document_details.model.DocumentDetailsUi
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.dashboardfeature.DashboardDocumentModel
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData

internal object DocumentToDashboardModelMapper {

    fun List<DocumentUi>.mapToDashboardUi(resourceProvider: ResourceProvider): List<DashboardDocumentModel> {
        return this.map {
            mapItem(it, resourceProvider)
        }
    }

    private fun mapItem(
        documentUi: DocumentUi,
        resourceProvider: ResourceProvider
    ): DashboardDocumentModel {
        return with(documentUi) {
            DashboardDocumentModel(
                documentId = documentId,
                documentName = documentName,
                documentIdentifier = documentIdentifier,
                documentMetaData = documentMetaData,
                bottomDetail = getBottomDetailByDocType(documentUi, resourceProvider),
            )
        }
    }

    private fun getBottomDetailByDocType(
        documentUi: DocumentUi,
        resourceProvider: ResourceProvider
    ): InfoTextWithNameAndValueData? {
        return when (documentUi.documentIdentifier) {
            DocumentIdentifier.MDL, DocumentIdentifier.PID_SDJWT, DocumentIdentifier.PID_MDOC -> {
                documentUi.userFullName?.let { fullName ->
                    InfoTextWithNameAndValueData.create(
                        resourceProvider.getString(R.string.dashboard_document_owner),
                        fullName
                    )
                }
            }

            else -> {
                val itemData: InfoTextWithNameAndValueData? = documentUi.highlightedFields
                    .filterIsInstance<DocumentDetailsUi.DefaultItem>()
                    .firstOrNull()?.itemData

                itemData?.infoValues?.toTypedArray()?.let { info ->
                    return InfoTextWithNameAndValueData.create(
                        title = "", //Per design as the Card already shows same text
                        infoValues = info
                    )
                } ?: itemData
            }
        }
    }
}