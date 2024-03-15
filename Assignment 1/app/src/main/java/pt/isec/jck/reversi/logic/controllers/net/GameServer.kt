package pt.isec.jck.reversi.logic.controllers.net

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.TaskCompletionSource
import org.json.JSONArray
import org.json.JSONObject
import pt.isec.jck.reversi.logic.controllers.game.ReversiGame
import pt.isec.jck.reversi.logic.controllers.player.NetworkPlayer
import pt.isec.jck.reversi.logic.controllers.utils.getExternalIP
import pt.isec.jck.reversi.logic.controllers.utils.toBase64
import pt.isec.jck.reversi.logic.controllers.utils.toBitmap
import pt.isec.jck.reversi.logic.models.game.Disc
import pt.isec.jck.reversi.logic.models.game.Piece
import pt.isec.jck.reversi.logic.models.net.Command
import pt.isec.jck.reversi.logic.models.player.IPlayer
import pt.isec.jck.reversi.logic.models.player.PlayerData
import java.net.ServerSocket
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class GameServer(private val game: ReversiGame, private val defaultTimeout: Int) {

    private var svSocket: ServerSocket? = null
    private var keepAccepting: Boolean = true

    fun initServer(serverCreated: TaskCompletionSource<Unit>) {
        thread {
            registerCallbacks()

            svSocket = ServerSocket(game.getGameSettings().port)
//            svSocket = ServerSocket()
            val serverSocket = svSocket!!
            Log.d("Server", "Server connected and accepting connections")
            serverCreated.setResult(null)
            while (keepAccepting) {
                try {
                    val client = serverSocket.accept()

                    val nPlayer = NetworkPlayer(
                        client,
                        PlayerData("", Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)),
                        Disc.Empty,
                        game.getGameSettings().specialPieces
                    )

                    handleClient(nPlayer)
                } catch (ex: Exception) {
                    Log.d("Server", "Error while accepting: ${ex.message}")
                }
            }

            if (!serverSocket.isClosed)
                serverSocket.close()
            Log.d("initServer", "Server not accepting anymore connections")
        }
    }

    //region Server Callbacks
    private fun registerCallbacks() {
        game.setOnPlayerToggleReady(this::onPlayerToggleReady)
        game.setOnAddPlayer(this::onAddPlayer)
        game.setOnRemovePlayer(this::onRemovePlayer)
        game.setOnShowPlaceablePieces(this::onShowPlaceablePieces)
        game.setOnClearPlaceablePieces(this::onClearPlaceablePieces)
        game.setOnPlacePiece(this::onPlacePiece)
        game.setOnPlaceBomb(this::onPlaceBomb)
        game.setOnBeginReplacePiece(this::onBeginReplacePiece)
        game.setOnAddReplacePiece(this::onAddReplacePiece)
        game.setOnRemoveReplacePiece(this::onRemoveReplacePiece)
        game.setOnEndReplacePiece(this::onEndReplacePiece)
        game.setOnPlayerScoreUpdated(this::onPlayerScoreUpdated)
        game.setOnNextPlayer(this::onNextPlayer)
        game.setOnPlayerSkip(this::onPlayerSkip)
        game.setOnGameReady(this::onGameReady)
        game.setOnGameOver(this::onGameOver)
        game.setOnPlayerBombStatusChange(this::onPlayerBombStatusChange)
        game.setOnPlayerReplaceStatusChange(this::onPlayerReplaceStatusChange)
    }

    private fun unregisterCallbacks() {
        game.setOnPlayerToggleReady(null)
        game.setOnAddPlayer(null)
        game.setOnRemovePlayer(null)
        game.setOnShowPlaceablePieces(null)
        game.setOnClearPlaceablePieces(null)
        game.setOnPlacePiece(null)
        game.setOnPlaceBomb(null)
        game.setOnBeginReplacePiece(null)
        game.setOnAddReplacePiece(null)
        game.setOnRemoveReplacePiece(null)
        game.setOnEndReplacePiece(null)
        game.setOnPlayerScoreUpdated(null)
        game.setOnNextPlayer(null)
        game.setOnPlayerSkip(null)
        game.setOnGameReady(null)
        game.setOnGameOver(null)
        game.setOnPlayerBombStatusChange(null)
        game.setOnPlayerReplaceStatusChange(null)
    }

    private fun onPlayerToggleReady(player: IPlayer) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.Ready.name)
        jsonObject.put("Payload", player.getDisc().name)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onAddPlayer(player: IPlayer) {
        val jsonToConnecting = JSONObject()
        jsonToConnecting.put("Command", Command.Connect.name)
        val payloadToConnecting = JSONArray()
        val playerConnecting = JSONObject()
        playerConnecting.put("Username", player.getUsername())
        playerConnecting.put("Avatar", player.getAvatar().toBase64())
        playerConnecting.put("Ready", player.isReady())
        playerConnecting.put("Disc", player.getDisc().name)
        payloadToConnecting.put(playerConnecting)

        val jsonToConnected = JSONObject()
        jsonToConnected.put("Command", Command.Connect.name)
        val payloadToConnected = JSONArray()
        payloadToConnected.put(playerConnecting)
        jsonToConnected.put("Payload", payloadToConnected)

        game.getAllPlayers().filter { it.getDisc() != player.getDisc() }.forEach {
            val connectedPlayer = JSONObject()
            connectedPlayer.put("Username", it.getUsername())
            connectedPlayer.put("Avatar", it.getAvatar().toBase64())
            connectedPlayer.put("Ready", it.isReady())
            connectedPlayer.put("Disc", it.getDisc().name)
            payloadToConnecting.put(connectedPlayer)

            /**
             * While we add the other players to the JSON object to send to the
             * connecting player, we send the player that is connecting to the other already
             * connected players
             */
            (it as NetworkPlayer).outputStream.writeUnshared(jsonToConnected.toString())
        }
        jsonToConnecting.put("Payload", payloadToConnecting)

        (player as NetworkPlayer).outputStream.writeUnshared(jsonToConnecting.toString())
    }

    private fun onRemovePlayer(player: IPlayer) {
        val jsonObject = JSONObject()
        val cmd = if (keepAccepting) Command.Disconnect else Command.ForceDisconnect
        jsonObject.put("Command", cmd.name)

        if (cmd == Command.Disconnect)
            jsonObject.put("Payload", player.getDisc().name)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onShowPlaceablePieces(pieces: List<Piece>) {
        val player = game.getCurrentPlayer() as NetworkPlayer
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.ShowPlaceablePieces.name)
        val payload = JSONArray()

        pieces.forEach {
            val obj = JSONObject()
            obj.put("x", it.x)
            obj.put("y", it.y)
            payload.put(obj)
        }

        jsonObject.put("Payload", payload)
        player.outputStream.writeUnshared(jsonObject.toString())
    }

    private fun onClearPlaceablePieces() {
        val player = game.getCurrentPlayer() as NetworkPlayer
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.ClearPlaceablePieces.name)
        player.outputStream.writeUnshared(jsonObject.toString())
    }

    private fun onPlacePiece(piece: Piece) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.PlacePiece.name)
        val payload = JSONObject()
        payload.put("Player", piece.disc.name)
        payload.put("x", piece.x)
        payload.put("y", piece.y)
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val player = it as NetworkPlayer
            player.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onPlaceBomb(piece: Piece) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.PlaceBomb.name)
        val payload = JSONObject()
        payload.put("Player", piece.disc.name)
        payload.put("x", piece.x)
        payload.put("y", piece.y)
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val player = it as NetworkPlayer
            player.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onBeginReplacePiece() {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.BeginReplacePiece.name)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onAddReplacePiece(piece: Piece) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.AddReplacePiece.name)
        val payload = JSONObject()
        payload.put("Player", piece.disc.name)
        payload.put("x", piece.x)
        payload.put("y", piece.y)
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onRemoveReplacePiece(piece: Piece) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.RemoveReplacePiece.name)
        val payload = JSONObject()
        payload.put("Player", piece.disc.name)
        payload.put("x", piece.x)
        payload.put("y", piece.y)
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onEndReplacePiece(pieces: List<Piece>?) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.EndReplacePiece.name)
        val payload = JSONArray()
        pieces?.forEach {
            val piece = JSONObject()
            piece.put("Player", it.disc.name)
            piece.put("x", it.x)
            piece.put("y", it.y)
            payload.put(piece)
        }
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onPlayerScoreUpdated(player: IPlayer) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.UpdateScore.name)
        val payload = JSONObject()
        payload.put("Player", player.getDisc().name)
        payload.put("Score", player.getScore().score)
        jsonObject.put("Payload", payload)
        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onNextPlayer(oldPlayer: IPlayer, newPlayer: IPlayer) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.CurrentPlayer.name)
        val payload = JSONObject()
        payload.put("OldPlayer", oldPlayer.getDisc().name)
        payload.put("NewPlayer", newPlayer.getDisc().name)
        jsonObject.put("Payload", payload)
        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onPlayerSkip(player: IPlayer) {
        val p = player as NetworkPlayer
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.PlayerSkip.name)
        p.outputStream.writeUnshared(jsonObject.toString())
    }

    private fun onGameReady() {
        keepAccepting = false

        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.GameStart.name)
        val payload = JSONObject()
        payload.put("SpecialPieces", game.getGameSettings().specialPieces)
        payload.put("AutoSkip", game.getGameSettings().autoSkip)
        payload.put("ShowPlaceable", game.getGameSettings().showPlaceable)
        payload.put("InfiniteSpecialPieces", game.getGameSettings().infiniteSpecialPieces)
        payload.put("GameMode", game.getGameMode().name)
        payload.put("Player", game.getCurrentPlayer().getDisc().name)
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }

        game.initGameView()
    }

    private fun onGameOver(winner: IPlayer?) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.GameOver.name)
        jsonObject.put("Payload", winner?.getDisc()?.name)
        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onPlayerBombStatusChange(player: IPlayer) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.PlayerBombStatusChange.name)
        val payload = JSONObject()
        payload.put("Player", player.getDisc().name)
        payload.put("Status", player.canPlaceBomb())
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }

    private fun onPlayerReplaceStatusChange(player: IPlayer) {
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.PlayerReplaceStatusChange.name)
        val payload = JSONObject()
        payload.put("Player", player.getDisc().name)
        payload.put("Status", player.canReplacePieces())
        jsonObject.put("Payload", payload)

        game.getAllPlayers().forEach {
            val p = it as NetworkPlayer
            p.outputStream.writeUnshared(jsonObject.toString())
        }
    }
    //endregion

    private fun handleClient(nPlayer: NetworkPlayer) {
        nPlayer.socket.soTimeout = defaultTimeout

        try {
            val string = nPlayer.inputStream.readUnshared() as String
            val jsonObj = JSONObject(string)
            val command = jsonObj.getString("Command")
            if (command == Command.Connect.name) {
                val payload = jsonObj.getJSONObject("Payload")
                val username = payload.getString("Username")
                val avatarBase64 = payload.getString("Avatar")
                val disc = Disc.values()[game.getPlayerCount() + Disc.Empty.ordinal + 1]
                nPlayer.getPlayerData().username = username
                nPlayer.getPlayerData().avatar = avatarBase64.toBitmap()
                nPlayer.setDisc(disc)

                if (game.addPlayer(nPlayer)) {
                    handlePlayer(nPlayer)
                }
            }
        } catch (ex: Exception) {
            Log.d("Server", "Something went wrong while handlingClient: ${ex.message}")
        }
    }

    private fun handlePlayer(nPlayer: NetworkPlayer) {
        nPlayer.socket.soTimeout = 0
        thread {
            val blockingQueue = ArrayBlockingQueue<String>(10)
            val keepListening = true
            handlePlayerCommand(blockingQueue, nPlayer)
            try {
                while (keepListening) {
                    val string = nPlayer.inputStream.readUnshared() as String
                    Log.d("Server", "From Client: '$string'")
                    blockingQueue.put(string)
                }
            } catch (ex: Exception) {
                Log.d("Server", "Exception: '${ex.message}'")
                game.removePlayer(nPlayer)
            }
        }
    }

    private fun handlePlayerCommand(
        blockingQueue: ArrayBlockingQueue<String>,
        nPlayer: NetworkPlayer
    ) {
        thread {
            while (true) {
                val string = blockingQueue.take()
                val jsonObject = JSONObject(string)
                when (jsonObject.getString("Command")) {
                    Command.Ready.name -> {
                        game.togglePlayerReady(nPlayer.getDisc())
                    }
                    Command.PlacePiece.name -> {
                        if (nPlayer == game.getCurrentPlayer()) {
                            val payload = jsonObject.getJSONObject("Payload")
                            val x = payload.getInt("x")
                            val y = payload.getInt("y")
                            game.placePiece(x, y)
                        }
                    }
                    Command.PlaceBomb.name -> {
                        if (nPlayer == game.getCurrentPlayer()) {
                            val payload = jsonObject.getJSONObject("Payload")
                            val x = payload.getInt("x")
                            val y = payload.getInt("y")
                            game.placeBomb(x, y)
                        }
                    }
                    Command.PlayerSkip.name -> {
                        if (nPlayer == game.getCurrentPlayer())
                            game.setNextPlayer()
                    }
                    Command.Disconnect.name -> {
                        if (!nPlayer.socket.isClosed)
                            nPlayer.socket.close()
                        return@thread
                    }
                }
            }
        }
    }

    fun getServerExternalAddress(): String {
        return getExternalIP()
    }

    fun getServerPort(): Int {
        return svSocket?.localPort ?: game.getGameSettings().port
    }

    fun endServer() {
        keepAccepting = false
        val jsonObject = JSONObject()
        jsonObject.put("Command", Command.ForceDisconnect.name)
        game.getAllPlayers().forEach {
            val player = it as NetworkPlayer
            player.outputStream.writeUnshared(jsonObject.toString())
        }
        closeSvSocket()
    }

    fun closeSvSocket() {
        unregisterCallbacks()
        svSocket?.close()
    }
}
