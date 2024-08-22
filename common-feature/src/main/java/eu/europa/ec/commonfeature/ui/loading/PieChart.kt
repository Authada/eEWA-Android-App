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

package eu.europa.ec.commonfeature.ui.loading

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import eu.europa.ec.resourceslogic.theme.WalletTheme

@Composable
internal fun PieChart(
    modifier: Modifier = Modifier
) {

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ), label = ""
    )


    var size by remember {
        mutableStateOf(IntSize.Zero)
    }

    val activePieColor = MaterialTheme.colorScheme.primary
    val pieBackgroundColor = MaterialTheme.colorScheme.secondary
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = modifier
                .onSizeChanged {
                    size = it
                }
        ) {
            drawArc(
                color = pieBackgroundColor,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
            )
            drawArc(
                color = activePieColor,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                startAngle = -90f,
                sweepAngle = angle,
                useCenter = true,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewPieChar() {
    WalletTheme {
        PieChart(
            modifier = Modifier.size(300.dp)
        )
    }
}