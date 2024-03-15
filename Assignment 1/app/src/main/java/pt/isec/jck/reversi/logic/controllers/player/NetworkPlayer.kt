package pt.isec.jck.reversi.logic.controllers.player

import pt.isec.jck.reversi.logic.models.game.Disc
import pt.isec.jck.reversi.logic.models.player.PlayerData
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class NetworkPlayer(
    val socket: Socket,
    playerData: PlayerData,
    disc: Disc,
    usingSpecialPieces: Boolean,
    ready: Boolean = false
) : HumanPlayer(playerData, disc, usingSpecialPieces, ready) {
    val outputStream: ObjectOutputStream = ObjectOutputStream(socket.getOutputStream())
    val inputStream: ObjectInputStream = ObjectInputStream(socket.getInputStream())
}
