package pt.isec.jck.reversi.logic.models.game

class Board(val gameMode: GameMode) {

    val board = arrayOfNulls<List<Piece>>(gameMode.boardSize).mapIndexed { x, _ ->
        arrayOfNulls<Piece>(gameMode.boardSize).mapIndexed { y, _ ->
            Piece(x, y, Disc.Empty)
        }
    }

    init {
        if (gameMode.numPlayers == 2 && gameMode.boardSize == 8) {
            // Normal Configuration
            placePiece(3, 3, Disc.White)
            placePiece(4, 4, Disc.White)
            placePiece(3, 4, Disc.Black)
            placePiece(4, 3, Disc.Black)
        } else if (gameMode.numPlayers == 3 && gameMode.boardSize == 10) {
            // 1v1v1 Configuration
            placePiece(2, 4, Disc.White)
            placePiece(3, 5, Disc.White)
            placePiece(6, 3, Disc.White)
            placePiece(7, 2, Disc.White)
            placePiece(2, 5, Disc.Black)
            placePiece(3, 4, Disc.Black)
            placePiece(6, 6, Disc.Black)
            placePiece(7, 7, Disc.Black)
            placePiece(6, 2, Disc.Gold)
            placePiece(7, 3, Disc.Gold)
            placePiece(6, 7, Disc.Gold)
            placePiece(7, 6, Disc.Gold)
        }
    }

    fun canPlace(piece: Piece): Boolean {
        return board[piece.x][piece.y].disc == Disc.Empty && getReversiblePieces(piece).isNotEmpty()
    }

    fun getPlaceablePieces(disc: Disc): List<Piece> {
        return board.flatten().filter { canPlace(Piece(it.x, it.y, disc)) }
    }

    fun canPlacePiece(disc: Disc): Boolean {
        return getPlaceablePieces(disc).isNotEmpty()
    }

    fun countPieces(disc: Disc): Int {
        return board.flatten().count { it.disc == disc }
    }

    fun isGameOver(): Boolean {
        var temp = true
        for (i in 0 until gameMode.numPlayers) {
            temp = temp && !canPlacePiece(Disc.values()[i + Disc.Empty.ordinal + 1])
        }
        return temp
    }

    fun placePiece(piece: Piece) {
        placePiece(piece.x, piece.y, piece.disc)
    }

    private fun placePiece(x: Int, y: Int, disc: Disc) {
        if (x < 0 || x >= gameMode.boardSize || y < 0 || y >= gameMode.boardSize)
            return
        board[x][y].disc = disc
    }

    fun placeBomb(piece: Piece) {
        placePiece(piece.x - 1, piece.y - 1, Disc.Empty)
        placePiece(piece.x - 1, piece.y, Disc.Empty)
        placePiece(piece.x - 1, piece.y + 1, Disc.Empty)
        placePiece(piece.x, piece.y + 1, Disc.Empty)
        placePiece(piece.x + 1, piece.y + 1, Disc.Empty)
        placePiece(piece.x + 1, piece.y, Disc.Empty)
        placePiece(piece.x + 1, piece.y - 1, Disc.Empty)
        placePiece(piece.x, piece.y - 1, Disc.Empty)
        placePiece(piece)
    }

    fun replaceDiscs(piece1: Piece, piece2: Piece, piece3: Piece) {
        placePiece(piece1.x, piece1.y, piece3.disc)
        placePiece(piece2.x, piece2.y, piece3.disc)
        placePiece(piece3.x, piece3.y, piece1.disc)
    }

    fun getDiscOnPosition(x: Int, y: Int): Disc {
        return board[x][y].disc
    }

    fun getReversiblePieces(target: Piece): List<Piece> {
        return searchLeftPieces(target)
            .asSequence()
            .plus(searchRightPieces(target))
            .plus(searchTopPieces(target))
            .plus(searchBottomPieces(target))
            .plus(searchTopLeftPieces(target))
            .plus(searchBottomRightPieces(target))
            .plus(searchTopRightPieces(target))
            .plus(searchBottomLeftPieces(target))
            .toList()
    }

    private fun searchLeftPieces(target: Piece): List<Piece> {
        if (target.x == 0) return emptyList()
        val leftPieces = board.take(target.x).map { it[target.y] }.reversed()
        return getReversiblePieces(target, leftPieces)
    }

    private fun searchRightPieces(target: Piece): List<Piece> {
        if (target.x + 1 > gameMode.boardSize - 1) return emptyList()
        val rightPieces = board.drop(target.x + 1).map { it[target.y] }
        return getReversiblePieces(target, rightPieces)
    }

    private fun searchTopPieces(target: Piece): List<Piece> {
        if (target.y == 0) return emptyList()
        val topPieces = board[target.x].take(target.y).reversed()
        return getReversiblePieces(target, topPieces)
    }

    private fun searchBottomPieces(target: Piece): List<Piece> {
        if (target.y + 1 > gameMode.boardSize - 1) return emptyList()
        val bottomPieces = board[target.x].drop(target.y + 1)
        return getReversiblePieces(target, bottomPieces)
    }

    private fun searchTopLeftPieces(target: Piece): List<Piece> {
        if (target.x == 0 || target.y == 0) return emptyList()
        val topLeftPieces = board.flatten()
            .filter { it.x < target.x && it.y < target.y }
            .filter { it.x - it.y == target.x - target.y }
            .reversed()
        return getReversiblePieces(target, topLeftPieces)
    }

    private fun searchBottomRightPieces(target: Piece): List<Piece> {
        if (target.x + 1 > gameMode.boardSize - 1 || target.y + 1 > gameMode.boardSize - 1) return emptyList()
        val bottomRightPieces = board.flatten()
            .filter { it.x > target.x && it.y > target.y }
            .filter { it.x - it.y == target.x - target.y }
        return getReversiblePieces(target, bottomRightPieces)
    }

    private fun searchTopRightPieces(target: Piece): List<Piece> {
        if (target.x + 1 > gameMode.boardSize || target.y == 0) return emptyList()
        val topRightPieces = board.flatten()
            .filter { it.x > target.x && it.y < target.y }
            .filter { it.x + it.y == target.x + target.y }
        return getReversiblePieces(target, topRightPieces)
    }

    private fun searchBottomLeftPieces(target: Piece): List<Piece> {
        if (target.x == 0 || target.y + 1 > gameMode.boardSize - 1) return emptyList()
        val bottomLeftPieces = board.flatten()
            .filter { it.x < target.x && it.y > target.y }
            .filter { it.x + it.y == target.x + target.y }
            .reversed()
        return getReversiblePieces(target, bottomLeftPieces)
    }

    private fun getReversiblePieces(target: Piece, pieces: List<Piece>): List<Piece> {
        val end = pieces.indexOfFirst { it.disc == target.disc }
        if (end == -1) return emptyList()
        val reversiblePieces = pieces.take(end)
        if (reversiblePieces.all { target.disc.adversary().contains(it.disc) }) {
            return reversiblePieces.map { Piece(it.x, it.y, target.disc) }
        }
        return emptyList()
    }
}
