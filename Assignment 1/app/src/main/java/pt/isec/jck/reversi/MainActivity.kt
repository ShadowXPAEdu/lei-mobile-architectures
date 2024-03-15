package pt.isec.jck.reversi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.jck.reversi.activities.GameSettingsActivity
import pt.isec.jck.reversi.activities.ProfileActivity
import pt.isec.jck.reversi.application.ReversiApplication
import pt.isec.jck.reversi.databinding.ActivityMainBinding
import pt.isec.jck.reversi.logic.controllers.utils.parseFirestoreTopScoreArray
import pt.isec.jck.reversi.logic.controllers.utils.toBase64
import pt.isec.jck.reversi.logic.controllers.utils.toBitmap
import pt.isec.jck.reversi.logic.models.game.GameMode
import pt.isec.jck.reversi.logic.models.player.PlayerData
import pt.isec.jck.reversi.logic.models.player.TopScore
import pt.isec.jck.reversi.logic.models.profile.Profile
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: ReversiApplication

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val googleSignInActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    createAndShowLoginFailedDialog()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        app = application as ReversiApplication
        setContentView(binding.root)
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnMainOffline.setOnClickListener {
            val intent = Intent(this, GameSettingsActivity::class.java)
            intent.putExtra("GameMode", GameMode.Offline.ordinal)
            startActivity(intent)
        }
        binding.btnMainOnline.setOnClickListener {
            if (!hasNetworkConnection()) {
                Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createAndShowCreateOrJoinRoomDialog(R.string.play_online, GameMode.Online)
        }
        binding.btnMainOnline3.setOnClickListener {
            if (!hasNetworkConnection()) {
                Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createAndShowCreateOrJoinRoomDialog(R.string.play_online_3, GameMode.Online3)
        }
    }

    //region Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        try {
            menuInflater.inflate(R.menu.main_menu, menu)
        } catch (ex: Exception) {
            return false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val user = app.user
        val menuItemLogin: MenuItem = menu!!.findItem(R.id.sign_in_button)
        val menuItemUser: MenuItem = menu.findItem(R.id.user)
        val menuItemLogout = menu.findItem(R.id.logout)
        val userExists = user != null
        menuItemLogin.isVisible = !userExists
        menuItemUser.isVisible = userExists
        menuItemUser.title = user?.playerData?.username ?: getString(R.string.user)
        menuItemLogout.isVisible = userExists
        binding.btnMainOnline.isEnabled = userExists
        binding.btnMainOnline3.isEnabled = userExists

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                val aboutLayout = layoutInflater.inflate(
                    R.layout.about_layout,
                    binding.root,
                    false
                ) as LinearLayout
                AlertDialog.Builder(this)
                    .setView(aboutLayout)
                    .setPositiveButton(R.string.okay) { d, _ ->
                        d.dismiss()
                    }
                    .show()
            }
            R.id.sign_in_button -> {
                if (app.user == null)
                    signIn()
            }
            R.id.user -> {
                if (app.user != null) {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.logout -> {
                if (app.user != null)
                    signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //endregion

    //region Firebase Authentication
    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        db = Firebase.firestore
        updateUI(currentUser)
    }

    private fun signIn() {
        googleSignInActivityForResult.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    createAndShowLoginFailedDialog()
                }
            }
    }

    private fun signOut() {
        googleSignInClient.revokeAccess()
        auth.signOut()
        app.user = null
        invalidateOptionsMenu()
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null /*&& app.user == null*/) {
            val task = db.collection("users").document(user.email!!).get()
            task.addOnCompleteListener {
                onFirestoreUserInfoComplete(it, user)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onFirestoreUserInfoComplete(it: Task<DocumentSnapshot>, user: FirebaseUser) {
        if (it.isSuccessful) {
            thread {
                val doc = it.result
                if (!doc.exists()) {
                    val photoStream =
                        URL(
                            user.photoUrl.toString().replace("s96-c", "s120-c")
                        ).openStream()
                    val avatarByteArray = photoStream.readBytes()
                    val username = user.displayName!!
                    val topScore = ArrayList<TopScore>()
                    val dbUser = hashMapOf(
                        "username" to username,
                        "avatar" to avatarByteArray.toBase64(),
                        "topScore" to topScore
                    )
                    app.user = Profile(
                        PlayerData(
                            username,
                            avatarByteArray.toBitmap()
                        ),
                        user.email!!,
                        topScore
                    )
                    db.collection("users").document(user.email!!).set(dbUser)
                } else {
                    val userData = doc.data!!

                    val topScores =
                        parseFirestoreTopScoreArray(userData["topScore"] as ArrayList<HashMap<String, Any>>)

                    app.user = Profile(
                        PlayerData(
                            userData["username"] as String,
                            (userData["avatar"] as String).toBitmap()
                        ),
                        user.email!!,
                        topScores
                    )
                }
                runOnUiThread {
                    invalidateOptionsMenu()
                }
            }
        }
    }
    //endregion

    private fun createAndShowCreateOrJoinRoomDialog(titleRes: Int, gameMode: GameMode) {
        AlertDialog.Builder(this).setMessage(R.string.create_join_room)
            .setCancelable(false)
            .setNeutralButton(R.string.cancel, null)
            .setPositiveButton(R.string.create) { _, _ ->
                val intent = Intent(this, GameSettingsActivity::class.java)
                intent.putExtra("GameMode", gameMode.ordinal)
                intent.putExtra("CreateServer", true)
                startActivity(intent)
            }
            .setNegativeButton(R.string.join) { _, _ ->
                val intent = Intent(this, GameSettingsActivity::class.java)
                intent.putExtra("GameMode", gameMode.ordinal)
                intent.putExtra("CreateServer", false)
                startActivity(intent)
            }
            .setTitle(titleRes)
            .show()
    }

    private fun createAndShowLoginFailedDialog() {
        AlertDialog.Builder(this).setMessage(getString(R.string.google_sign_in_failed))
            .setPositiveButton(getString(R.string.okay)) { d, _ ->
                d.dismiss()
            }
            .setTitle(getString(R.string.google_sign_in))
            .show()
    }

    private fun hasNetworkConnection(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkCapabilities(network).apply {
                if (this?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                    this?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                )
                    return true
            }
        }
        return false
    }
}
