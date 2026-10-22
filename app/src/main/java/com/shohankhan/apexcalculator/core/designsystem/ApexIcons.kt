package com.shohankhan.apexcalculator.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * A single coherent stroke-based icon family drawn on a 24×24 grid with a 1.8 unit stroke.
 * Keeping the family in-house avoids a heavyweight icon dependency and guarantees that every
 * icon shares the same optical weight, joins and corner radii.
 */
object ApexIcons {

    val Calculator: ImageVector = icon("ApexCalculator") {
        stroke {
            moveTo(6f, 2f); lineTo(18f, 2f); quadTo(20f, 2f, 20f, 4f); lineTo(20f, 20f)
            quadTo(20f, 22f, 18f, 22f); lineTo(6f, 22f); quadTo(4f, 22f, 4f, 20f)
            lineTo(4f, 4f); quadTo(4f, 2f, 6f, 2f); close()
            moveTo(7.5f, 6.5f); lineTo(16.5f, 6.5f)
            moveTo(8.5f, 11f); lineTo(8.6f, 11f)
            moveTo(12f, 11f); lineTo(12.1f, 11f)
            moveTo(15.5f, 11f); lineTo(15.6f, 11f)
            moveTo(8.5f, 15f); lineTo(8.6f, 15f)
            moveTo(12f, 15f); lineTo(12.1f, 15f)
            moveTo(15.5f, 15f); lineTo(15.6f, 15f)
            moveTo(8.5f, 19f); lineTo(8.6f, 19f)
            moveTo(12f, 19f); lineTo(12.1f, 19f)
            moveTo(15.5f, 19f); lineTo(15.6f, 19f)
        }
    }

    val Scientific: ImageVector = icon("ApexScientific") {
        stroke {
            moveTo(2.5f, 12f); lineTo(21.5f, 12f)
            moveTo(3f, 17.5f); curveTo(6f, 6f, 8.5f, 6f, 11f, 12f)
            curveTo(13.5f, 18f, 16.5f, 18f, 20f, 7f)
            moveTo(12f, 9.5f); lineTo(12f, 14.5f)
        }
    }

    val Formula: ImageVector = icon("ApexFormula") {
        stroke {
            moveTo(4f, 4f); lineTo(15f, 4f)
            quadTo(19f, 4f, 19f, 8f); lineTo(19f, 20f)
            lineTo(7f, 20f); quadTo(4f, 20f, 4f, 17f); close()
            moveTo(8f, 4f); lineTo(8f, 20f)
            moveTo(11f, 9f); lineTo(16f, 9f)
            moveTo(11f, 13f); lineTo(16f, 13f)
        }
    }

    val QuickSolve: ImageVector = icon("ApexQuickSolve") {
        fill {
            moveTo(13.5f, 2f); lineTo(5f, 13.5f); lineTo(10.5f, 13.5f); lineTo(9.5f, 22f)
            lineTo(19f, 10f); lineTo(13f, 10f); close()
        }
    }

    val Programmer: ImageVector = icon("ApexProgrammer") {
        stroke {
            moveTo(8f, 6.5f); lineTo(2.5f, 12f); lineTo(8f, 17.5f)
            moveTo(16f, 6.5f); lineTo(21.5f, 12f); lineTo(16f, 17.5f)
            moveTo(14f, 5f); lineTo(10f, 19f)
        }
    }

    val Tools: ImageVector = icon("ApexTools") {
        stroke {
            moveTo(3f, 6f); lineTo(21f, 6f)
            moveTo(3f, 12f); lineTo(21f, 12f)
            moveTo(3f, 18f); lineTo(21f, 18f)
            moveTo(9f, 6f); arcTo(2.4f, 2.4f, 0f, true, true, 9.01f, 6f)
            moveTo(15f, 12f); arcTo(2.4f, 2.4f, 0f, true, true, 15.01f, 12f)
            moveTo(7f, 18f); arcTo(2.4f, 2.4f, 0f, true, true, 7.01f, 18f)
        }
    }

    val History: ImageVector = icon("ApexHistory") {
        stroke {
            moveTo(12f, 3f); arcTo(9f, 9f, 0f, true, true, 11.99f, 3f)
            moveTo(12f, 7f); lineTo(12f, 12.5f); lineTo(16f, 14.5f)
            moveTo(3.2f, 8f); lineTo(3.2f, 3.5f); lineTo(7.5f, 3.5f)
        }
    }

    val Star: ImageVector = icon("ApexStar") {
        fill {
            moveTo(12f, 2.6f); lineTo(14.9f, 8.6f); lineTo(21.4f, 9.5f)
            lineTo(16.7f, 14.1f); lineTo(17.8f, 20.6f); lineTo(12f, 17.5f)
            lineTo(6.2f, 20.6f); lineTo(7.3f, 14.1f); lineTo(2.6f, 9.5f)
            lineTo(9.1f, 8.6f); close()
        }
    }

