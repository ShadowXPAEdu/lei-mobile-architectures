package pt.isec.jck.reversi.logic.models.player

data class Score(var score: Int) {
    override fun toString(): String {
        return score.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Score) return false

        if (score != other.score) return false

        return true
    }

    override fun hashCode(): Int {
        return score
    }
}
