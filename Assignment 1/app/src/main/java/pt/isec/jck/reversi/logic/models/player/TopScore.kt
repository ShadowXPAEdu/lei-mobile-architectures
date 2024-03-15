package pt.isec.jck.reversi.logic.models.player

import java.util.Date

data class TopScore(
    val playerScore: Score,
    val adversaryScore: List<Pair<PlayerDataFirestore, Score>>,
    val date: Date = Date()
)
