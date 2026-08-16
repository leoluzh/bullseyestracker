package com.bullseyestracker.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bullseyestracker.ui.theme.DartGold
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

/** Shared dartboard ring emblem used on the splash and home screens. */
@Composable
fun DartboardEmblem(size: Dp = 76.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val radius = size.toPx() / 2f
        drawCircle(color = DartRed, radius = radius, style = Stroke(width = radius * 0.32f))
        drawCircle(color = DartGreen, radius = radius * 0.62f, style = Stroke(width = radius * 0.26f))
        drawCircle(color = DartGold, radius = radius * 0.16f)
    }
}
