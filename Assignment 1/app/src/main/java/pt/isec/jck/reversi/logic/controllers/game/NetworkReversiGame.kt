package pt.isec.jck.reversi.logic.controllers.game

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import org.json.JSONArray
import org.json.JSONObject
import pt.isec.jck.reversi.logic.controllers.net.GameServer
import pt.isec.jck.reversi.logic.controllers.player.HumanPlayer
import pt.isec.jck.reversi.logic.controllers.utils.parseFirestoreTopScoreArray
import pt.isec.jck.reversi.logic.controllers.utils.toBase64
import pt.isec.jck.reversi.logic.controllers.utils.toBitmap
import pt.isec.jck.reversi.logic.models.game.Disc
import pt.isec.jck.reversi.logic.models.game.GameMode
import pt.isec.jck.reversi.logic.models.game.GameSettings
import pt.isec.jck.reversi.logic.models.game.Piece
import pt.isec.jck.reversi.logic.models.net.Command
import pt.isec.jck.reversi.logic.models.player.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class NetworkReversiGame(
    gameMode: GameMode,
    gameSettings: GameSettings,
    private val createServer: Boolean,
    private val player: IPlayer,
    private val doc: DocumentReference? = null,
    private val defaultTimeout: Int = 10000
) : ReversiGame(gameMode, gameSettings) {

    private var server: GameServer? = null

    private val socket: Socket = Socket()
    private var inputStream: ObjectInputStream? = null
    private var outputStream: ObjectOutputStream? = null

    private var isGameReady = false
    private var isServerDead = false
//    private var hasGameEnded = false

    //region Server
    private fun beginServer(gameMode: GameMode, serverCreated: TaskCompletionSource<Unit>) {
        server = GameServer(ReversiGame(gameMode, getGameSettings()), defaultTimeout)
        server!!.initServer(serverCreated)
    }

    fun endServer() {
        thread {
            if (!createServer) {
                val jsonObject = JSONObject()
                jsonObject.put("Command", Command.Disconnect.name)
                try {
                    outputStream?.writeUnshared(jsonObject.toString())
                } catch (_: Exception) {
                }
            }
            server?.endServer()
        }
    }

    fun getServerExternalAddress(): String {
        return if (createServer)
            server!!.getServerExternalAddress()
        else
            getGameSettings().ip
    }

    fun getServerPort(): Int {
        return if (createServer)
            server!!.getServerPort()
        else
            getGameSettings().port
    }
    //endregion

    //region Client
    init {
        val serverCreated = TaskCompletionSource<Unit>()
        serverCreated.task.addOnCompleteListener {
            beginClient()
        }

        if (createServer) {
            beginServer(gameMode, serverCreated)
        } else {
            serverCreated.setResult(null)
        }
    }

    private fun beginClient() {
        thread {
            try {
                socket.connect(InetSocketAddress(getGameSettings().ip, getGameSettings().port))
                inputStream = ObjectInputStream(socket.getInputStream())
                outputStream = ObjectOutputStream(socket.getOutputStream())

                val jsonObject = JSONObject()
                jsonObject.put("Command", Command.Connect.name)
                val payload = JSONObject()
                payload.put("Username", player.getUsername())
                payload.put("Avatar", player.getAvatar().toBase64())
                jsonObject.put("Payload", payload)

                outputStream!!.writeUnshared(jsonObject.toString())

                val string = inputStream!!.readUnshared() as String
                val responseJson = JSONObject(string)
                val command = responseJson.getString("Command")
                if (command == Command.Connect.name) {
                    val responsePayload = responseJson.getJSONArray("Payload")
                    val thisPlayer = responsePayload.getJSONObject(0)
                    player.setDisc(Disc.valueOf(thisPlayer.getString("Disc")))
                    addPlayer(player)

                    addPlayersFromServer(responsePayload)

                    startListening(inputStream!!)
                }
            } catch (ex: Exception) {
                Log.d("Client", "Could not connect... '${ex.message}'")
                if (!socket.isClosed)
                    socket.close()
            }
        }
    }

    private fun addPlayersFromServer(payload: JSONArray) {
        for (i in 0 until payload.length()) {
            val playerJson = payload.getJSONObject(i)
            val hPlayer = HumanPlayer(
                PlayerData(
                    playerJson.getString("Username"),
                    playerJson.getString("Avatar").toBitmap()
                ),
                Disc.valueOf(playerJson.getString("Disc")),
                true,
                playerJson.getBoolean("Ready")
            )
            addPlayer(hPlayer)
        }
    }

    private fun startListening(inputStream: ObjectInputStream) {
        socket.soTimeout = 0
        thread {
            val blockingQueue = ArrayBlockingQueue<String>(10)
            val keepListening = true
            handleServerCommand(blockingQueue)
            try {
                while (keepListening) {
                    val string = inputStream.readUnshared() as String
                    Log.d("Client", "From Server: '$string'")
                    blockingQueue.put(string)
                }
            } catch (ex: Exception) {
                Log.d("Client", "Could not startListening: ${ex.message}")
                if (!socket.isClosed)
                    socket.close()
                onServerDead()
            }
        }
    }

    private fun handleServerCommand(blockingQueue: ArrayBlockingQueue<String>) {
        thread {
            while (true) {
                val string = blockingQueue.take()
                val jsonObject = JSONObject(string)
                when (jsonObject.getString("Command")) {
                    Command.Connect.name -> {
                        val payload = jsonObject.getJSONArray("Payload")
                        addPlayersFromServer(payload)
                    }
                    Command.Ready.name -> {
                        val disc = jsonObject.getString("Payload")
                        super.togglePlayerReady(Disc.valueOf(disc))
                    }
                    Command.GameStart.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val usingSpecialPieces = payload.getBoolean("SpecialPieces")
                        setGameSettings(
                            GameSettings(
                                getGameSettings().ip, getGameSettings().port,
                                usingSpecialPieces,
                                payload.getBoolean("AutoSkip"),
                                payload.getBoolean("ShowPlaceable"),
                                payload.getBoolean("InfiniteSpecialPieces")
                            )
                        )
                        setGameMode(GameMode.valueOf(payload.getString("GameMode")))
                        setCurrentPlayer(Disc.valueOf(payload.getString("Player")))
                        isGameReady = true
                        player.setUsingSpecialPieces(usingSpecialPieces)
                        checkIsGameReady()
                    }
                    Command.ShowPlaceablePieces.name -> {
                        val payload = jsonObject.getJSONArray("Payload")
                        val pieces = ArrayList<Piece>()
                        for (i in 0 until payload.length()) {
                            val piece = payload.getJSONObject(i)
                            pieces.add(
                                Piece(
                                    piece.getInt("x"),
                                    piece.getInt("y"),
                                    Disc.Empty
                                )
                            )
                        }
                        invokeShowPlaceablePieces(pieces)
                    }
                    Command.PlacePiece.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val piece = Piece(
                            payload.getInt("x"),
                            payload.getInt("y"),
                            Disc.valueOf(payload.getString("Player"))
                        )
                        placePiece(piece)
                    }
                    Command.PlaceBomb.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val piece = Piece(
                            payload.getInt("x"),
                            payload.getInt("y"),
                            Disc.valueOf(payload.getString("Player"))
                        )
                        placeBomb(piece)
                    }
                    Command.BeginReplacePiece.name -> {
                        beginReplacePiece()
                    }
                    Command.AddReplacePiece.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val piece = Piece(
                            payload.getInt("x"),
                            payload.getInt("y"),
                            Disc.valueOf(payload.getString("Player"))
                        )
                        addPieceToList(piece)
                    }
                    Command.RemoveReplacePiece.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val piece = Piece(
                            payload.getInt("x"),
                            payload.getInt("y"),
                            Disc.valueOf(payload.getString("Player"))
                        )
                        removePieceFromList(piece)
                    }
                    Command.EndReplacePiece.name -> {
                        val payload = jsonObject.getJSONArray("Payload")
                        val replaceList = ArrayList<Piece>()
                        for (i in 0 until payload.length()) {
                            val obj = payload.getJSONObject(i)
                            val piece = Piece(
                                obj.getInt("x"),
                                obj.getInt("y"),
                                Disc.valueOf(obj.getString("Player"))
                            )
                            replaceList.add(piece)
                        }
                        replacePieces(if (replaceList.isEmpty()) null else replaceList.toList())
                        if (replaceList.isNotEmpty())
                            invokeClearPlaceablePieces()
                        clearReplaceList()
                    }
                    Command.ClearPlaceablePieces.name -> {
                        invokeClearPlaceablePieces()
                    }
                    Command.UpdateScore.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val disc = Disc.valueOf(payload.getString("Player"))
                        val score = payload.getInt("Score")
                        updateScore(getPlayerByDisc(disc)!!, score)
                    }
                    Command.CurrentPlayer.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val oldDisc = Disc.valueOf(payload.getString("OldPlayer"))
                        val newDisc = Disc.valueOf(payload.getString("NewPlayer"))
                        setCurrentPlayer(newDisc)
                        invokeNextPlayer(getPlayerByDisc(oldDisc)!!, getPlayerByDisc(newDisc)!!)
                    }
                    Command.PlayerSkip.name -> {
                        invokePlayerSkip(player)
                    }
                    Command.PlayerBombStatusChange.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val p = getPlayerByDisc(Disc.valueOf(payload.getString("Player")))!!
                        p.setCanPlaceBomb(payload.getBoolean("Status"))
                        invokePlayerBombStatusChange(p)
                    }
                    Command.PlayerReplaceStatusChange.name -> {
                        val payload = jsonObject.getJSONObject("Payload")
                        val p = getPlayerByDisc(Disc.valueOf(payload.getString("Player")))!!
                        p.setCanReplacePieces(payload.getBoolean("Status"))
                        invokePlayerReplaceStatusChange(p)
                    }
                    Command.GameOver.name -> {
                        var winner: IPlayer? = null
                        if (jsonObject.has("Payload")) {
                            val disc = Disc.valueOf(jsonObject.getString("Payload"))
                            winner = getPlayerByDisc(disc)
                        }

                        if (winner == player && !hasGameEnded)
                            doc?.get()
                                ?.addOnCompleteListener { onFirebaseTopScoreListener(it, winner) }

                        invokeGameOver(winner)
                    }
                    Command.Disconnect.name -> {
                        val p = getPlayerByDisc(Disc.valueOf(jsonObject.getString("Payload")))!!
                        removePlayer(p)
                    }
                    Command.ForceDisconnect.name -> {
                        onServerDead()
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onFirebaseTopScoreListener(it: Task<DocumentSnapshot>, winner: IPlayer) {
        if (it.isSuccessful) {
            val userData = it.result.data!!

            val topScores =
                parseFirestoreTopScoreArray(userData["topScore"] as ArrayList<HashMap<String, Any>>)

            updateTopScore(
                winner,
                topScores
            )
        }
    }

    private fun updateTopScore(winner: IPlayer, topScore: ArrayList<TopScore>) {
        if (topScore.size == 5) {
            // We need to check if top scores are full so we can remove the lowest top score...
            val lowestScore =
                topScore.find { it.playerScore.score <= winner.getScore().score } ?: return
            topScore.remove(lowestScore)
        }
        val pairList = mutableListOf<Pair<PlayerDataFirestore, Score>>()
        getAllPlayers()
            .filter { it != winner }
            .forEach {
                pairList.add(Pair(PlayerDataFirestore(it.getPlayerData()), it.getScore()))
            }
        topScore.add(
            TopScore(
                winner.getScore(),
                pairList
            )
        )
        doc?.update("topScore", topScore.toList())
    }

    private fun onServerDead() {
        if (!isServerDead) {
            isServerDead = true
            invokeGameDead()
            resetOnGameDead()
            if (isGameReady)
                super.initGameView(null)
            if (!socket.isClosed)
                socket.close()
            inputStream = null
            outputStream = null
            server?.closeSvSocket()
        }
    }
    //endregion

    //region IGame
    override fun togglePlayerReady(disc: Disc) {
        if (isServerDead) {
            super.togglePlayerReady(disc)
        } else {
            thread {
                val jsonObject = JSONObject()
                jsonObject.put("Command", Command.Ready.name)
                outputStream?.writeUnshared(jsonObject.toString())
            }
        }
    }

    override fun isGameReady(): Boolean {
        if (isServerDead)
            return super.isGameReady()
        return isGameReady
    }

    override fun initGameView(player: IPlayer?) {
        if (isServerDead)
            super.initGameView(null)
        else
            super.initGameView(this.player)
    }

    override fun placePiece(x: Int, y: Int) {
        if (isServerDead)
            super.placePiece(x, y)
        else {
            if (!hasGameEnded && player == getCurrentPlayer()) {
                thread {
                    val jsonObject = JSONObject()
                    jsonObject.put("Command", Command.PlacePiece.name)
                    val payload = JSONObject()
                    payload.put("x", x)
                    payload.put("y", y)
                    jsonObject.put("Payload", payload)
                    outputStream?.writeUnshared(jsonObject.toString())
                }
            }
        }
    }

    override fun placeBomb(x: Int, y: Int): Boolean {
        if (isServerDead)
            return super.placeBomb(x, y)
        if (!hasGameEnded && player == getCurrentPlayer()) {
            thread {
                val jsonObject = JSONObject()
                jsonObject.put("Command", Command.PlaceBomb.name)
                val payload = JSONObject()
                payload.put("x", x)
                payload.put("y", y)
                jsonObject.put("Payload", payload)
                outputStream?.writeUnshared(jsonObject.toString())
            }
        }
        return true
    }

    override fun setNextPlayer() {
        if (isServerDead)
            super.setNextPlayer()
        else {
            if (player == getCurrentPlayer()) {
                thread {
                    val jsonObject = JSONObject()
                    jsonObject.put("Command", Command.PlayerSkip.name)
                    outputStream?.writeUnshared(jsonObject.toString())
                }
            }
        }
    }
    //endregion
}
