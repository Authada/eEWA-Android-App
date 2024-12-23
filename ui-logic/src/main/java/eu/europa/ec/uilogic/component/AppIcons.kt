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

package eu.europa.ec.uilogic.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import eu.europa.ec.resourceslogic.R

/**
 * Data class to be used when we want to display an Icon.
 * @param resourceId The id of the icon. Can be null
 * @param contentDescriptionId The id of its content description.
 * @param imageVector The [ImageVector] of the icon, null by default.
 * @throws IllegalArgumentException If both [resourceId] AND [imageVector] are null.
 */
@Stable
data class IconData(
    @DrawableRes val resourceId: Int?,
    @StringRes val contentDescriptionId: Int,
    val imageVector: ImageVector? = null,
    val tint: Color? = null
) {
    init {
        require(
            resourceId == null && imageVector != null
                    || resourceId != null && imageVector == null
                    || resourceId != null && imageVector != null
        ) {
            "An Icon should at least have a valid resourceId or a valid imageVector."
        }
    }
}

/**
 * A Singleton object responsible for providing access to all the app's Icons.
 */
object AppIcons {

    object DrivingCategory {
        val TwoWheeler: IconData = IconData(
            resourceId = R.drawable.ic_category_two_wheeler,
            contentDescriptionId = R.string.content_description_empty
        )

        val Car: IconData = IconData(
            resourceId = R.drawable.ic_category_car,
            contentDescriptionId = R.string.content_description_empty
        )

        val Delivery: IconData = IconData(
            resourceId = R.drawable.ic_category_delivery,
            contentDescriptionId = R.string.content_description_empty
        )

        val Transportation: IconData = IconData(
            resourceId = R.drawable.ic_category_bus,
            contentDescriptionId = R.string.content_description_empty
        )

        val Agriculture: IconData = IconData(
            resourceId = R.drawable.ic_category_agriculture,
            contentDescriptionId = R.string.content_description_empty
        )
    }


    val ArrowBack: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_arrow_back_icon,
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
    )

    val Close: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_close_icon,
        imageVector = Icons.Filled.Close
    )

    val VerticalMore: IconData = IconData(
        resourceId = R.drawable.ic_more,
        contentDescriptionId = R.string.content_description_more_vert_icon,
        imageVector = null
    )

    val Warning: IconData = IconData(
        resourceId = R.drawable.ic_warning,
        contentDescriptionId = R.string.content_description_warning_icon,
        imageVector = null
    )

    val Error: IconData = IconData(
        resourceId = R.drawable.ic_error,
        contentDescriptionId = R.string.content_description_error_icon,
        imageVector = null
    )

    val Delete: IconData = IconData(
        resourceId = R.drawable.ic_delete,
        contentDescriptionId = R.string.content_description_delete_icon,
        imageVector = null
    )

    val Help: IconData = IconData(
        resourceId = R.drawable.ic_help,
        contentDescriptionId = R.string.content_description_help_icon,
        imageVector = null
    )

    val Check: IconData = IconData(
        resourceId = R.drawable.ic_check,
        contentDescriptionId = R.string.content_description_check_icon,
        imageVector = null
    )

    val TouchId: IconData = IconData(
        resourceId = R.drawable.ic_touch_id,
        contentDescriptionId = R.string.content_description_touch_id_icon,
        imageVector = null
    )

    val QR: IconData = IconData(
        resourceId = R.drawable.ic_qr,
        contentDescriptionId = R.string.content_description_qr_icon,
        imageVector = null
    )

    val NFC: IconData = IconData(
        resourceId = R.drawable.ic_nfc,
        contentDescriptionId = R.string.content_description_nfc_icon,
        imageVector = null
    )

    val User: IconData = IconData(
        resourceId = R.drawable.ic_user,
        contentDescriptionId = R.string.content_description_user_icon,
        imageVector = null
    )

    val Id: IconData = IconData(
        resourceId = R.drawable.ic_pid,
        contentDescriptionId = R.string.content_description_id_icon,
        imageVector = null
    )

    val DriversLicense: IconData = IconData(
        resourceId = R.drawable.ic_drivers_license,
        contentDescriptionId = R.string.mdl,
        imageVector = null
    )

    val ProxyId: IconData = IconData(
        resourceId = R.drawable.ic_proxy_document,
        contentDescriptionId = R.string.content_description_id_icon,
        imageVector = null
    )

    val IdStroke: IconData = IconData(
        resourceId = R.drawable.ic_id_stroke,
        contentDescriptionId = R.string.content_description_id_stroke_icon,
        imageVector = null
    )

    val Success: IconData = IconData(
        resourceId = R.drawable.ic_success,
        contentDescriptionId = R.string.content_description_success_icon,
        imageVector = null
    )

    val StoreId: IconData = IconData(
        resourceId = R.drawable.ic_store_id,
        contentDescriptionId = R.string.content_description_store_id_icon,
        imageVector = null
    )

    val Lock: IconData = IconData(
        resourceId = R.drawable.ic_lock,
        contentDescriptionId = R.string.content_description_lock_icon,
        imageVector = null
    )

    val Email: IconData = IconData(
        resourceId = R.drawable.ic_email,
        contentDescriptionId = R.string.content_description_email_icon,
        imageVector = null
    )

    val OtherId: IconData = IconData(
        resourceId = R.drawable.ic_other_document,
        contentDescriptionId = R.string.content_description_id_stroke_icon,
        imageVector = null
    )

    val Logo: IconData = IconData(
        resourceId = R.drawable.ic_logo,
        contentDescriptionId = R.string.content_description_logo_icon,
        imageVector = null
    )

    val KeyboardArrowDown: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_arrow_down_icon,
        imageVector = Icons.Default.KeyboardArrowDown
    )

    val KeyboardArrowUp: IconData = IconData(
        resourceId = null,
        contentDescriptionId = R.string.content_description_arrow_up_icon,
        imageVector = Icons.Default.KeyboardArrowUp
    )

    val VisibilityOn: IconData = IconData(
        resourceId = R.drawable.ic_visibility_on,
        contentDescriptionId = R.string.content_description_visibility_icon,
        imageVector = null
    )

    val VisibilityOff: IconData = IconData(
        resourceId = R.drawable.ic_visibility_off,
        contentDescriptionId = R.string.content_description_visibility_off_icon,
        imageVector = null
    )

    val Add: IconData = IconData(
        resourceId = R.drawable.ic_add,
        contentDescriptionId = R.string.content_description_add_icon,
        imageVector = null
    )

    val Edit: IconData = IconData(
        resourceId = R.drawable.ic_edit,
        contentDescriptionId = R.string.content_description_edit_icon,
        imageVector = null
    )

    val Verified: IconData = IconData(
        resourceId = R.drawable.ic_verified,
        contentDescriptionId = R.string.content_description_verified_icon,
        imageVector = null
    )

    val OpenInBrowser: IconData = IconData(
        resourceId = R.drawable.ic_open_in_browser,
        contentDescriptionId = R.string.content_description_open_verifier_icon,
        imageVector = null
    )

    val ProxyIdentityIcon: IconData = IconData(
        resourceId = R.drawable.ic_proxy_info,
        contentDescriptionId = R.string.content_description_proxy_explanation_icon
    )

    val Message: IconData = IconData(
        resourceId = R.drawable.ic_message,
        contentDescriptionId = R.string.content_description_message,
        imageVector = null
    )
}