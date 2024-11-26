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

package eu.europa.ec.commonfeature.ui.document_details.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.commonfeature.ui.document_details.model.DrivingPrivilege.Category.*
import eu.europa.ec.uilogic.component.IconData


internal data class DrivingPrivilege(
    @SerializedName("vehicle_category_code")
    val vehicleCategoryCode: Category,
    @SerializedName("issue_date")
    val issueDate: String,
    @SerializedName("expiry_date")
    val expiryDate: String,
    @SerializedName("codes")
    val codes: List<JsonObject>?
) {
    enum class Category {
        AM, A1, A2, A, //Two wheelers
        B1, B, BE, // Car
        C1, C, C1E, CE, // Delivery vans/trucks
        D1, D, D1E, DE, // Transportation bus
        L, T // Agriculture
    }
}


internal fun DrivingPrivilege.Category.toIcon(): IconData {
    return when (this) {
        AM, A1, A2, A -> AppIcons.DrivingCategory.TwoWheeler
        B1, B, BE -> AppIcons.DrivingCategory.Car
        C1, C, C1E, CE -> AppIcons.DrivingCategory.Delivery
        D1, D, D1E, DE -> AppIcons.DrivingCategory.Transportation
        L, T -> AppIcons.DrivingCategory.Agriculture
    }
}