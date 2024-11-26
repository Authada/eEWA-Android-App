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

package eu.europa.ec.issuancefeature.ui.document.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.theme.values.textPrimary
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.DrivingPrivilegesData
import eu.europa.ec.uilogic.component.ScalableText
import eu.europa.ec.uilogic.component.defaultInfoNameTextStyle
import eu.europa.ec.uilogic.component.defaultInfoValueTextStyle
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.uilogic.component.wrap.WrapImage


@Composable
internal fun DrivingPrivilegesDetailItem(
    title: String,
    items: List<DrivingPrivilegesData>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = defaultInfoNameTextStyle
        )

        items.map { category ->
            DrivingCategoryDetailItem(
                category = category
            )
        }
    }

}

@Composable
private fun DrivingCategoryDetailItem(
    category: DrivingPrivilegesData
) {
    val detailsTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.textPrimary
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {

        Category(
            category = category,
            categoryTextStyle = defaultInfoValueTextStyle
        )

        Column(
            modifier = Modifier.padding(start = SPACING_MEDIUM.dp),
            verticalArrangement = Arrangement.spacedBy(SPACING_EXTRA_SMALL.dp)
        ) {
            category.values.forEach { listOfMaps ->
                listOfMaps.forEach { map ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ScalableText(
                            modifier = Modifier.weight(1f),
                            text = "${map.key}:",
                            textStyle = detailsTextStyle
                        )

                        ScalableText(
                            modifier = Modifier.weight(1f),
                            text = map.value,
                            textStyle = detailsTextStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Category(
    category: DrivingPrivilegesData,
    categoryTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScalableText(
            text = stringResource(
                R.string.document_details_mdl_category_code,
                category.vehicleCategoryCode
            ),
            textStyle = categoryTextStyle
        )

        WrapImage(
            modifier = Modifier.size(24.dp),
            iconData = category.icon.copy(
                tint = MaterialTheme.colorScheme.tertiary
            )
        )
    }
}


@Composable
private fun DetailItem(
    title: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
    ) {
        Text(
            text = title,
            style = defaultInfoNameTextStyle
        )

        Text(
            text = value,
            style = defaultInfoValueTextStyle
        )
    }
}

@ThemeModePreviews
@Composable
private fun DrivingCategoryDetailItemPreview() {
    PreviewTheme {
        DrivingPrivilegesDetailItem(
            title = "Privileges",
            items = listOf(
                DrivingPrivilegesData(
                    vehicleCategoryCode = "A",
                    values =
                    listOf(
                        mapOf("Code" to "78"),
                        mapOf(
                            "Issue date" to "03.03.2010",
                            "Expiry date" to "03.03.2020",
                            "Code" to "S02",
                            "Sign" to "<",
                            "Value" to "5"
                        )
                    ),
                    icon = AppIcons.DrivingCategory.TwoWheeler
                ),
                DrivingPrivilegesData(
                    vehicleCategoryCode = "B",
                    values =
                    listOf(
                        mapOf("Code" to "78"),
                        mapOf(
                            "Issue date" to "03.03.2010",
                            "Expiry date" to "03.03.2020",
                            "Code" to "S02",
                            "Sign" to "<",
                            "Value" to "5"
                        )
                    ),
                    icon = AppIcons.DrivingCategory.Car
                )
            )
        )
    }
}
