package pt.isec.jck.reversi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.application.ReversiApplication
import pt.isec.jck.reversi.databinding.ActivityGameSettingsBinding
import pt.isec.jck.reversi.logic.controllers.game.NetworkReversiGame
import pt.isec.jck.reversi.logic.controllers.game.ReversiGame
import pt.isec.jck.reversi.logic.controllers.player.AnonymousPlayer
import pt.isec.jck.reversi.logic.controllers.player.HumanPlayer
import pt.isec.jck.reversi.logic.models.game.Disc
import pt.isec.jck.reversi.logic.models.game.GameMode
import pt.isec.jck.reversi.logic.models.game.GameSettings
import pt.isec.jck.reversi.logic.models.player.PlayerData

class GameSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameSettingsBinding
    private lateinit var app: ReversiApplication
    private lateinit var gameMode: GameMode
    private var createServer: Boolean = false

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameSettingsBinding.inflate(layoutInflater)
        app = application as ReversiApplication
        setContentView(binding.root)

        gameMode = GameMode.values()[intent.getIntExtra("GameMode", 0)]

        binding.cbGameSettingsSpecialDiscs.setOnCheckedChangeListener { _, isChecked ->
            binding.cbGameSettingsInfiniteSpecialDiscs.isEnabled = isChecked
            if (!isChecked)
                binding.cbGameSettingsInfiniteSpecialDiscs.isChecked = false
        }

        binding.cbGameSettingsInfiniteSpecialDiscs.isEnabled =
            binding.cbGameSettingsSpecialDiscs.isChecked

        if (!gameMode.online) {
            binding.llGameSettingsIP.visibility = View.GONE
            binding.llGameSettingsPort.visibility = View.GONE
        } else {
            createServer = intent.getBooleanExtra("CreateServer", false)
            if (createServer) {
                binding.llGameSettingsIP.visibility = View.GONE
            } else {
                binding.llGameSettingsCheckBoxes.visibility = View.GONE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        db = Firebase.firestore
    }

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
                val ip = binding.etGameSettingsIP.text.toString()
                val port = binding.etGameSettingsPort.text.toString().toIntOrNull()
                val specialPieces = binding.cbGameSettingsSpecialDiscs.isChecked
                val autoSkip = binding.cbGameSettingsAdvancedSkip.isChecked
                val showPlaceable = binding.cbGameSettingsShowPlaceable.isChecked
                val infiniteSpecialPieces =
                    specialPieces && binding.cbGameSettingsInfiniteSpecialDiscs.isChecked

                val gameSettings =
                    GameSettings(
                        ip,
                        port ?: 9999,
                        specialPieces,
                        autoSkip,
                        showPlaceable,
                        infiniteSpecialPieces
                    )

                if (app.player == null) {
                    val playerData = if (app.user == null) PlayerData(
                        "Player",
                        app.defaultPlayerAvatar
                    ) else app.user!!.playerData

                    app.player = HumanPlayer(
                        playerData,
                        Disc.Black,
                        specialPieces
                    )
                }

                if (!gameMode.online) {
                    val game = ReversiGame(gameMode, gameSettings)

                    game.addPlayer(app.player!!)
                    game.addPlayer(
                        AnonymousPlayer(
                            app.defaultPlayerAvatar,
                            Disc.White,
                            specialPieces
                        )
                    )
                    app.game = game
                } else {
                    app.game =
                        NetworkReversiGame(
                            gameMode, gameSettings, createServer,
                            app.player!!, db.collection("users").document(app.user!!.email)
                        )
                }

                val intent = Intent(this, WaitingRoomActivity::class.java)
                startActivity(intent)
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
        app.game = null
        app.player = null
    }
    //endregion
}
