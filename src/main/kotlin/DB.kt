import com.example.include.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@kotlinx.serialization.Serializable
data class UserWithPlace(val place: Int, val user: User)

open class DB(
    hostDB: String,
    portDB: Int,
    private var nameDB: String,
    private var userDB: String,
    private var passwordDB: String
) {

    private var connection =
        DriverManager.getConnection("jdbc:mysql://$userDB:$passwordDB@$hostDB:$portDB/$nameDB")

    fun getConnectionDB(): Connection {
        return connection
    }

}

class FireWaterDB : DB(
    "85.193.80.157",
    3306,
    "default_db",
    "gen_user",
    "Qazwsx23e47"
)

class UsersDB {
    private var db: FireWaterDB = FireWaterDB()

    private fun getStatement() =
        db.getConnectionDB().createStatement()

    private fun getRandomString(length: Int): String {
        val allowedChars =
            ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getUserById(id: Id): User? {
        val rs =
            getStatement().executeQuery("select * from `users` where `id`=$id")
        if (rs.next()) {
            return User(
                rs.getInt("id"),
                rs.getString("secretKey"),
                rs.getString("nickname"),
                rs.getString("photo"),
                rs.getString("photo"),
                rs.getString("eMail"),
                rs.getString("password")
            )
        }
        return null
    }

    fun getUserByEmail(email: String): User? {
        val rs =
            getStatement().executeQuery("select * from `users` where `email`='$email'")
        if (rs.next()) {
            return User(
                rs.getInt("id"),
                rs.getString("secretKey"),
                rs.getString("nickname"),
                rs.getString("photo"),
                rs.getString("photo"),
                rs.getString("eMail"),
                rs.getString("password")
            )
        }
        return null
    }

    fun isFreeNickName(nickName: String): Boolean {
        return !getStatement().executeQuery("select * from `users` where `nickname`='$nickName'")
            .next()
    }

    fun isFreeEmail(eMail: String): Boolean {
        return !getStatement().executeQuery("select * from `users` where `email`='$eMail'")
            .next()
    }

    fun updateStatus(id: Id, status: String) {
        getStatement().executeUpdate("update `users` set `status`=\"$status\" where `id`=$id")
    }

    fun updatePassword(id: Id, password: String) {
        getStatement().executeUpdate("update `users` set `password`=\"$password\" where `id`=$id")
    }


    fun deleteUser(user: User) {
        getStatement().executeUpdate("delete * from `users` where `id`=${user.id}")
    }

    fun registerUser(nickName: String): Id {
        val secretKey: String = getRandomString(40)
        val generatedColumns = arrayOf("ID")
        try {
            val st = db.getConnectionDB().prepareStatement(
                "INSERT INTO `users`( `nickname`, `secretKey`) VALUES ('$nickName','$secretKey')",
                generatedColumns
            )
            val res = st.executeUpdate()
            if (res != 0) {
                val rs = st.generatedKeys
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }
        } catch (_: SQLException) {
        }
        return -1
    }

    fun registerUserByEmail(
        nickName: String,
        eMail: String,
        password: String
    ): Id {
        val generatedColumns = arrayOf("ID")
        try {
            val secretKey: String = getRandomString(40)
            val st = db.getConnectionDB().prepareStatement(
                "INSERT INTO `users`( `nickname`, `email`, `password`, `secretKey`) VALUES ('$nickName','$eMail', '$password', '$secretKey')",
                generatedColumns
            )
            val res = st.executeUpdate()
            if (res != 0) {
                val rs = st.generatedKeys
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }
        } catch (_: SQLException) {
        }
        return -1
    }

    fun getLevels(idPlayer: Id): List<Level> {
        val rs =
            getStatement().executeQuery(
                "SELECT l.id, l.short_description, l.level, l.difficulty, l.picture, water.result_water, fire.result_fire " +
                        "FROM `levels` AS l left outer join best_results_water AS water " +
                        "on water.id_level=l.id and water.id_player=$idPlayer " +
                        "left outer join best_results_fire AS fire on fire.id_level=l.id and fire.id_player=$idPlayer"
            )

        val levelsList = mutableListOf<Level>()
        while (rs.next()) {
            levelsList.add(
                Level(
                    rs.getInt("id"),
                    rs.getInt("level"),
                    rs.getString("short_description"),
                    rs.getString("picture"),
                    rs.getInt("difficulty"),
                    rs.getInt("result_water"),
                    rs.getInt("result_fire")
                )
            )
        }
        return levelsList
    }

    fun getLevelById(idLevel: Id): String? {
        val rs =
            getStatement().executeQuery("SELECT `config_json` FROM `levels` WHERE `id`=$idLevel")

        if (rs.next()) {
            return rs.getString("config_json");
        }
        return null
    }

    fun getGameHistory(idPlayer: Id): List<GameHistory> {
        val rs = getStatement().executeQuery(
            "SELECT game_history.id_player_water, game_history.id_player_fire, users.nickname, levels.id, levels.level, game_history.stars, game_history.time_start, game_history.time " +
                    "FROM `game_history` " +
                    "INNER join `users` on (users.id=id_player_water or users.id=id_player_fire) and not(users.id=$idPlayer) " +
                    "INNER join `levels` on levels.id=game_history.id_level " +
                    "where game_history.id_player_water = $idPlayer or game_history.id_player_fire = $idPlayer"
        );
        val gameHistoryList = mutableListOf<GameHistory>()
        while (rs.next()) {
            gameHistoryList.add(
                GameHistory(
                    rs.getInt("id_player_water"),
                    rs.getInt("id_player_fire"),
                    rs.getString("nickname"),
                    rs.getInt("id"),
                    rs.getInt("level"),
                    rs.getInt("stars"),
                    rs.getInt("time_start"),
                    rs.getInt("time")
                )
            )
        }
        return gameHistoryList
    }

    fun topUpdate() {
        val rs =
            getStatement().executeQuery(
                "SELECT users.id as id, (rw.sumf+rf.sumw) as rating " +
                        "from `users` " +
                        "inner join (SELECT users.id, sum(levels.difficulty*best_results_fire.result_fire) as sumf " +
                        "FROM `users` left outer join best_results_fire on best_results_fire.id_player=users.id " +
                        "left outer join levels on levels.id=best_results_fire.id_level GROUP by users.id) as rw on rw.id=users.id " +
                        "inner join (SELECT users.id, sum(levels.difficulty*best_results_water.result_water) as sumw FROM `users` " +
                        "left outer join best_results_water on best_results_water.id_player=users.id " +
                        "left outer join levels on levels.id=best_results_water.id_level " +
                        "GROUP by users.id) as rf on rf.id=users.id " +
                        "ORDER BY rating DESC "
            )
        val usersTop = mutableListOf<Triple<Id, Int, Int>>()
        var i: Int = 0
        while (rs.next()) {
            usersTop.add(
                Triple(
                    rs.getInt("id"),
                    rs.getInt("rating"),
                    ++i
                )
            )
        }
        getStatement().executeUpdate("delete from top")
        val values: String = usersTop.joinToString(
            separator = ", ",
            transform = { "(${it.first}, ${it.second}, ${it.third})" }
        )
        getStatement().executeUpdate("INSERT INTO top(id_user, rating, place) values $values")
    }

    fun getTop(id: Id): List<UserPlace> {
        val rs =
            getStatement().executeQuery(
                "(select users.id, users.nickname, top.place, top.rating " +
                        "from `top` " +
                        "inner join `users` on top.id_user = users.id " +
                        "ORDER BY top.place LIMIT 10) " +
                        "union " +
                        "(select users.id, users.nickname, top.place, top.rating " +
                        "from `top` " +
                        "inner join `users` on top.id_user = `users`.id " +
                        "WHERE `users`.id=${id})"
            )
        val res = mutableListOf<UserPlace>()
        while (rs.next()) {
            res.add(
                UserPlace(
                    rs.getInt("id"),
                    rs.getString("nickname"),
                    rs.getInt("place"),
                    rs.getInt("rating")
                )
            )
        }
        return res
    }

    fun saveGameResults(
        idGame: Id,
        idPlayerFire: Id,
        idPlayerWater: Id,
        result: Long,
        time: Long,
        idLevel: Int
    ) {
        getStatement().executeUpdate(
            "INSERT INTO `best_results_fire`(`id_player`, `id_level`, `result_fire`) " +
                    "VALUES ($idPlayerFire, $idLevel, $result) " +
                    "ON DUPLICATE KEY " +
                    "update `result_fire`=IF(`result_fire`<$result, $result, `result_fire`)"
        )
        getStatement().executeUpdate(
            "INSERT INTO `best_results_water`(`id_player`, `id_level`, `result_water`) " +
                "VALUES ($idPlayerWater, $idLevel, $result) " +
                    "ON DUPLICATE KEY " +
                    "update `result_water`=IF(`result_water`<$result, $result, `result_water`)"
        )
        getStatement().executeUpdate(
            "INSERT INTO `game_history`(`id_player_water`, `id_player_fire`, `id_level`, `stars`, `time_start`, `time`)" +
                    " VALUES ($idPlayerWater, $idPlayerFire, $idLevel, $result, $time, $time)"
        )
    }
}