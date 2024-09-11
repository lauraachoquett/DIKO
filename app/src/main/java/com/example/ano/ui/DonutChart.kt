package com.example.ano.ui

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AnoTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Component for creating Donut Chart
 * Slices are painted clockwise
 * e.g. 1st input value starts from top to the right, etc
 */

private const val animationDuration = 800
private const val chartDegrees = 360f
private const val emptyIndex = -1
private val defaultSliceWidth = 20.dp
private val defaultSlicePadding = 5.dp
private val defaultSliceClickPadding = 5.dp

@Composable
internal fun DonutChart(
    modifier: Modifier = Modifier,
    listOfDescription : List<String>,
    colors: List<Color>,
    inputValues: List<Float>,
    textColor: Color = MaterialTheme.colorScheme.primary,
    sliceWidthDp: Dp = defaultSliceWidth,
    slicePaddingDp: Dp = defaultSlicePadding,
    sliceClickPaddingDp: Dp = defaultSliceClickPadding,
    animated: Boolean = true
) {

    assert(inputValues.isNotEmpty() && inputValues.size == colors.size) {
        "Input values count must be equal to colors size"
    }

    // calculate each input percentage
    val proportions = inputValues.toPercent()

    // calculate each input slice degrees
    val angleProgress = proportions.map { prop ->
        chartDegrees * prop / 100
    }

    // start drawing clockwise (top to right)
    var startAngle = 270f

    // used for animating each slice
    val pathPortion = remember {
        Animatable(initialValue = 0f)
    }

    // clicked slice in chart
    var clickedItemIndex by remember {
        mutableStateOf(emptyIndex)
    }

    // calculate each slice end point in degrees, for handling click position
    val progressSize = mutableListOf<Float>()

    LaunchedEffect(angleProgress) {
        progressSize.add(angleProgress.first())
        for (x in 1 until angleProgress.size) {
            progressSize.add(angleProgress[x] + progressSize[x - 1])
        }
    }

    val density = LocalDensity.current

    //convert dp values to pixels
    val sliceWidthPx = with(density) { sliceWidthDp.toPx() }
    val slicePaddingPx = with(density) { slicePaddingDp.toPx() }
    val sliceClickPaddingPx = with(density) { sliceClickPaddingDp.toPx() }

    // text style
    val textFontSize = with(density) { 14.dp.toPx() }
    val textPaint = remember {
        Paint().apply {
            color = textColor.toArgb()
            textSize = textFontSize
            textAlign = Paint.Align.CENTER
        }
    }

    // slice width when clicked
    val selectedSliceWidth = sliceWidthPx + sliceClickPaddingPx

    // animate chart slices on composition
    LaunchedEffect(inputValues) {
        pathPortion.animateTo(1f, animationSpec = tween(if (animated) animationDuration else 0))
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {

        val canvasSize = min(constraints.maxWidth, constraints.maxHeight)/2
        val padding = canvasSize * slicePaddingPx / 100f
        val size = Size(canvasSize.toFloat() - padding, canvasSize.toFloat() - padding + 20)
        val canvasSizeDp = with(density) { canvasSize.toDp() }+ 20.dp
        val sliceWidth = with(LocalDensity.current) { 20.dp.toPx() }


        Canvas(
            modifier = Modifier
                .size(canvasSizeDp)
                .pointerInput(inputValues) {

                    detectTapGestures { offset ->
                        val clickedAngle = touchPointToAngle(
                            width = canvasSize.toFloat(),
                            height = canvasSize.toFloat(),
                            touchX = offset.x,
                            touchY = offset.y,
                            chartDegrees = chartDegrees
                        )
                        progressSize.forEachIndexed { index, item ->
                            if (clickedAngle <= item) {
                                clickedItemIndex = index
                                return@detectTapGestures
                            }
                        }
                    }
                }
        ) {

            angleProgress.forEachIndexed { index, angle ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = angle * pathPortion.value,
                    useCenter = false,
                    size = size,
                    style = Stroke(width = if (clickedItemIndex == index) selectedSliceWidth else sliceWidthPx),
                    topLeft = Offset(padding / 2, padding / 2)
                )
                val sliceCenterAngle = startAngle + angle / 2
                val labelRadius = (size.width / 2) + (sliceWidthPx / 2) + 20.dp.toPx() // Adjusted label radius to move it outside the circle
                val sliceCenterX = ((size.width+ 30.dp.toPx())  / 2) + labelRadius * cos(Math.toRadians(sliceCenterAngle.toDouble())).toFloat()
                val sliceCenterY = ((size.height +30.dp.toPx() ) / 2) + labelRadius * sin(Math.toRadians(sliceCenterAngle.toDouble())).toFloat()


                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = Paint().apply {
                        color = textColor.toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 14.sp.toPx() // Adjust text size if needed
                    }
                    drawText(
                        "${inputValues[index].roundToInt()}",
                        sliceCenterX,
                        sliceCenterY,
                        textPaint
                    )
                }
                startAngle += angle
            }
            drawIntoCanvas { canvas ->
                if (clickedItemIndex == emptyIndex){
                    canvas.nativeCanvas.drawText(
                        "Mon apprentissage",
                        (canvasSize / 2) + textFontSize / 4,
                        (canvasSize / 2) + textFontSize / 4,
                        textPaint
                    )
                }
            }



            if (clickedItemIndex != emptyIndex) {
                drawIntoCanvas { canvas ->
                    // Récupérer le texte du tableau et le fractionner sur les '\n'
                    val textToDraw = listOfDescription[clickedItemIndex].split("\n")

                    // Coordonnées du centre
                    val centerX = (canvasSize / 2) + textFontSize / 4
                    var centerY = (canvasSize / 2) + textFontSize / 4

                    // Définir une limite de longueur (par exemple 20 caractères)
                    val maxLineLength = 20

                    textToDraw.forEach { line ->
                        // Si une ligne est trop longue, fractionner en sous-lignes
                        val words = line.chunked(maxLineLength)

                        words.forEach { subLine ->
                            // Dessiner chaque sous-ligne individuellement
                            canvas.nativeCanvas.drawText(
                                subLine,
                                centerX,
                                centerY,
                                textPaint
                            )
                            // Ajouter un décalage pour la ligne suivante
                            centerY += textFontSize + 10f // Ajuste cette valeur pour l'espacement vertical
                        }
                    }
                }
            }
        }
    }

}

internal fun List<Float>.toPercent(): List<Float> {
    return this.map { item ->
        item * 100 / this.sum()
    }
}


internal fun touchPointToAngle(
    width: Float,
    height: Float,
    touchX: Float,
    touchY: Float,
    chartDegrees: Float
): Double {
    val x = touchX - (width * 0.5f)
    val y = touchY - (height * 0.5f)
    var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()) + Math.PI / 2)
    angle = if (angle < 0) angle + chartDegrees else angle
    return angle
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val listOfFloat = listOf(20f,30f,50f)
    val chartColors = listOf(
        Color(0xFF3869B0),
        Color(0xFFE46962),
        Color(0xFF8A4BD3)
    )
    AnoTheme {
        DonutChart(
            inputValues = listOfFloat,
            colors = chartColors,
            listOfDescription = listOf("Journal","Politique")
        )
    }
}