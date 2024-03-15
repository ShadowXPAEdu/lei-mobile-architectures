package pt.isec.jck.reversi.logic.models.game

enum class GameMode(val numPlayers: Int, val boardSize: Int, val online: Boolean) {
    Offline(2, 8, false),
    Online(2, 8, true),
    Online3(3, 10, true)
}
