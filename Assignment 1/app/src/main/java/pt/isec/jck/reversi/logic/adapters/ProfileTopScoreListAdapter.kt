package pt.isec.jck.reversi.logic.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import pt.isec.jck.reversi.R
import pt.isec.jck.reversi.logic.models.player.PlayerData
import pt.isec.jck.reversi.logic.models.player.Score
import pt.isec.jck.reversi.logic.models.player.TopScore
import pt.isec.jck.reversi.logic.models.profile.Profile

class ProfileTopScoreListAdapter(
    private val profile: Profile,
    private val layoutInflater: LayoutInflater,
    private val context: Context
) : BaseAdapter() {

    init {
        profile.topScore.sortBy { t -> t.playerScore.score }
    }

    private val d = DateFormat.getLongDateFormat(context)
    private val t = DateFormat.getTimeFormat(context)

    override fun getCount(): Int {
        return profile.topScore.size
    }

    override fun getItem(position: Int): TopScore {
        return profile.topScore[(count - 1) - position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val topScore = getItem(position)

        val view =
            convertView ?: layoutInflater.inflate(R.layout.top_score_layout, parent, false)
        val data = view.findViewById<TextView>(R.id.tvData)

        data.text = context.getString(
            R.string.date_time,
            d.format(topScore.date),
            t.format(topScore.date)
        )

        val llGamePlayer1 = view.findViewById<LinearLayout>(R.id.llGamePlayer1)
        val llGamePlayer2 = view.findViewById<LinearLayout>(R.id.llGamePlayer2)
        val llGamePlayer3 = view.findViewById<LinearLayout>(R.id.llGamePlayer3)

        val llGamePlayers =
            arrayOf(llGamePlayer1, llGamePlayer2, llGamePlayer3)

        addPlayerToView(profile.playerData, topScore.playerScore, llGamePlayer1)

        for (i in topScore.adversaryScore.indices) {
            addPlayerToView(
                PlayerData(topScore.adversaryScore[i].first),
                topScore.adversaryScore[i].second,
                llGamePlayers[i + 1]
            )
        }

        return view
    }

    @SuppressLint("InflateParams")
    private fun addPlayerToView(playerData: PlayerData, score: Score, llGamePlayer: LinearLayout) {
        llGamePlayer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.top_score_player_layout, null)

        val playerAvatar = view.findViewById<ImageView>(R.id.imgPlayerAvatar)
        val playerName = view.findViewById<TextView>(R.id.tvPlayerName)
        val playerScore = view.findViewById<TextView>(R.id.tvPlayerScore)

        playerAvatar.setImageBitmap(playerData.avatar)
        playerName.text = playerData.username
        playerScore.text = score.toString()
        llGamePlayer.addView(view)
    }
}
