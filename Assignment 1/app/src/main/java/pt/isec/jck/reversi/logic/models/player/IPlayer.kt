package pt.isec.jck.reversi.logic.models.player

import android.graphics.Bitmap
import pt.isec.jck.reversi.logic.models.game.Disc

interface IPlayer {
    fun getDisc(): Disc
    fun setDisc(disc: Disc)
    fun getPlayerData(): PlayerData
    fun getUsername(): String
    fun getAvatar(): Bitmap
    fun getScore(): Score
    fun canPlaceBomb(): Boolean
    fun canReplacePieces(): Boolean
    fun setUsingSpecialPieces(usingSpecialPieces: Boolean)
    fun isReady(): Boolean
    fun toggleReady()
    fun disablePlaceBomb()
    fun disableReplacePieces()
    fun setScore(score: Int)
    fun setCanPlaceBomb(canPlaceBomb: Boolean)
    fun setCanReplacePieces(canReplacePieces: Boolean)
}
