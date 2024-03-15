package pt.isec.jck.reversi.application

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.logic.models.game.IGame
import pt.isec.jck.reversi.logic.models.player.IPlayer
import pt.isec.jck.reversi.logic.models.profile.Profile

class ReversiApplication : Application() {

    lateinit var defaultPlayerAvatar: Bitmap
        private set

    var game: IGame? = null
    var user: Profile? = null
    var player: IPlayer? = null

    var logs: ArrayList<String>? = null

    var imageBitmap: Bitmap? = null

    override fun onCreate() {
        super.onCreate()
        val vectorDrawable =
            AppCompatResources.getDrawable(this, R.drawable.ic_avatar) as Drawable
        defaultPlayerAvatar = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(defaultPlayerAvatar)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
    }
}