    val StarOutline: ImageVector = icon("ApexStarOutline") {
        stroke {
            moveTo(12f, 2.6f); lineTo(14.9f, 8.6f); lineTo(21.4f, 9.5f)
            lineTo(16.7f, 14.1f); lineTo(17.8f, 20.6f); lineTo(12f, 17.5f)
            lineTo(6.2f, 20.6f); lineTo(7.3f, 14.1f); lineTo(2.6f, 9.5f)
            lineTo(9.1f, 8.6f); close()
        }
    }

    val Settings: ImageVector = icon("ApexSettings") {
        stroke {
            moveTo(12f, 8.5f); arcTo(3.5f, 3.5f, 0f, true, true, 12.01f, 8.5f)
            moveTo(12f, 2.5f); lineTo(12f, 5f)
            moveTo(12f, 19f); lineTo(12f, 21.5f)
            moveTo(4.2f, 7f); lineTo(6.4f, 8.3f)
            moveTo(17.6f, 15.7f); lineTo(19.8f, 17f)
            moveTo(4.2f, 17f); lineTo(6.4f, 15.7f)
            moveTo(17.6f, 8.3f); lineTo(19.8f, 7f)
        }
    }

    val Info: ImageVector = icon("ApexInfo") {
        stroke {
            moveTo(12f, 3f); arcTo(9f, 9f, 0f, true, true, 12.01f, 3f)
            moveTo(12f, 11f); lineTo(12f, 17f)
            moveTo(12f, 7.6f); lineTo(12.1f, 7.6f)
        }
    }

    val Search: ImageVector = icon("ApexSearch") {
        stroke {
            moveTo(10.5f, 3.5f); arcTo(6.5f, 6.5f, 0f, true, true, 10.51f, 3.5f)
            moveTo(15.3f, 15.3f); lineTo(21f, 21f)
        }
    }

