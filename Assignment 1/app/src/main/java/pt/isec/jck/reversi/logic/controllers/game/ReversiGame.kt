package pt.isec.jck.reversi.logic.controllers.game

import pt.isec.jck.reversi.logic.models.game.*
import pt.isec.jck.reversi.logic.models.player.IPlayer
import pt.isec.jck.reversi.logic.models.player.PlayerData
import java.util.*
import kotlin.collections.ArrayList

open class ReversiGame(
    gameMode: GameMode,
    private var gameSettings: GameSettings
) : IGame {

    //region Game properties
    private var board = Board(gameMode)

    private val players = ArrayList<IPlayer>()
    private val random = Random()
    private var currentPlayer = random.nextInt(gameMode.numPlayers)

    private var isReplacingPieces = false
    private val replacePieces = ArrayList<Piece>()
    protected var hasGameEnded = false
        private set
    //endregion

    //region Callback properties
    private var onAddPlayer: ((player: IPlayer) -> Unit)? = null
    private var onRemovePlayer: ((player: IPlayer) -> Unit)? = null
    private var onPlayerToggleReady: ((player: IPlayer) -> Unit)? = null
    private var onShowPlaceablePieces: ((pieces: List<Piece>) -> Unit)? = null
    private var onClearPlaceablePieces: (() -> Unit)? = null
    private var onPlacePiece: ((piece: Piece) -> Unit)? = null
    private var onPlaceBomb: ((piece: Piece) -> Unit)? = null
    private var onBeginReplacePiece: (() -> Unit)? = null
    private var onAddReplacePiece: ((piece: Piece) -> Unit)? = null
    private var onRemoveReplacePiece: ((piece: Piece) -> Unit)? = null
    private var onEndReplacePiece: ((pieces: List<Piece>?) -> Unit)? = null
    private var onPlayerScoreUpdated: ((player: IPlayer) -> Unit)? = null
    private var onNextPlayer: ((oldPlayer: IPlayer, newPlayer: IPlayer) -> Unit)? = null
    private var onPlayerSkip: ((player: IPlayer) -> Unit)? = null
    private var onGameReady: (() -> Unit)? = null
    private var onGameOver: ((winner: IPlayer?) -> Unit)? = null
    private var onPlayerBombStatusChange: ((player: IPlayer) -> Unit)? = null
    private var onPlayerReplaceStatusChange: ((player: IPlayer) -> Unit)? = null
    private var onGameDead: (() -> Unit)? = null
    //endregion

    //region Callback functions
    override fun setOnAddPlayer(callback: ((player: IPlayer) -> Unit)?) {
        onAddPlayer = callback
    }

    override fun setOnRemovePlayer(callback: ((player: IPlayer) -> Unit)?) {
        onRemovePlayer = callback
    }

    override fun setOnPlayerToggleReady(callback: ((player: IPlayer) -> Unit)?) {
        onPlayerToggleReady = callback
    }

    override fun setOnShowPlaceablePieces(callback: ((pieces: List<Piece>) -> Unit)?) {
        if (gameSettings.showPlaceable)
            onShowPlaceablePieces = callback
    }

    override fun setOnClearPlaceablePieces(callback: (() -> Unit)?) {
        if (gameSettings.showPlaceable)
            onClearPlaceablePieces = callback
    }

    override fun setOnPlacePiece(callback: ((piece: Piece) -> Unit)?) {
        onPlacePiece = callback
    }

    override fun setOnPlaceBomb(callback: ((piece: Piece) -> Unit)?) {
        if (gameSettings.specialPieces)
            onPlaceBomb = callback
    }

    override fun setOnBeginReplacePiece(callback: (() -> Unit)?) {
        if (gameSettings.specialPieces)
            onBeginReplacePiece = callback
    }

    override fun setOnAddReplacePiece(callback: ((piece: Piece) -> Unit)?) {
        if (gameSettings.specialPieces)
            onAddReplacePiece = callback
    }

    override fun setOnRemoveReplacePiece(callback: ((piece: Piece) -> Unit)?) {
        if (gameSettings.specialPieces)
            onRemoveReplacePiece = callback
    }

    override fun setOnEndReplacePiece(callback: ((pieces: List<Piece>?) -> Unit)?) {
        if (gameSettings.specialPieces)
            onEndReplacePiece = callback
    }

    override fun setOnPlayerScoreUpdated(callback: ((player: IPlayer) -> Unit)?) {
        onPlayerScoreUpdated = callback
    }

    override fun setOnNextPlayer(callback: ((oldPlayer: IPlayer, newPlayer: IPlayer) -> Unit)?) {
        onNextPlayer = callback
    }

    override fun setOnPlayerSkip(callback: ((player: IPlayer) -> Unit)?) {
        onPlayerSkip = callback
    }

    override fun setOnGameReady(callback: (() -> Unit)?) {
        onGameReady = callback
    }

    override fun setOnGameOver(callback: ((winner: IPlayer?) -> Unit)?) {
        onGameOver = callback
    }

    override fun setOnPlayerBombStatusChange(callback: ((player: IPlayer) -> Unit)?) {
        if (gameSettings.specialPieces)
            onPlayerBombStatusChange = callback
    }

    override fun setOnPlayerReplaceStatusChange(callback: ((player: IPlayer) -> Unit)?) {
        if (gameSettings.specialPieces)
            onPlayerReplaceStatusChange = callback
    }

    override fun setOnGameDead(callback: (() -> Unit)?) {
        onGameDead = callback
    }

    override fun initGameView(player: IPlayer?) {
        board.board.flatten()
            .filter { it.disc != Disc.Empty }
            .forEach {
                invokePlacePiece(it)
            }
        players.forEach {
            updateScore(it, board.countPieces(it.getDisc()))
            invokePlayerBombStatusChange(it)
            invokePlayerReplaceStatusChange(it)
        }
        val curPlayer = players[currentPlayer]
        if (player == null || player == curPlayer)
            invokeShowPlaceablePieces(board.getPlaceablePieces(curPlayer.getDisc()))
        invokeNextPlayer(curPlayer, curPlayer)
        replacePieces.forEach {
            invokeAddReplacePiece(it)
        }
    }

    protected fun checkIsGameReady() {
        if (isGameReady()) {
            onGameReady?.invoke()
        }
    }

    protected fun invokeShowPlaceablePieces(pieces: List<Piece>) {
        onShowPlaceablePieces?.invoke(pieces)
    }

    protected fun invokeClearPlaceablePieces() {
        onClearPlaceablePieces?.invoke()
    }

    protected fun invokePlacePiece(piece: Piece) {
        onPlacePiece?.invoke(piece)
    }

    protected fun invokePlaceBomb(piece: Piece) {
        onPlaceBomb?.invoke(piece)
    }

    protected fun invokeBeginReplacePiece() {
        onBeginReplacePiece?.invoke()
    }

    protected fun invokeAddReplacePiece(piece: Piece) {
        onAddReplacePiece?.invoke(piece)
    }

    protected fun invokeRemoveReplacePiece(piece: Piece) {
        onRemoveReplacePiece?.invoke(piece)
    }

    protected fun invokeEndReplacePiece(pieces: List<Piece>?) {
        onEndReplacePiece?.invoke(pieces)
    }

    protected fun invokePlayerScoreUpdated(player: IPlayer) {
        onPlayerScoreUpdated?.invoke(player)
    }

    protected fun invokeNextPlayer(oldPlayer: IPlayer, newPlayer: IPlayer) {
        onNextPlayer?.invoke(oldPlayer, newPlayer)
    }

    protected fun invokePlayerSkip(player: IPlayer) {
        onPlayerSkip?.invoke(player)
    }

    protected fun invokeGameOver(winner: IPlayer?) {
        hasGameEnded = true
        onGameOver?.invoke(winner)
    }

    protected fun invokePlayerBombStatusChange(player: IPlayer) {
        onPlayerBombStatusChange?.invoke(player)
    }

    protected fun invokePlayerReplaceStatusChange(player: IPlayer) {
        onPlayerReplaceStatusChange?.invoke(player)
    }

    protected fun invokeGameDead() {
        onGameDead?.invoke()
    }

    protected fun resetOnGameDead() {
        onGameDead = null
    }
    //endregion

    override fun getGameMode(): GameMode {
        return board.gameMode
    }

    override fun getGameSettings(): GameSettings {
        return gameSettings
    }

    override fun getPlayerCount(): Int {
        return players.size
    }

    override fun getPlayerByIndex(index: Int): IPlayer? {
        if (index < 0 || index > getPlayerCount())
            return null
        return players[index]
    }

    override fun getPlayerByDisc(disc: Disc): IPlayer? {
        return players.find { it.getDisc() == disc }
    }

    override fun getPlayerByPlayerData(playerData: PlayerData): IPlayer? {
        return players.find { it.getPlayerData() == playerData }
    }

    override fun getAllPlayers(): List<IPlayer> {
        return players.toList()
    }

    override fun getCurrentPlayer(): IPlayer {
        return players[currentPlayer]
    }

    private fun getPlayerIndexByDisc(disc: Disc): Int {
        val player = getPlayerByDisc(disc)
        return players.indexOf(player)
    }

    protected fun setCurrentPlayer(disc: Disc) {
        val index = getPlayerIndexByDisc(disc)
        if (index == -1)
            return
        currentPlayer = index
    }

    override fun setNextPlayer() {
        val oldPlayer = players[currentPlayer]
        currentPlayer = (currentPlayer + 1) % board.gameMode.numPlayers
        val newPlayer = players[currentPlayer]
        players.forEach { updateScore(it, board.countPieces(it.getDisc())) }
        invokeNextPlayer(oldPlayer, newPlayer)
        invokeShowPlaceablePieces(board.getPlaceablePieces(newPlayer.getDisc()))

        if (!board.canPlacePiece(newPlayer.getDisc())) {
            invokeClearPlaceablePieces()
            if (gameSettings.autoSkip) {
                setNextPlayer()
            } else {
                invokePlayerSkip(newPlayer)
            }
        }
    }

    protected fun updateScore(player: IPlayer, score: Int) {
        player.setScore(score)
        invokePlayerScoreUpdated(player)
    }

    override fun addPlayer(player: IPlayer): Boolean {
        if (players.contains(player) || players.size == board.gameMode.numPlayers)
            return false
        val added = players.add(player)
        if (added)
            onAddPlayer?.invoke(player)
        return added
    }

    override fun removePlayer(player: IPlayer): Boolean {
        val removed = players.remove(player)
        if (removed)
            onRemovePlayer?.invoke(player)
        return removed
    }

    override fun togglePlayerReady(disc: Disc) {
        val player = getPlayerByDisc(disc) ?: return
        player.toggleReady()
        onPlayerToggleReady?.invoke(player)
        checkIsGameReady()
    }

    override fun isReplacingPieces(): Boolean {
        return isReplacingPieces
    }

    override fun isGameReady(): Boolean {
        return (players.size == board.gameMode.numPlayers && areAllPlayersReady())
    }

    private fun areAllPlayersReady(): Boolean {
        return (players.count { it.isReady() } == board.gameMode.numPlayers)
    }

    override fun isGameOver(): Boolean {
        return board.isGameOver()
    }

    override fun isOwnPiece(x: Int, y: Int): Boolean {
        return board.getDiscOnPosition(x, y) == players[currentPlayer].getDisc()
    }

    override fun isEmptyPiece(x: Int, y: Int): Boolean {
        return board.getDiscOnPosition(x, y) == Disc.Empty
    }

    override fun isAdversaryPiece(x: Int, y: Int): Boolean {
        return !isOwnPiece(x, y) && !isEmptyPiece(x, y)
    }

    override fun placePiece(x: Int, y: Int) {
        if (hasGameEnded)
            return

        if (isReplacingPieces()) {
            if (isPieceOnReplaceList(x, y)) {
                removeReplacePiece(x, y)
            } else {
                addReplacePiece(x, y)
            }
            return
        }

        val player = players[currentPlayer]
        val piece = Piece(x, y, player.getDisc())

        if (!board.canPlace(piece)) return
        invokeClearPlaceablePieces()

        placePiece(piece)
        board.getReversiblePieces(piece).forEach { placePiece(it) }

        if (isGameOver()) {
            players.forEach { updateScore(it, board.countPieces(it.getDisc())) }
            val winner = players.maxByOrNull { it.getScore().score }

            if (winner != null && players.count { winner.getScore().score == it.getScore().score } > 1)
            // Draw (No winner or winner's score shows up more than once...)
                invokeGameOver(null)
            else
                invokeGameOver(winner)

            return
        }

        setNextPlayer()
    }

    protected fun placePiece(piece: Piece) {
        board.placePiece(piece)
        invokePlacePiece(piece)
    }

    override fun placeBomb(x: Int, y: Int): Boolean {
        if (hasGameEnded)
            return false

        if (!gameSettings.specialPieces || isReplacingPieces())
            return false

        if (isOwnPiece(x, y)) {
            addReplacePiece(x, y)
            return true
        }

        val curPlayer = players[currentPlayer]
        val curPlayerDisc = curPlayer.getDisc()

        val piece = Piece(x, y, curPlayerDisc)
        if (!curPlayer.canPlaceBomb() || !board.canPlace(piece)) return true
        invokeClearPlaceablePieces()

        placeBomb(piece)
        if (!gameSettings.infiniteSpecialPieces)
            curPlayer.disablePlaceBomb()
        invokePlayerBombStatusChange(curPlayer)

        if (board.isGameOver()) {
            players.forEach { updateScore(it, board.countPieces(it.getDisc())) }
            val winner = players.maxByOrNull { it.getScore().score }

            if (winner != null && players.count { winner.getScore().score == it.getScore().score } > 1)
            // Draw (No winner or winner's score shows up more than once...)
                invokeGameOver(null)
            else
                invokeGameOver(winner)

            return true
        }

        setNextPlayer()

        return true
    }

    protected fun placeBomb(piece: Piece) {
        board.placeBomb(piece)
        invokePlaceBomb(piece)
    }

    override fun beginReplacePiece() {
        clearReplaceList()
        isReplacingPieces = true
        invokeBeginReplacePiece()
    }

    override fun addReplacePiece(x: Int, y: Int): Boolean {
        val player = players[currentPlayer]
        if (!player.canReplacePieces())
            return false

        if (replacePieces.size == 0)
            beginReplacePiece()

        if (replacePieces.size in 0..1 && isOwnPiece(x, y)) {
            val piece = Piece(x, y, player.getDisc())
            addPieceToList(piece)
            return true
        }
        if (replacePieces.size == 2 && isAdversaryPiece(x, y)) {
            val piece = Piece(x, y, board.getDiscOnPosition(x, y))
            addPieceToList(piece)
            endReplacePiece()
            return true
        }
        return false
    }

    protected fun addPieceToList(piece: Piece) {
        replacePieces.add(piece)
        invokeAddReplacePiece(piece)
    }

    override fun isPieceOnReplaceList(x: Int, y: Int): Boolean {
        val piece = Piece(x, y, players[currentPlayer].getDisc())
        return replacePieces.contains(piece)
    }

    override fun removeReplacePiece(x: Int, y: Int): Boolean {
        if (replacePieces.size in 1..2) {
            val piece = Piece(x, y, players[currentPlayer].getDisc())
            val temp = removePieceFromList(piece)
            if (replacePieces.size == 0)
                endReplacePiece()
            return temp
        }

        return false
    }

    protected fun removePieceFromList(piece: Piece): Boolean {
        val temp = replacePieces.remove(piece)
        if (temp)
            invokeRemoveReplacePiece(piece)
        return temp
    }

    override fun endReplacePiece() {
        if (replacePieces.size == 3) {
            val curPlayer = players[currentPlayer]
            invokeClearPlaceablePieces()

            replacePieces(replacePieces)
            if (!gameSettings.infiniteSpecialPieces)
                curPlayer.disableReplacePieces()
            invokePlayerReplaceStatusChange(curPlayer)

            if (board.isGameOver()) {
                clearReplaceList()
                players.forEach { updateScore(it, board.countPieces(it.getDisc())) }
                val winner = players.maxByOrNull { it.getScore().score }

                if (winner != null && players.count { winner.getScore().score == it.getScore().score } > 1)
                // Draw (No winner or winner's score shows up more than once...)
                    invokeGameOver(null)
                else
                    invokeGameOver(winner)
                return
            }

            setNextPlayer()
        } else {
            invokeEndReplacePiece(null)
        }
        clearReplaceList()
    }

    protected fun replacePieces(pieces: List<Piece>?) {
        if (pieces == null) {
            invokeEndReplacePiece(null)
            return
        }
        board.replaceDiscs(pieces[0], pieces[1], pieces[2])
        invokeEndReplacePiece(pieces.toList())
    }

    protected fun clearReplaceList() {
        isReplacingPieces = false
        replacePieces.clear()
    }

    protected fun setGameSettings(gameSettings: GameSettings) {
        this.gameSettings = gameSettings
    }

    protected fun setGameMode(gameMode: GameMode) {
        board = Board(gameMode)
    }
}
