package com.example.game

import com.example.ROLE
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameResult {

}

class MainGame(val level: Int) {

    private lateinit var fire : Player
    private lateinit var water : Player
    private val widthScaleCoefficient: Int = 1
    private val heightScaleCoefficient: Int = 1

    init {
        val listOfLevelObjects : MutableList<LevelObject> = mutableListOf()
        listOfLevelObjects.add(
            LevelObject(
                0f * widthScaleCoefficient,
                0f * heightScaleCoefficient,
                1920f * widthScaleCoefficient,
                100f * heightScaleCoefficient,
            )
        )
        listOfLevelObjects.add(
            LevelObject(
                0f * widthScaleCoefficient,
                0f * heightScaleCoefficient,
                100f * widthScaleCoefficient,
                1080f * heightScaleCoefficient
            )
        )
        listOfLevelObjects.add(
            LevelObject(
                1820f * widthScaleCoefficient,
                0f * heightScaleCoefficient,
                100f * widthScaleCoefficient,
                1080f * heightScaleCoefficient
            )
        )
        listOfLevelObjects.add(
            LevelObject(
                0f * widthScaleCoefficient,
                880f * heightScaleCoefficient,
                1920f * widthScaleCoefficient,
                200f * heightScaleCoefficient
            )
        )
        listOfLevelObjects.add(
            LevelObject(
                1120f * widthScaleCoefficient,
                480f * heightScaleCoefficient,
                800f * widthScaleCoefficient,
                50f * heightScaleCoefficient
            )
        )
        listOfLevelObjects.add(
            LevelObject(
                0f * widthScaleCoefficient,
                280f * heightScaleCoefficient,
                800f * widthScaleCoefficient,
                50f * heightScaleCoefficient
            )
        )
        listOfLevelObjects.add(
            LevelObject(
                0f * widthScaleCoefficient,
                640f * heightScaleCoefficient,
                800f * widthScaleCoefficient,
                50f * heightScaleCoefficient
            )
        )
        val gl : GameLevel = GameLevel(listOfLevelObjects);
        fire = Player(500.0f, 300.0f, 100.0f * widthScaleCoefficient, 100.0f * heightScaleCoefficient, gl)
        water = Player(1000.0f, 300.0f, 100.0f * widthScaleCoefficient, 100.0f * heightScaleCoefficient, gl)
    }

    private fun step(player: Player, step: String) {
        when(step) {
            "left" -> {
                player.moveLeft()
            }
            "right" -> {
                player.moveRight()
            }
            "jump" -> {
                player.jump()
            }
        }
    }

    fun getCoordinates() : String {
        @Serializable
        data class Point(val posX: Float, val posY: Float);

        @Serializable
        data class Coordinates(val water : Point, val fire : Point, val what: String);

        return Json.encodeToString(Coordinates(Point(water.posX, water.posY), Point(fire.posX, fire.posY), "coordinates"));
    }


    fun step(step: String, role: ROLE) {
        when(role) {
            ROLE.FIRE -> step(fire, step)
            ROLE.WATER -> step(water, step)
        }
    }

    fun serialize() : String {
        @Serializable
        data class SerializeMap(val what : String);

        return Json.encodeToString(SerializeMap("map"));
    }

    fun isClose() {

    }

    fun getResults() : GameResult = TODO()

}