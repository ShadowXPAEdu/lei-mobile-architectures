package pt.isec.jck.reversi.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.application.ReversiApplication
import pt.isec.jck.reversi.databinding.ActivityGameBinding
import pt.isec.jck.reversi.logic.controllers.game.NetworkReversiGame
import pt.isec.jck.reversi.logic.models.game.Disc
import pt.isec.jck.reversi.logic.models.game.IGame
import pt.isec.jck.reversi.logic.models.game.Piece
import pt.isec.jck.reversi.logic.models.player.IPlayer
import java.util.*
import kotlin.collections.ArrayList

class GameActivity : AppCompatActivity() {

    companion object {
        const val NOW_PLAYING = 1f
        const val WAITING_TURN = 0.4f
    }

    private lateinit var binding: ActivityGameBinding
    private lateinit var app: ReversiApplication
    private lateinit var game: IGame
    private lateinit var logs: ArrayList<String>

    private lateinit var logAdapter: ArrayAdapter<String>

    private lateinit var pieceList: List<List<ImageView>>
    private lateinit var llGamePlayers: Array<LinearLayout>

    private val random = Random()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        app = application as ReversiApplication
        game = app.game!!
        logs = app.logs!!
        setContentView(binding.root)

        llGamePlayers =
            arrayOf(binding.llGamePlayer1, binding.llGamePlayer2, binding.llGamePlayer3)
        for (i in 0 until game.getPlayerCount())
            addPlayerToView(game.getPlayerByIndex(i)!!, llGamePlayers[i])

        binding.glGameBoard.columnCount = game.getGameMode().boardSize
        binding.glGameBoard.rowCount = game.getGameMode().boardSize

        logAdapter = ArrayAdapter(this, R.layout.log_layout, logs)
        binding.lvGameLogs.adapter = logAdapter

        pieceList =
            arrayOfNulls<List<ImageView>>(game.getGameMode().boardSize).mapIndexed { x, _ ->
                arrayOfNulls<ImageView>(game.getGameMode().boardSize).mapIndexed { y, _ ->
                    val piece = layoutInflater.inflate(R.layout.board_piece, null)
                    piece.setOnClickListener {
                        game.placePiece(x, y)
                    }
                    piece.setOnLongClickListener {
                        game.placeBomb(x, y)
                    }
                    binding.glGameBoard.addView(piece)
                    piece.findViewById(R.id.imgBoardPiece) as ImageView
                }
            }

