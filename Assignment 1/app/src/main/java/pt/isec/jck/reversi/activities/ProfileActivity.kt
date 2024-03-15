package pt.isec.jck.reversi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.application.ReversiApplication
import pt.isec.jck.reversi.databinding.ActivityProfileBinding
import pt.isec.jck.reversi.logic.adapters.ProfileTopScoreListAdapter
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var app: ReversiApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        app = application as ReversiApplication
        setContentView(binding.root)

        val appUser = app.user!!

        binding.imgProfilePicture.setImageBitmap(appUser.playerData.avatar)
        binding.tvProfileName.text = appUser.playerData.username
        binding.tvProfileEmail.text = appUser.email
        binding.lvProfileTopScore.adapter =
            ProfileTopScoreListAdapter(appUser, layoutInflater, this)
    }

    //region Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        try {
            menuInflater.inflate(R.menu.edit_menu, menu)
        } catch (ex: Exception) {
            return false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                val intent = Intent(this, EditProfileActivity::class.java)
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
    }
    //endregion
}
