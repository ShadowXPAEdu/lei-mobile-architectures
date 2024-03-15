package pt.isec.jck.reversi.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.application.ReversiApplication
import pt.isec.jck.reversi.databinding.ActivityWaitingRoomBinding
import pt.isec.jck.reversi.logic.adapters.WaitingRoomPlayerListAdapter
import pt.isec.jck.reversi.logic.controllers.game.NetworkReversiGame
import pt.isec.jck.reversi.logic.models.game.IGame
import pt.isec.jck.reversi.logic.models.player.IPlayer
import kotlin.concurrent.thread

class WaitingRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingRoomBinding
    private lateinit var app: ReversiApplication
    private lateinit var game: IGame
    private lateinit var player: IPlayer
    private lateinit var waitingRoomPlayerListAdapter: WaitingRoomPlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaitingRoomBinding.inflate(layoutInflater)
        app = application as ReversiApplication
        game = app.game!!
        player = app.player!!
        setContentView(binding.root)

        registerCallbacks()

        if (game.getGameMode().online) {
            thread {
                val g = game as NetworkReversiGame
                val externalIp = g.getServerExternalAddress()
                val port = g.getServerPort()
                with(binding) {
                    runOnUiThread {
                        tvIP.text = externalIp
                        tvPort.text = port.toString()
                    }
                }
            }
        } else {
            binding.llWaitingRoomInfo.visibility = View.GONE
        }

        waitingRoomPlayerListAdapter = WaitingRoomPlayerListAdapter(app, layoutInflater)
        binding.lvWaitingRoomPlayerList.adapter = waitingRoomPlayerListAdapter
    }

    //region Callbacks
    private fun registerCallbacks() {
        game.setOnPlayerToggleReady(this::onPlayerToggleReady)
        game.setOnAddPlayer(this::onAddPlayer)
        game.setOnRemovePlayer(this::onRemovePlayer)
        game.setOnGameReady(this::onGameReady)
        game.setOnGameDead(this::onGameDead)
    }

    private fun unregisterCallbacks() {
        game.setOnPlayerToggleReady(null)
        game.setOnAddPlayer(null)
        game.setOnRemovePlayer(null)
        game.setOnGameReady(null)
        game.setOnGameDead(null)
    }

    private fun onPlayerToggleReady(player: IPlayer) {
        Log.d("WaitingRoom", "PlayerReady: $player")
        runOnUiThread {
            waitingRoomPlayerListAdapter.notifyDataSetChanged()
        }
    }

    private fun onAddPlayer(player: IPlayer) {
        Log.d("WaitingRoom", "AddedPlayer: $player")
        runOnUiThread {
            waitingRoomPlayerListAdapter.notifyDataSetChanged()
        }
    }

    private fun onRemovePlayer(player: IPlayer) {
        Log.d("WaitingRoom", "RemovedPlayer: $player")
        runOnUiThread {
            waitingRoomPlayerListAdapter.notifyDataSetChanged()
        }
    }

    private fun onGameReady() {
        unregisterCallbacks()
        app.logs = ArrayList()
        app.logs!!.add(getString(R.string.game_started))
        runOnUiThread {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onGameDead() {
        onSupportNavigateUp()
    }
    //endregion

    //region Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        try {
            menuInflater.inflate(R.menu.checked_menu, menu)
        } catch (ex: Exception) {
            return false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.check -> {
                game.togglePlayerReady(player.getDisc())
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //endregion

    //region OnBack
    override fun onBackPressed() {
        onSupportNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBack()
        return super.onSupportNavigateUp()
    }

    private fun onBack() {
        if (game.getGameMode().online) {
            val g = game as NetworkReversiGame
            g.endServer()
        }
        app.game = null
        app.player = null
    }
    //endregion
}
