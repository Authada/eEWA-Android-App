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

package eu.europa.ec.commonfeature.ui.document_details.model

import eu.europa.ec.uilogic.component.DrivingPrivilegesData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndImageData
import eu.europa.ec.uilogic.component.InfoTextWithNameAndValueData

sealed interface DocumentDetailsUi {

    val priority: Priority

    data class DefaultItem(
        val itemData: InfoTextWithNameAndValueData,
        override val priority: Priority = Priority.NORMAL
    ) : DocumentDetailsUi

    data class SignatureItem(
        val itemData: InfoTextWithNameAndImageData,
        override val priority: Priority = Priority.NORMAL
    ) : DocumentDetailsUi

    data class DrivingPrivilegesItem(
        val nameOfTheSection: String,
        val itemData: List<DrivingPrivilegesData>,
        override val priority: Priority = Priority.NORMAL
    ) : DocumentDetailsUi

    data object Unknown : DocumentDetailsUi {
        override val priority: Priority = Priority.LOW
    }
}

enum class Priority(val sortingPriority: Int) {
    NORMAL(0),
    LOW(1)
}