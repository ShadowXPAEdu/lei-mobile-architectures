package pt.isec.jck.reversi.logic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.application.ReversiApplication
import pt.isec.jck.reversi.logic.models.player.IPlayer

class WaitingRoomPlayerListAdapter(
    private val app: ReversiApplication,
    private val layoutInflater: LayoutInflater
) : BaseAdapter() {

    override fun getCount(): Int {
        return app.game?.getPlayerCount() ?: 0
    }

    override fun getItem(position: Int): IPlayer? {
        return app.game?.getPlayerByIndex(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val player = getItem(position)

        if (player != null) {
            val view: View = convertView ?: layoutInflater.inflate(
                R.layout.waitingroom_player_layout,
                parent,
                false
            )

            val playerName = view.findViewById<TextView>(R.id.tvPlayerName)
            val playerReady = view.findViewById<CheckBox>(R.id.cbPlayerReady)
            val playerImage = view.findViewById<ImageView>(R.id.imgPlayerAvatar)
            val playerDisc = view.findViewById<ImageView>(R.id.imgPlayerDisc)

            playerName.text = player.getUsername()
            playerReady.isChecked = player.isReady()
            playerImage.setImageBitmap(player.getAvatar())
            playerDisc.setBackgroundColor(player.getDisc().color)

            return view
        }
        return null
    }
}
