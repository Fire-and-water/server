package com.example

class Game(idCreator: Int, level : Int) {
    var gameId : Int = 0;
    var player1 : Int = idCreator;
    var player2 : Int = -1;
    init {
        gameId = (100_000..999_999).random(); // TODO: хардкод((
    }
    fun connect(player2 : Int) {
        this.player2 = player2;
    }
}