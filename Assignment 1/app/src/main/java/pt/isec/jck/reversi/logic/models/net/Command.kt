package pt.isec.jck.reversi.logic.models.net

enum class Command {

    /**
     * Client sends the command to tell the server it is connecting to the server
     * Client to Server ->
     * { "Command": "Connect", "Payload": { "Username": "Player", "Avatar": "Base64Bitmap" }}
     *
     * Server acknowledges Client connection by sending a Connect command back with all the Players
     * on the Waiting Room (The Client that is connecting is sent as index 0 with the Disc that was
     * given by the Server)
     * Server sends to all the other Clients the information about the Client that is connecting
     * Server to Client ->
     * { "Command": "Connect", "Payload": [
     *  { "Username": "Player", "Avatar": "Base64Bitmap", "Ready": false, "Disc": "Black" },
     *  { "Username": "Player 2", "Avatar": "Base64Bitmap", "Ready": true, "Disc": "White" },
     *  ...] }
     */
    Connect,

    /**
     * Client sends to Server that the player wants to toggle ready
     * Client to Server ->
     * { "Command": "Ready" }
     *
     * Server sends to all Clients that a player toggled ready
     * Server to Client ->
     * { "Command": "Ready", "Payload": "White" }
     */
    Ready,

    /**
     * Server sends to all Clients that the game is starting with all the settings, game mode
     * and who is the first to play
     * Server to Client ->
     * { "Command": "GameStart", "Payload":
     *  { "SpecialPieces": true, "AutoSkip": false, "ShowPlaceable": true,
     *  "InfiniteSpecialPieces": false, "GameMode": "Online", "Player": "Black" } }
     */
    GameStart,

    /**
     * Server sends to a Client a list of placeable pieces
     * Server to Client ->
     * { "Command": "ShowPlaceablePieces", "Payload": [{ "x": 1, "y": 4 }, ...] }
     */
    ShowPlaceablePieces,

    /**
     * Server orders the Client to clear the placeable pieces
     * Server to Client ->
     * { "Command": "ClearPlaceablePieces" }
     */
    ClearPlaceablePieces,

    /**
     * Server sends to all Clients the updated score of a player
     * Server to Client ->
     * { "Command": "UpdateScore", "Payload": { "Player": "Black", "Score": 10 } }
     */
    UpdateScore,

    /**
     * Client sends place piece coordinates to the Server
     * Client to Server ->
     * { "Command": "PlacePiece", "Payload": { "x": 6, "y": 1 } }
     *
     * Server sends to all Clients the piece that was placed
     * Server to Client ->
     * { "Command": "PlacePiece", "Payload": { "Player": "Black", "x": 3, "y": 5 } }
     */
    PlacePiece,

    /**
     * Clients sends place bomb coordinates to the Server
     * Client to Server ->
     * { "Command": "PlaceBomb", "Payload": { "x": 6, "y": 1 } }
     *
     * Server sends to all Clients the bomb that was placed
     * Server to Client ->
     * { "Command": "PlaceBomb", "Payload": { "Player": "White", "x": 1, "y": 5 } }
     */
    PlaceBomb,

    /**
     * Server sends to all Clients that the current player has begun replacing pieces
     * Server to Client ->
     * { "Command": "BeginReplacePiece" }
     */
    BeginReplacePiece,

    /**
     * Server sends to all Clients that the current player selected a piece to replace
     * Server to Client ->
     * { "Command": "AddReplacePiece", "Payload": { "Player": "Black", "x": 4, "y": 2 } }
     */
    AddReplacePiece,

    /**
     * Server sends to all Clients that the current player has removed a piece to replace
     * Server to Client ->
     * { "Command": "RemoveReplacePiece", "Payload": { "Player": "White", "x": 5, "y": 2 } }
     */
    RemoveReplacePiece,

    /**
     * Server sends to all Clients the pieces to replace ([] or [{piece1}, {piece2}, {piece3}])
     * Server to Client ->
     * { "Command": "EndReplacePiece", "Payload": [{ "Player": "Black", "x": 2, "y": 5 },
     *  { "Player": "Black", "x": 5, "y": 1 }, { "Player": "White", "x": 4, "y": 7 }] }
     */
    EndReplacePiece,

    /**
     * Server sends to all Clients information about a player's bomb status (a.k.a. CanPlaceBomb)
     * Server to Client ->
     * { "Command": "PlayerBombStatusChange", "Payload": { "Player": "White", "Status": true } }
     */
    PlayerBombStatusChange,

    /**
     * Server sends to all Clients information about a player's replace piece status
     * (a.k.a. CanReplacePieces)
     * Server to Client ->
     * { "Command": "PlayerReplaceStatusChange", "Payload": { "Player": "White", "Status": true } }
     */
    PlayerReplaceStatusChange,

    /**
     * Server tells a Client that they need to skip the play
     * Server to Client ->
     * { "Command": "PlayerSkip" }
     */
    PlayerSkip,

    /**
     * Server sends to all Clients the previous and current player
     * Server to Client ->
     * { "Command": "CurrentPlayer", "Payload": { "OldPlayer": "White", "NewPlayer": "Black" } }
     */
    CurrentPlayer,

    /**
     * Server sends to all Clients that the game has ended
     * Server to Client ->
     * { "Command": "GameOver", "Payload": "Black" }
     */
    GameOver,

    /**
     * Client sends to server they wish to disconnect
     * Client to Server ->
     * { "Command": "Disconnect" }
     *
     * Server sends to all Clients that a client has disconnected
     * Server to Client ->
     * { "Command": "Disconnect", "Payload": "White" }
     */
    Disconnect,

    /**
     * Server sends to all Clients that a player has disconnected (Willingly or Crashed) and all
     * other players need to disconnect
     * Server to Client ->
     * { "Command": "ForceDisconnect" }
     */
    ForceDisconnect
}
