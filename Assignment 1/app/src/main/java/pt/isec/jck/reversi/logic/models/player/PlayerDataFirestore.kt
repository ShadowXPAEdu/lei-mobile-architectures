package pt.isec.jck.reversi.logic.models.player

import pt.isec.jck.reversi.logic.controllers.utils.toBase64

data class PlayerDataFirestore(val username: String, val avatar: String) {
    constructor(playerData: PlayerData) : this(playerData.username, playerData.avatar.toBase64())
}
