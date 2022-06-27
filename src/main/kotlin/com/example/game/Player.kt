package com.example.game

class Player(
  //  private val context: Context,
    var posX: Float,
    var posY: Float,
    private val width: Float,
    private val height: Float,
    private val level: GameLevel
) {
    var movingRight: Boolean = false
    var movingLeft: Boolean = false
//    private var paint = Paint()
    var isJumping: Boolean = false
    var isOnGround: Boolean = true
    var tick: Int = 0
/*
    init {
        paint.color = Color.RED
    }

    fun draw(canvas: Canvas?) {
        canvas?.drawRect(posX, posY, posX + width, posY + height, paint)
    }
*/
    fun update() {
        if (isJumping && tick < 30) {
            jump()
            tick++
            isOnGround = false
        } else {
            tick = 0
            isJumping = false
            fall()
        }
        if (movingLeft && !movingRight) moveLeft()
        if (movingRight && !movingLeft) moveRight()
    }

    fun moveLeft() {
        updatePosX(-10F)
    }

    fun moveRight() {
        updatePosX(10F)
    }

    fun jump() {
        updatePosY(-10F)
    }

    private fun fall() {
        isOnGround = false
        updatePosY(10F)
    }

    private fun collidesWithObject(levelObject: LevelObject): Boolean {
        return !((posX + width < levelObject.x || posX > levelObject.x + levelObject.width) ||
                (posY + height < levelObject.y || posY > levelObject.y + levelObject.height))

    }

    fun updatePosX(x: Float) {
        posX += x
        for (levelObject in level.listOfLevelObjects) {
            if (collidesWithObject(levelObject)) {
                posX = if (x < 0) {
                    levelObject.x + levelObject.width + 1f
                } else {
                    levelObject.x - width - 1f
                }
            }
        }
    }

    fun updatePosY(y: Float) {
        posY += y
        var isCollided = false
        for (levelObject in level.listOfLevelObjects) {
            if (collidesWithObject(levelObject)) {
                isCollided = true
                if (y < 0) {
                    posY = levelObject.y + levelObject.height + 1f
                    isJumping = false
                } else {
                    posY = levelObject.y - height - 1f
                    isOnGround = true
                }
            }
        }
    }
}