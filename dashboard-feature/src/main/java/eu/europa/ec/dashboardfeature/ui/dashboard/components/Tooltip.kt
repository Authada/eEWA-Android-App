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

package eu.europa.ec.dashboardfeature.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import eu.europa.ec.resourceslogic.theme.values.tooltipSecondaryTextColor
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.ELEVATION_HIGH
import eu.europa.ec.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.VSpacer

private val CORNER = 50.dp
private const val OFFSET_X_PERCENT = 5
private const val OFFSET_Y_PERCENT = 55
private const val HEIGHT_OF_THE_CARD_PERCENT = 70


@Composable
internal fun Tooltip(
    cardSize: IntSize,
    cardPosition: Offset,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var showDialog by remember {
        mutableStateOf(true)
    }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val offsetForPlacingBubble = IntOffset(
        x = cardSize.width - (cardSize.width * OFFSET_X_PERCENT / 100),
        y = cardSize.height - (cardSize.height * OFFSET_Y_PERCENT / 100)
    )

    if (showDialog) {
        Popup(
            onDismissRequest = { showDialog = false },
            alignment = Alignment.Center,
            offset = offsetForPlacingBubble
        ) {
            TooltipContent(
                cardSize = cardSize,
                offset = offsetForPlacingBubble,
                screenWidth = screenWidth,
                modifier = modifier,
                onClick = {
                    onClick()
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun TooltipContent(
    cardSize: IntSize,
    offset: IntOffset,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = CORNER,
        bottomEnd = CORNER,
        bottomStart = CORNER
    )

    with(LocalDensity.current) {
        val bubbleHeight = (cardSize.height * HEIGHT_OF_THE_CARD_PERCENT / 100).toDp()
        val bubbleWidth = screenWidth - cardSize.width.toDp() - SPACING_LARGE.dp
        Card(
            modifier = modifier
                .height(bubbleHeight)
                .width(bubbleWidth)
                .shadow(
                    elevation = ELEVATION_HIGH.dp, shape = shape
                )
                .clickable {
                    onClick()
                },
            shape = shape,
            colors = CardDefaults.elevatedCardColors().copy(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,

            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    modifier = Modifier
                        .padding(horizontal = SPACING_MEDIUM.dp),
                    text = stringResource(id = eu.europa.ec.resourceslogic.R.string.dashboard_tooltip_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )

                VSpacer.Small()

                Text(
                    modifier = Modifier
                        .padding(horizontal = SPACING_MEDIUM.dp),
                    text = stringResource(id = eu.europa.ec.resourceslogic.R.string.dashboard_tooltip_subtitle),
                    style = MaterialTheme.typography.bodyMedium
                        .copy(color = MaterialTheme.colorScheme.tooltipSecondaryTextColor)
                )
            }
        }
    }
}

@Composable
@ThemeModePreviews
private fun TooltipPreview() {
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            TooltipContent(
                cardSize = IntSize(100, 450),
                offset = IntOffset(x = 10, y = 0),
                screenWidth = 300.dp,
                onClick = {}
            )
        }
    }
}