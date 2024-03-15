package pt.isec.jck.reversi.logic.models.game

import android.graphics.Color

enum class Disc(val color: Int) {
    // Special Discs
    Empty(Color.TRANSPARENT),

    // Normal Discs
    Black(Color.BLACK),
    White(Color.WHITE),
    Gold(Color.argb(255, 255, 215, 0));

    open fun adversary(): List<Disc> {
        return values().filter { d ->
            d != Empty && d != this
        }
    }
}