    val Close: ImageVector = icon("ApexClose") {
        stroke {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Back: ImageVector = icon("ApexBack") {
        stroke {
            moveTo(15f, 4.5f); lineTo(7.5f, 12f); lineTo(15f, 19.5f)
        }
    }

    val ChevronRight: ImageVector = icon("ApexChevronRight") {
        stroke {
            moveTo(9f, 4.5f); lineTo(16.5f, 12f); lineTo(9f, 19.5f)
        }
    }

    val Copy: ImageVector = icon("ApexCopy") {
        stroke {
            moveTo(9f, 9f); lineTo(19f, 9f); quadTo(21f, 9f, 21f, 11f); lineTo(21f, 21f)
            quadTo(21f, 23f, 19f, 23f); lineTo(9f, 23f); quadTo(7f, 23f, 7f, 21f)
            lineTo(7f, 11f); quadTo(7f, 9f, 9f, 9f); close()
            moveTo(15f, 6f); lineTo(15f, 4f); quadTo(15f, 2f, 13f, 2f); lineTo(5f, 2f)
            quadTo(3f, 2f, 3f, 4f); lineTo(3f, 14f); quadTo(3f, 16f, 5f, 16f); lineTo(7f, 16f)
        }
    }

    val Share: ImageVector = icon("ApexShare") {
        stroke {
            moveTo(18f, 3f); arcTo(2.6f, 2.6f, 0f, true, true, 18.01f, 3f)
            moveTo(6f, 9.4f); arcTo(2.6f, 2.6f, 0f, true, true, 6.01f, 9.4f)
            moveTo(18f, 15.8f); arcTo(2.6f, 2.6f, 0f, true, true, 18.01f, 15.8f)
            moveTo(8.3f, 10.8f); lineTo(15.7f, 6.5f)
            moveTo(8.3f, 13.2f); lineTo(15.7f, 17.5f)
        }
    }

    val Check: ImageVector = icon("ApexCheck") {
        stroke {
            moveTo(4.5f, 12.5f); lineTo(9.5f, 17.5f); lineTo(19.5f, 6.5f)
        }
    }

    val Add: ImageVector = icon("ApexAdd") {
        stroke {
            moveTo(12f, 4.5f); lineTo(12f, 19.5f)
            moveTo(4.5f, 12f); lineTo(19.5f, 12f)
        }
    }

    val Delete: ImageVector = icon("ApexDelete") {
        stroke {
            moveTo(9f, 4f); lineTo(20f, 4f); quadTo(22f, 4f, 22f, 6f); lineTo(22f, 18f)
            quadTo(22f, 20f, 20f, 20f); lineTo(9f, 20f); lineTo(2.5f, 12f); close()
            moveTo(12.5f, 9f); lineTo(18.5f, 15f)
            moveTo(18.5f, 9f); lineTo(12.5f, 15f)
        }
    }

    val More: ImageVector = icon("ApexMore") {
        fill {
            moveTo(5f, 12f); arcTo(1.6f, 1.6f, 0f, true, true, 5.01f, 12f)
            moveTo(12f, 12f); arcTo(1.6f, 1.6f, 0f, true, true, 12.01f, 12f)
            moveTo(19f, 12f); arcTo(1.6f, 1.6f, 0f, true, true, 19.01f, 12f)
        }
    }

    val Grid: ImageVector = icon("ApexGrid") {
        stroke {
            moveTo(4f, 4f); lineTo(20f, 4f); lineTo(20f, 20f); lineTo(4f, 20f); close()
            moveTo(4f, 9.3f); lineTo(20f, 9.3f)
            moveTo(4f, 14.7f); lineTo(20f, 14.7f)
            moveTo(9.3f, 4f); lineTo(9.3f, 20f)
            moveTo(14.7f, 4f); lineTo(14.7f, 20f)
        }
    }

    val Calendar: ImageVector = icon("ApexCalendar") {
        stroke {
            moveTo(5f, 5.5f); lineTo(19f, 5.5f); quadTo(21f, 5.5f, 21f, 7.5f)
            lineTo(21f, 19f); quadTo(21f, 21f, 19f, 21f); lineTo(5f, 21f)
            quadTo(3f, 21f, 3f, 19f); lineTo(3f, 7.5f); quadTo(3f, 5.5f, 5f, 5.5f); close()
            moveTo(3f, 10.5f); lineTo(21f, 10.5f)
            moveTo(8f, 2.5f); lineTo(8f, 7f)
            moveTo(16f, 2.5f); lineTo(16f, 7f)
            moveTo(7.5f, 14.5f); lineTo(8f, 14.5f)
            moveTo(11.75f, 14.5f); lineTo(12.25f, 14.5f)
            moveTo(16f, 14.5f); lineTo(16.5f, 14.5f)
            moveTo(7.5f, 18f); lineTo(8f, 18f)
            moveTo(11.75f, 18f); lineTo(12.25f, 18f)
        }
    }

    val Memory: ImageVector = icon("ApexMemory") {
        stroke {
            moveTo(7f, 7f); lineTo(17f, 7f); quadTo(18f, 7f, 18f, 8f); lineTo(18f, 16f)
            quadTo(18f, 17f, 17f, 17f); lineTo(7f, 17f); quadTo(6f, 17f, 6f, 16f)
            lineTo(6f, 8f); quadTo(6f, 7f, 7f, 7f); close()
            moveTo(4f, 10f); lineTo(6f, 10f); moveTo(4f, 14f); lineTo(6f, 14f)
            moveTo(18f, 10f); lineTo(20f, 10f); moveTo(18f, 14f); lineTo(20f, 14f)
            moveTo(9.5f, 4f); lineTo(9.5f, 7f); moveTo(14.5f, 4f); lineTo(14.5f, 7f)
            moveTo(9.5f, 17f); lineTo(9.5f, 20f); moveTo(14.5f, 17f); lineTo(14.5f, 20f)
            moveTo(10f, 12f); lineTo(14f, 12f)
        }
    }

    val Undo: ImageVector = icon("ApexUndo") {
        stroke {
            moveTo(4f, 10f); lineTo(9f, 5f)
            moveTo(4f, 10f); lineTo(9f, 15f)
            moveTo(4f, 10f); lineTo(14f, 10f)
            quadTo(20f, 10f, 20f, 15f); quadTo(20f, 20f, 14f, 20f); lineTo(10f, 20f)
        }
    }

    val Percent: ImageVector = icon("ApexPercent") {
        stroke {
            moveTo(6f, 6.5f); arcTo(2f, 2f, 0f, true, true, 6.01f, 6.5f)
            moveTo(18f, 17.5f); arcTo(2f, 2f, 0f, true, true, 18.01f, 17.5f)
            moveTo(19f, 5f); lineTo(5f, 19f)
        }
    }

    val Sigma: ImageVector = icon("ApexSigma") {
        stroke {
            moveTo(18f, 4.5f); lineTo(6.5f, 4.5f); lineTo(13f, 12f); lineTo(6.5f, 19.5f); lineTo(18f, 19.5f)
        }
    }

    val Warning: ImageVector = icon("ApexWarning") {
        stroke {
            moveTo(12f, 3.5f); lineTo(21.5f, 20f); lineTo(2.5f, 20f); close()
            moveTo(12f, 9f); lineTo(12f, 14f)
            moveTo(12f, 17f); lineTo(12.1f, 17f)
        }
    }

    val Refresh: ImageVector = icon("ApexRefresh") {
        stroke {
            moveTo(20f, 12f); arcTo(8f, 8f, 0f, false, true, 4.5f, 15f)
            moveTo(4f, 12f); arcTo(8f, 8f, 0f, false, true, 19.5f, 9f)
            moveTo(20.5f, 4.5f); lineTo(20.5f, 9.5f); lineTo(15.5f, 9.5f)
            moveTo(3.5f, 19.5f); lineTo(3.5f, 14.5f); lineTo(8.5f, 14.5f)
        }
    }

    private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply(block).build()

    private fun ImageVector.Builder.stroke(block: PathBuilder.() -> Unit) {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
    }

    private fun ImageVector.Builder.fill(block: PathBuilder.() -> Unit) {
        path(
            fill = SolidColor(Color.Black),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.2f,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
    }
}