        registerCallbacks()
        game.initGameView()
    }

    //region Helper
    @SuppressLint("InflateParams")
    private fun addPlayerToView(player: IPlayer, llGamePlayer: LinearLayout) {
        val view = layoutInflater.inflate(R.layout.game_player_layout, null)
        val playerAvatar = view.findViewById<ImageView>(R.id.imgPlayerAvatar)
        val playerName = view.findViewById<TextView>(R.id.tvPlayerName)
        val playerDisc = view.findViewById<ImageView>(R.id.imgPlayerDisc)

        playerAvatar.setImageBitmap(player.getAvatar())
        playerName.text = player.getUsername()
        playerDisc.setBackgroundColor(player.getDisc().color)

        llGamePlayer.addView(view)
        llGamePlayer.tag = player
        llGamePlayer.alpha = WAITING_TURN
    }
    //endregion

    //region Callbacks
    private fun registerCallbacks() {
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
        game.setOnGameOver(this::onGameOver)
        game.setOnPlayerBombStatusChange(this::onPlayerBombStatusChange)
        game.setOnPlayerReplaceStatusChange(this::onPlayerReplaceStatusChange)
        game.setOnGameDead(this::onGameDead)
    }

    private fun addToLogs(message: String) {
        runOnUiThread {
            logs.add(message)
            logAdapter.notifyDataSetChanged()
        }
    }

    private fun onShowPlaceablePieces(pieces: List<Piece>) {
        pieces.forEach {
            runOnUiThread {
                pieceList[it.x][it.y].setBackgroundColor(
                    ContextCompat.getColor(this, R.color.light_green)
                )
            }
        }
        addToLogs(getString(R.string.onShowPlaceablePieces))
    }

    private fun onClearPlaceablePieces() {
        pieceList.flatten()
            .forEach {
                runOnUiThread {
                    it.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                }
            }
        addToLogs(getString(R.string.onClearPlaceablePieces))
    }

    private fun onPlacePiece(piece: Piece) {
        val gameMode = game.getGameMode()
        with(piece) {
            if (x < 0 || x >= gameMode.boardSize || y < 0 || y >= gameMode.boardSize)
                return
        }

        val imgResource = when (piece.disc) {
            Disc.Black -> R.drawable.ic_black_disc
            Disc.White -> R.drawable.ic_white_disc
            Disc.Gold -> R.drawable.ic_gold_disc
            Disc.Empty -> android.R.color.transparent
        }
        runOnUiThread {
            with(pieceList[piece.x][piece.y]) {
                animatePlacePiece(this, imgResource)
            }
        }
        addToLogs(getString(R.string.onPlacePiece, piece.disc.name, piece.x, piece.y))
    }

    private fun onPlaceBomb(piece: Piece) {
        val gameMode = game.getGameMode()
        with(piece) {
            for (i in -1..1)
                for (j in -1..1)
                    if (!((i == 0 && j == 0) || (x + i < 0 || x + i >= gameMode.boardSize || y + j < 0 || y + j >= gameMode.boardSize)))
                        runOnUiThread {
                            animatePlaceBomb(pieceList[x + i][y + j])
                        }
        }
        onPlacePiece(piece)
        addToLogs(getString(R.string.onPlaceBomb, piece.x, piece.y))
    }

    private fun onBeginReplacePiece() {
        Log.d("Game", "Began replacing pieces")
        addToLogs(getString(R.string.onBeginReplacePiece))
    }

    private fun onAddReplacePiece(piece: Piece) {
        val color =
            if (game.isOwnPiece(piece.x, piece.y))
                ContextCompat.getColor(this, R.color.light_red)
            else
                ContextCompat.getColor(this, R.color.light_blue)
        runOnUiThread {
            pieceList[piece.x][piece.y].setBackgroundColor(color)
        }
        addToLogs(getString(R.string.onAddReplacePiece, piece.disc.name, piece.x, piece.y))
    }

    private fun onRemoveReplacePiece(piece: Piece) {
        runOnUiThread {
            pieceList[piece.x][piece.y].setBackgroundColor(
                ContextCompat.getColor(this, R.color.green)
            )
        }
        addToLogs(getString(R.string.onRemoveReplacePiece, piece.disc.name, piece.x, piece.y))
    }

    private fun onEndReplacePiece(pieces: List<Piece>?) {
        Log.d("Game", "Ended replacing pieces")
        if (pieces != null) {
            val piece1 = Piece(pieces[0].x, pieces[0].y, pieces[2].disc)
            val piece2 = Piece(pieces[1].x, pieces[1].y, pieces[2].disc)
            val piece3 = Piece(pieces[2].x, pieces[2].y, pieces[0].disc)
            onPlacePiece(piece1)
            onPlacePiece(piece2)
            onPlacePiece(piece3)
            addToLogs(getString(R.string.onEndReplacePieceSwapped))
        }
        addToLogs(getString(R.string.onEndReplacePiece))
    }

    private fun onPlayerScoreUpdated(player: IPlayer) {
        val llPlayer = binding.llGamePlayers.findViewWithTag<LinearLayout>(player)
        val tvScore = llPlayer.findViewById<TextView>(R.id.tvPlayerScore)
        runOnUiThread {
            tvScore.text = "${player.getScore()}"
        }
        addToLogs(getString(R.string.onPlayerScoreUpdated, player.getDisc().name))
    }

    private fun onNextPlayer(oldPlayer: IPlayer, newPlayer: IPlayer) {
        val llOldPlayer = binding.llGamePlayers.findViewWithTag<LinearLayout>(oldPlayer)
        val llNewPlayer = binding.llGamePlayers.findViewWithTag<LinearLayout>(newPlayer)
        runOnUiThread {
            animateLinearLayoutAlpha(llOldPlayer, WAITING_TURN)
            animateLinearLayoutAlpha(llNewPlayer, NOW_PLAYING)
        }
        addToLogs(getString(R.string.onNextPlayer, newPlayer.getDisc().name))
    }

    private fun onPlayerSkip(player: IPlayer) {
        val builder = AlertDialog.Builder(this)
            .setMessage(getString(R.string.you_must_skip))
            .setCancelable(false)
            .setPositiveButton(R.string.okay) { d, _ ->
                game.setNextPlayer()
                d.dismiss()
            }
            .setTitle(getString(R.string.skip))

        runOnUiThread {
            builder.show()
        }
        addToLogs(getString(R.string.onPlayerSkip, player.getDisc().name))
    }

    private fun onGameOver(winner: IPlayer?) {
        val builder = AlertDialog.Builder(this)
            .setPositiveButton(R.string.okay) { d, _ ->
                d.dismiss()
            }
            .setTitle(getString(R.string.winner))

        if (winner != null) {
            val playerLayout = layoutInflater.inflate(
                R.layout.top_score_player_layout,
                binding.root,
                false
            ) as LinearLayout
            val imgView = playerLayout.findViewById<ImageView>(R.id.imgPlayerAvatar)
            val nameView = playerLayout.findViewById<TextView>(R.id.tvPlayerName)
            val scoreView = playerLayout.findViewById<TextView>(R.id.tvPlayerScore)
            imgView.setImageBitmap(winner.getAvatar())
            nameView.text = winner.getUsername()
            scoreView.text = "${winner.getScore().score}"
            builder.setView(playerLayout)
        } else {
            builder.setMessage(getString(R.string.game_draw))
        }

        highlightWinner(winner)
        runOnUiThread {
            builder.show()
        }
        addToLogs(getString(R.string.onGameOver))
    }

    private fun onPlayerBombStatusChange(player: IPlayer) {
        onPlayerStatusChange(player, player.canPlaceBomb(), R.id.tvBomb)
        addToLogs(getString(R.string.onPlayerBombStatusChange, player.getDisc().name))
    }

    private fun onPlayerReplaceStatusChange(player: IPlayer) {
        onPlayerStatusChange(player, player.canReplacePieces(), R.id.tvReplace)
        addToLogs(getString(R.string.onPlayerReplaceStatusChange, player.getDisc().name))
    }

    private fun onPlayerStatusChange(player: IPlayer, available: Boolean, id: Int) {
        val llPlayer = binding.llGamePlayers.findViewWithTag<LinearLayout>(player)
        val tv = llPlayer.findViewById<TextView>(id)
        val color =
            if (available) Color.TRANSPARENT else ContextCompat.getColor(this, R.color.red)
        runOnUiThread {
            tv.setBackgroundColor(color)
        }
    }

    private fun onGameDead() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.game_died))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.continue_playing)) { d, _ ->
                    d.dismiss()
                }
                .setNegativeButton(getString(R.string.exit)) { d, _ ->
                    d.dismiss()
                    onSupportNavigateUp()
                }
                .setTitle(getString(R.string.server_died))
                .show()
        }
        addToLogs(getString(R.string.onGameDead))
    }

    private fun highlightWinner(winner: IPlayer?) {
        llGamePlayers.forEach {
            runOnUiThread {
                animateLinearLayoutAlpha(it, WAITING_TURN)
            }
        }
        if (winner != null) {
            val llWinner = binding.llGamePlayers.findViewWithTag<LinearLayout>(winner)
            runOnUiThread {
                animateLinearLayoutAlpha(llWinner, NOW_PLAYING)
            }
        }
    }

    private fun animatePlacePiece(imgView: ImageView, imgRes: Int) {
        imgView.animate().apply {
            duration = 125
            rotationY(90f)
        }.withEndAction {
            imgView.animate().apply {
                duration = 125
                imgView.setImageResource(imgRes)
                rotationY(180f)
            }.start()
        }
    }

    private fun animatePlaceBomb(imgView: ImageView) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(250)
        valueAnimator.addUpdateListener {
            imgView.translationX = (random.nextFloat() - 0.5f) * imgView.width * 0.05f
            imgView.translationY = (random.nextFloat() - 0.5f) * imgView.height * 0.05f
        }
        valueAnimator.start()

        imgView.animate().apply {
            duration = 250
            startDelay = 200
            scaleX(0f)
            scaleY(0f)
            alpha(0f)
        }.withEndAction {
            imgView.animate().apply {
                duration = 250
                imgView.setImageResource(android.R.color.transparent)
                translationX(0f)
                translationY(0f)
                scaleX(1f)
                scaleY(1f)
                alpha(1f)
            }.start()
        }
    }

    private fun animateLinearLayoutAlpha(ll: LinearLayout, value: Float) {
        ll.animate().apply {
            duration = 250
            alpha(value)
        }.start()
    }
    //endregion

    //region OnBack
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.about_to_quit))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { d, _ ->
                d.dismiss()
                onSupportNavigateUp()
            }
            .setNegativeButton(getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .setTitle(getString(R.string.exiting))
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBack()
        return super.onSupportNavigateUp()
    }

    private fun onBack() {
        if (app.game is NetworkReversiGame) {
            val g = app.game as NetworkReversiGame
            g.endServer()
            g.setOnGameDead(null)
        }
        app.game = null
        app.player = null
    }
    //endregion
}
