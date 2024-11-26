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

package eu.europa.ec.uilogic.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import coil.compose.SubcomposeAsyncImage
import eu.europa.ec.eudi.wallet.document.room.DocumentMetaData
import eu.europa.ec.uilogic.component.wrap.WrapImage

@Composable
fun DocumentMetaDataImage(
    metaData: DocumentMetaData.Image?,
    errorImage: IconData?,
    modifier: Modifier = Modifier
) {
    SubcomposeAsyncImage(
        modifier = modifier,
        model = metaData?.url,
        contentDescription = metaData?.contentDescription,
        error = {
            if (errorImage != null) {
                WrapImage(iconData = errorImage)
            } else {
                val isPreview = LocalInspectionMode.current
                if (isPreview) {
                    WrapImage(iconData = AppIcons.ProxyIdentityIcon)
                }
            }
        }
    )
}