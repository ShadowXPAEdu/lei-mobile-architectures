package pt.isec.jck.reversi.logic.models.player

import android.graphics.Bitmap
import pt.isec.jck.reversi.logic.models.game.Disc

abstract class Player(
    private val playerData: PlayerData,
    private var disc: Disc,
    private var score: Score,
    usingSpecialPieces: Boolean,
    private var canPlaceBomb: Boolean = usingSpecialPieces,
    private var canReplacePieces: Boolean = usingSpecialPieces
) : IPlayer {

    override fun getDisc(): Disc {
        return disc
    }

    override fun setDisc(disc: Disc) {
        this.disc = disc
    }

    override fun getPlayerData(): PlayerData {
        return playerData
    }

    override fun getUsername(): String {
        return playerData.username
    }

    override fun getAvatar(): Bitmap {
        return playerData.avatar
    }

    override fun getScore(): Score {
        return score
    }

    override fun canPlaceBomb(): Boolean {
        return canPlaceBomb
    }

    override fun canReplacePieces(): Boolean {
        return canReplacePieces
    }

    override fun setUsingSpecialPieces(usingSpecialPieces: Boolean) {
        setCanPlaceBomb(usingSpecialPieces)
        setCanReplacePieces(usingSpecialPieces)
    }

    override fun setCanPlaceBomb(canPlaceBomb: Boolean) {
        this.canPlaceBomb = canPlaceBomb
    }

    override fun setCanReplacePieces(canReplacePieces: Boolean) {
        this.canReplacePieces = canReplacePieces
    }

    override fun disablePlaceBomb() {
        canPlaceBomb = false
    }

    override fun disableReplacePieces() {
        canReplacePieces = false
    }

    override fun setScore(score: Int) {
        this.score.score = score
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Player) return false

        if (playerData == other.playerData || disc == other.disc) return true // == because we don't want players with the same disc =)

        return false
    }

    override fun hashCode(): Int {
        var result = playerData.hashCode()
        result = 31 * result + disc.hashCode()
        result = 31 * result + score.hashCode()
        result = 31 * result + canPlaceBomb.hashCode()
        result = 31 * result + canReplacePieces.hashCode()
        return result
    }
}
