package pt.isec.jck.reversi.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.application.ReversiApplication
import pt.isec.jck.reversi.databinding.ActivityEditProfileBinding
import pt.isec.jck.reversi.logic.controllers.utils.scale
import pt.isec.jck.reversi.logic.controllers.utils.toBase64

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var app: ReversiApplication

    private lateinit var db: FirebaseFirestore

    private val cameraActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                app.imageBitmap = (it.data?.extras?.get("data") as Bitmap).scale()
            }
        }

    override fun onStart() {
        super.onStart()
        db = Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        app = application as ReversiApplication
        setContentView(binding.root)

        binding.etEditProfileName.setText(app.user!!.playerData.username)

        binding.btnEditProfilePicture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraActivityForResult.launch(takePictureIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.imgEditProfilePicture.setImageBitmap(
            app.imageBitmap ?: app.user!!.playerData.avatar
        )
    }

    //region Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        try {
            menuInflater.inflate(R.menu.save_menu, menu)
        } catch (ex: Exception) {
            return false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                val appUser = app.user!!

                appUser.playerData.username = binding.etEditProfileName.text.toString()
                db.collection("users").document(appUser.email)
                    .update("username", appUser.playerData.username)
                if (app.imageBitmap != null) {
                    appUser.playerData.avatar = app.imageBitmap!!.scale()
                    db.collection("users").document(appUser.email)
                        .update("avatar", appUser.playerData.avatar.toBase64())
                }

                onSupportNavigateUp()
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
        app.imageBitmap = null
    }
    //endregion
}
