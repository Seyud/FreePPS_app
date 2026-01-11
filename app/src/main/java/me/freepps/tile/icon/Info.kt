// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package me.freepps.tile.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

val MiuixIcons.Info: ImageVector
    get() = MiuixIcons.Regular.Info

val MiuixIcons.Regular.Info: ImageVector
    get() {
        if (_infoRegular != null) return _infoRegular!!
        _infoRegular = ImageVector.Builder(
            name = "Info.Regular",
            defaultWidth = 24.0f.dp,
            defaultHeight = 24.0f.dp,
            viewportWidth = 1225.2f,
            viewportHeight = 1225.2f,
        ).apply {
            group(scaleX = 1.0f, scaleY = -1.0f, translationX = -51.89999999999998f, translationY = 988.1f) {
                addPath(
                    pathData = listOf(
                        PathNode.MoveTo(1175.0f, 375.0f),
                        PathNode.QuadTo(1175.0f, 514.0f, 1106.5f, 631.0f),
                        PathNode.QuadTo(1038.0f, 748.0f, 921.0f, 817.0f),
                        PathNode.QuadTo(804.0f, 886.0f, 665.0f, 886.0f),
                        PathNode.QuadTo(526.0f, 886.0f, 409.0f, 817.0f),
                        PathNode.QuadTo(292.0f, 748.0f, 223.0f, 631.0f),
                        PathNode.QuadTo(154.0f, 514.0f, 154.0f, 375.0f),
                        PathNode.QuadTo(154.0f, 236.0f, 223.0f, 119.0f),
                        PathNode.QuadTo(292.0f, 2.0f, 409.0f, -66.5f),
                        PathNode.QuadTo(526.0f, -135.0f, 665.0f, -135.0f),
                        PathNode.QuadTo(804.0f, -135.0f, 921.0f, -66.5f),
                        PathNode.QuadTo(1038.0f, 2.0f, 1106.5f, 119.0f),
                        PathNode.QuadTo(1175.0f, 236.0f, 1175.0f, 375.0f),
                        PathNode.Close,
                        PathNode.MoveTo(240.0f, 375.0f),
                        PathNode.QuadTo(240.0f, 491.0f, 297.0f, 588.5f),
                        PathNode.QuadTo(354.0f, 686.0f, 451.5f, 743.0f),
                        PathNode.QuadTo(549.0f, 800.0f, 665.0f, 800.0f),
                        PathNode.QuadTo(780.0f, 800.0f, 877.5f, 743.0f),
                        PathNode.QuadTo(975.0f, 686.0f, 1032.5f, 588.5f),
                        PathNode.QuadTo(1090.0f, 491.0f, 1090.0f, 375.0f),
                        PathNode.QuadTo(1090.0f, 259.0f, 1032.5f, 161.5f),
                        PathNode.QuadTo(975.0f, 64.0f, 877.5f, 7.0f),
                        PathNode.QuadTo(780.0f, -50.0f, 665.0f, -50.0f),
                        PathNode.QuadTo(549.0f, -50.0f, 451.5f, 7.0f),
                        PathNode.QuadTo(354.0f, 64.0f, 297.0f, 161.5f),
                        PathNode.QuadTo(240.0f, 259.0f, 240.0f, 375.0f),
                        PathNode.Close,
                        PathNode.MoveTo(719.0f, 611.0f),
                        PathNode.QuadTo(719.0f, 634.0f, 703.5f, 649.5f),
                        PathNode.QuadTo(688.0f, 665.0f, 665.0f, 665.0f),
                        PathNode.QuadTo(642.0f, 665.0f, 626.0f, 649.0f),
                        PathNode.QuadTo(610.0f, 633.0f, 610.0f, 611.0f),
                        PathNode.QuadTo(610.0f, 589.0f, 626.5f, 572.5f),
                        PathNode.QuadTo(643.0f, 556.0f, 665.0f, 556.0f),
                        PathNode.QuadTo(687.0f, 556.0f, 703.0f, 572.0f),
                        PathNode.QuadTo(719.0f, 588.0f, 719.0f, 611.0f),
                        PathNode.Close,
                        PathNode.MoveTo(707.0f, 113.0f),
                        PathNode.VerticalTo(472.0f),
                        PathNode.QuadTo(707.0f, 484.0f, 700.0f, 491.5f),
                        PathNode.QuadTo(693.0f, 499.0f, 678.0f, 499.0f),
                        PathNode.HorizontalTo(650.0f),
                        PathNode.QuadTo(637.0f, 499.0f, 629.5f, 491.0f),
                        PathNode.QuadTo(622.0f, 483.0f, 622.0f, 472.0f),
                        PathNode.VerticalTo(113.0f),
                        PathNode.QuadTo(622.0f, 100.0f, 630.0f, 93.0f),
                        PathNode.QuadTo(638.0f, 86.0f, 651.0f, 86.0f),
                        PathNode.HorizontalTo(679.0f),
                        PathNode.QuadTo(692.0f, 86.0f, 699.5f, 93.0f),
                        PathNode.QuadTo(707.0f, 100.0f, 707.0f, 113.0f),
                        PathNode.Close,
                    ),
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    pathFillType = PathFillType.NonZero,
                )
            }
        }.build()
        return _infoRegular!!
    }

private var _infoRegular: ImageVector? = null
