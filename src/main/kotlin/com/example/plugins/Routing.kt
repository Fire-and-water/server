package com.example.plugins

import UsersDB
import com.example.UserSession
import com.example.include.*
import io.ktor.server.routing.*

import io.ktor.server.application.*
import io.ktor.server.response.*

import io.ktor.server.sessions.*

import kotlinx.serialization.Serializable
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

//val UsersDB() : UsersDB() = UsersDB()()

suspend fun getUserSession(call: ApplicationCall): UserSession? {
    val userSession: UserSession? = call.sessions.get()
    if (userSession == null) {
        call.respondText(
            Json.encodeToString(
                StatusWithMessage(
                    0,
                    "Пользователь не авторизован."
                )
            )
        )
    }
    return userSession
}

suspend fun getUserByUserSession(
    call: ApplicationCall,
    userSession: UserSession
): User? {
    val id = userSession.id
    val usr = UsersDB().getUserById(id)
    if (usr == null) {
        call.respondText(
            Json.encodeToString(
                StatusWithMessage(
                    2,
                    "Пользователь удалился"
                )
            )
        )
    }
    return usr
}

fun Application.configureRouting() {


    routing {

        get("/updateTop") {
            UsersDB().topUpdate()
            call.respondText("ok!")
        }

        get("getTop{id}") {

            // Валидация авторизации и существования аккаунта
            /*            val userSession = getUserSession(call) ?: return@get
                        val usr = getUserByUserSession(call, userSession) ?: return@get
            */
            @Serializable
            data class Top(val top: List<UserPlace>);

            call.respondText(
                Json.encodeToString(
                    Top(
                        UsersDB().getTop(
                            call.parameters["id"]!!.toInt()
                        )
                    )
                )
            )
        }

        get("/getMyHistoryGames") {

            // Валидация авторизации и существования аккаунта
            val userSession =
                getUserSession(call) ?: return@get
            val usr =
                getUserByUserSession(call, userSession)
                    ?: return@get

            call.respondText(
                Json.encodeToString(
                    UsersDB().getGameHistory(
                        usr.id
                    )
                )
            )
        }

        get("/getLevels") {

            // Валидация авторизации и существования аккаунта
            val userSession =
                getUserSession(call) ?: return@get
            val usr =
                getUserByUserSession(call, userSession)
                    ?: return@get

            call.respondText(
                Json.encodeToString(
                    UsersDB().getLevels(
                        usr.id
                    )
                )
            )
        }

        get("/getLevelById{id}") {

            call.respondText(UsersDB().getLevelById(call.parameters["id"]!!.toInt())!!);
        }

        get("/myInfo") {

            // Валидация авторизации и существования аккаунта
            val userSession =
                getUserSession(call) ?: return@get
            val usr =
                getUserByUserSession(call, userSession)
                    ?: return@get

            call.respondText(
                Json.encodeToString(
                    UserInfo(
                        usr.id,
                        usr.nickName,
                        usr.eMail,
                        usr.status
                    )
                )
            )

        }

        get("/userInfo{id}") {

            val usr =
                UsersDB().getUserById(call.parameters["id"]!!.toInt())
            if (usr == null) {
                call.respondText(
                    Json.encodeToString(
                        StatusWithMessage(
                            2,
                            "Пользователя нет такого увы"
                        )
                    )
                )
                return@get
            }

            call.respondText(
                Json.encodeToString(
                    UserInfo(
                        usr.id,
                        usr.nickName,
                        usr.eMail,
                        usr.status
                    )
                )
            )


        }


        get("/isFreeEmail{email}") {

            val eMail = call.parameters["eMail"]
            val isFree = UsersDB().isFreeEmail(eMail!!)

            @Serializable
            data class IsFreeEmailRespond(
                val eMail: String,
                val isFree: Boolean
            )

            call.respondText(
                Json.encodeToString(
                    IsFreeEmailRespond(eMail, isFree)
                )
            )

        }

        get("/authByEmail{email, password}") {
            println(call.parameters["password"]!!)
            //println(Base64.getDecoder().decode((call.parameters["password"]!!).toByteArray()))
            val usr =
                UsersDB().getUserByEmail(call.parameters["email"]!!)
            val res: StatusWithMessage = if (usr == null) {
                StatusWithMessage(
                    0,
                    "Пользователя с такой почтой не существует!"
                )
            } else if (usr.password != call.parameters["password"]!!) {
                StatusWithMessage(2, "Пароль неверный!")
            } else {
                StatusWithMessage(1, "Successful!")
            }
            if (res.status == 1) {
                call.sessions.set(UserSession(usr!!.id, 1))
                @Serializable
                data class UserWithStatus(
                    val status: Int,
                    val user: User
                );

                call.respondText(
                    Json.encodeToString(
                        UserWithStatus(1, usr!!)
                    )
                );
            } else {
                call.respondText(Json.encodeToString(res));
            }


        }

        get("/getToken") {

            // Валидация авторизации и существования аккаунта
            val userSession =
                getUserSession(call) ?: return@get
            val usr =
                getUserByUserSession(call, userSession)
                    ?: return@get

            @Serializable
            data class TokenRespond(
                val status: Int,
                val msg: String,
                val id: Int,
                val token: String
            )

            call.respondText(
                Json.encodeToString(
                    TokenRespond(
                        1,
                        "Ok!",
                        usr.id,
                        usr.secretKey
                    )
                )
            )

        }


        get("/changePassword{oldPassword, newPassword}") {
            // Валидация авторизации и существования аккаунта
            val userSession =
                getUserSession(call) ?: return@get
            val usr =
                getUserByUserSession(call, userSession)
                    ?: return@get

            if (usr.password != call.parameters["oldPassword"]) {
                call.respondText(
                    Json.encodeToString(
                        StatusWithMessage(
                            3,
                            "Старый пароль другой не совпадает"
                        )
                    )
                )
                return@get
            }

            UsersDB().updatePassword(
                usr.id,
                call.parameters["newPassword"]!!
            )
            call.respondText(
                Json.encodeToString(
                    StatusWithMessage(1, "ok")
                )
            )

        }

        get("/changeStatus{status}") {
            // Валидация авторизации и существования аккаунта
            val userSession =
                getUserSession(call) ?: return@get
            val usr =
                getUserByUserSession(call, userSession)
                    ?: return@get

            UsersDB().updateStatus(
                usr.id,
                call.parameters["status"]!!
            )
            call.respondText(
                Json.encodeToString(
                    StatusWithMessage(1, "ok")
                )
            )
        }



        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondText(
                Json.encodeToString(
                    StatusWithMessage(1, "logout complete")
                )
            )
        }

        get("/isFreeNickName{nickName}") {
            val nickName = call.parameters["nickname"]
            val isFree =
                UsersDB().isFreeNickName(nickName!!)

            @Serializable
            data class AnswerNick(
                val nickName: String,
                val isFree: Boolean
            )
            call.respondText(
                Json.encodeToString(
                    AnswerNick(
                        nickName,
                        isFree
                    )
                )
            )
        }

        get("/registerByEmail{nickname, email, password}") {

            @Serializable
            data class UserRespond(
                val status: Int,
                val msg: String,
                val id: Int
            )

            val id = UsersDB().registerUserByEmail(
                call.parameters["nickname"]!!,
                call.parameters["email"]!!,
                call.parameters["password"]!!
            )

            val ans: UserRespond = if (id == -1) {
                UserRespond(
                    0,
                    "Login or email is already in use",
                    id
                )
            } else {
                UserRespond(1, "Successful!", id)
            }

            call.respondText(Json.encodeToString(ans))
        }

        get("/getMyFriends{id}") {

            val userDB = UsersDB()

            val friendsIds = userDB.getFriendsIdsById(call.parameters["id"]!!.toInt())

            val friendsNames = friendsIds.map {
                userDB.getUserById(it)?.nickName
            }

            call.respondText(Json.encodeToString(friendsNames))
        }

    }
}
