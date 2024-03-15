package pt.isec.jck.reversi.logic.controllers.utils

import com.google.firebase.Timestamp
import pt.isec.jck.reversi.logic.models.player.PlayerDataFirestore
import pt.isec.jck.reversi.logic.models.player.Score
import pt.isec.jck.reversi.logic.models.player.TopScore

@Suppress("UNCHECKED_CAST")
fun parseFirestoreTopScoreArray(firebaseTopScore: ArrayList<HashMap<String, Any>>): ArrayList<TopScore> {
    val topScores = ArrayList<TopScore>()

    firebaseTopScore.forEach {
        val date = (it["date"] as Timestamp).toDate()
        val playerScore =
            ((it["playerScore"] as HashMap<String, Long>)["score"]!!).toInt()
        val adversaryScores =
            ArrayList<Pair<PlayerDataFirestore, Score>>()
        (it["adversaryScore"] as ArrayList<HashMap<String, HashMap<String, Any>>>).forEach {
            val username = it["first"]!!["username"] as String
            val avatar =
                it["first"]!!["avatar"] as String
            val score = (it["second"]!!["score"] as Long).toInt()

            adversaryScores.add(
                Pair(
                    PlayerDataFirestore(username, avatar),
                    Score(score)
                )
            )
        }

        topScores.add(
            TopScore(
                Score(playerScore),
                adversaryScores,
                date
            )
        )
    }

    return topScores
}
