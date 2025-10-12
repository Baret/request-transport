package de.gleex.reqtrans.model

import ktx.math.ImmutableVector2
import ktx.math.dst

sealed interface Building {
    val position: ImmutableVector2

    fun distanceTo(other: Building) = position.dst(other.position)

    companion object {
        const val WIDTH = 10f
        const val HEIGHT = 10f
    }
}

data class House(override val position: ImmutableVector2): Building

data class Workplace(override val position: ImmutableVector2): Building
