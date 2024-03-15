package pt.isec.jck.reversi.logic.models.player

import android.graphics.Bitmap
import pt.isec.jck.reversi.logic.controllers.utils.toBitmap

data class PlayerData(var username: String, var avatar: Bitmap) {
    constructor(playerDataFirestore: PlayerDataFirestore) : this(
        playerDataFirestore.username,
        playerDataFirestore.avatar.toBitmap()
    )
}
