package pt.isec.jck.reversi.logic.models.game

import pt.isec.jck.reversi.logic.models.player.IPlayer
import pt.isec.jck.reversi.logic.models.player.PlayerData

interface IGame {

    // Game callbacks
    fun setOnAddPlayer(callback: ((player: IPlayer) -> Unit)?)
    fun setOnRemovePlayer(callback: ((player: IPlayer) -> Unit)?)
    fun setOnPlayerToggleReady(callback: ((player: IPlayer) -> Unit)?)

    fun setOnShowPlaceablePieces(callback: ((pieces: List<Piece>) -> Unit)?)
    fun setOnClearPlaceablePieces(callback: (() -> Unit)?)
    fun setOnPlacePiece(callback: ((piece: Piece) -> Unit)?)
    fun setOnPlaceBomb(callback: ((piece: Piece) -> Unit)?)
    fun setOnBeginReplacePiece(callback: (() -> Unit)?)
    fun setOnAddReplacePiece(callback: ((piece: Piece) -> Unit)?)
    fun setOnRemoveReplacePiece(callback: ((piece: Piece) -> Unit)?)
    fun setOnEndReplacePiece(callback: ((pieces: List<Piece>?) -> Unit)?)
    fun setOnPlayerScoreUpdated(callback: ((player: IPlayer) -> Unit)?)
    fun setOnNextPlayer(callback: ((oldPlayer: IPlayer, newPlayer: IPlayer) -> Unit)?)
    fun setOnPlayerSkip(callback: ((player: IPlayer) -> Unit)?)
    fun setOnGameReady(callback: (() -> Unit)?)
    fun setOnGameOver(callback: ((winner: IPlayer?) -> Unit)?)

    fun setOnPlayerBombStatusChange(callback: ((player: IPlayer) -> Unit)?)
    fun setOnPlayerReplaceStatusChange(callback: ((player: IPlayer) -> Unit)?)

    fun setOnGameDead(callback: (() -> Unit)?)

    // View functions
    fun initGameView(player: IPlayer? = null)

    // Game functions
    fun getGameMode(): GameMode
    fun getGameSettings(): GameSettings
    fun getPlayerCount(): Int
    fun getPlayerByIndex(index: Int): IPlayer?
    fun getPlayerByDisc(disc: Disc): IPlayer?
    fun getPlayerByPlayerData(playerData: PlayerData): IPlayer?
    fun getAllPlayers(): List<IPlayer>
    fun getCurrentPlayer(): IPlayer
    fun setNextPlayer()
    fun addPlayer(player: IPlayer): Boolean
    fun removePlayer(player: IPlayer): Boolean

    fun togglePlayerReady(disc: Disc)

    fun isReplacingPieces(): Boolean

    fun isGameReady(): Boolean
    fun isGameOver(): Boolean

    // Board functions
    fun isOwnPiece(x: Int, y: Int): Boolean
    fun isEmptyPiece(x: Int, y: Int): Boolean
    fun isAdversaryPiece(x: Int, y: Int): Boolean

    fun placePiece(x: Int, y: Int)
    fun placeBomb(x: Int, y: Int): Boolean

    fun beginReplacePiece()
    fun addReplacePiece(x: Int, y: Int): Boolean
    fun isPieceOnReplaceList(x: Int, y: Int): Boolean
    fun removeReplacePiece(x: Int, y: Int): Boolean
    fun endReplacePiece()
}
