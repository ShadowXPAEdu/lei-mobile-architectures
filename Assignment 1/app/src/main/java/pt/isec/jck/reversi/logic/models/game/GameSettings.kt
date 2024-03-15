package pt.isec.jck.reversi.logic.models.game

data class GameSettings(
    val ip: String = "127.0.0.1",
    val port: Int = 9999,
    val specialPieces: Boolean,
    val autoSkip: Boolean,
    val showPlaceable: Boolean,
    val infiniteSpecialPieces: Boolean = false
)
