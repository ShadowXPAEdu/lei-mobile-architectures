package pt.isec.jck.reversi.logic.models.profile

import pt.isec.jck.reversi.logic.models.player.PlayerData
import pt.isec.jck.reversi.logic.models.player.TopScore

data class Profile(
    val playerData: PlayerData,
    val email: String,
    val topScore: ArrayList<TopScore> = ArrayList()
)
