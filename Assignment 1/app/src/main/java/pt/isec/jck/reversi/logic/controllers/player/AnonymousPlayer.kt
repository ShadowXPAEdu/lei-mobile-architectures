package pt.isec.jck.reversi.logic.controllers.player

import android.graphics.Bitmap
import pt.isec.jck.reversi.logic.models.game.Disc
import pt.isec.jck.reversi.logic.models.player.PlayerData

class AnonymousPlayer(
    avatar: Bitmap,
    disc: Disc,
    usingSpecialPieces: Boolean,
) : HumanPlayer(PlayerData("Anonymous", avatar), disc, usingSpecialPieces, true)
