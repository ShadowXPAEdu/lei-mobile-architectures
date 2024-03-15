package pt.isec.jck.reversi.logic.controllers.player

import pt.isec.jck.reversi.logic.models.game.Disc
import pt.isec.jck.reversi.logic.models.player.Player
import pt.isec.jck.reversi.logic.models.player.PlayerData
import pt.isec.jck.reversi.logic.models.player.Score

open class HumanPlayer(
    playerData: PlayerData,
    disc: Disc,
    usingSpecialPieces: Boolean,
    private var ready: Boolean = false
) : Player(playerData, disc, Score(0), usingSpecialPieces) {

    override fun isReady(): Boolean {
        return ready
    }

    override fun toggleReady() {
        ready = !ready
    }
}
