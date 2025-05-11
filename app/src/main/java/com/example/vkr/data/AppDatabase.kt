package com.example.vkr.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vkr.data.dao.AchievementDao
import com.example.vkr.data.dao.EventDao
import com.example.vkr.data.dao.UserDao
import com.example.vkr.data.model.AchievementEntity
import com.example.vkr.data.model.EventEntity
import com.example.vkr.data.model.UserAchievementCrossRef
import com.example.vkr.data.model.UserEntity
import com.example.vkr.data.model.UserEventCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.vkr.R
import com.example.vkr.data.dao.TeamDao
import com.example.vkr.data.model.TeamEntity
import com.google.gson.Gson
import com.yandex.mapkit.geometry.Point

@Database(
    entities = [
        UserEntity::class,
        EventEntity::class,
        UserEventCrossRef::class,
        AchievementEntity::class,
        UserAchievementCrossRef::class,
        TeamEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun teamDao(): TeamDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vkr_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            val appScope = CoroutineScope(Dispatchers.IO)
                            appScope.launch {
                                val database = getInstance(context)
                                database.populateInitialAchievements()
                                database.populateInitialTeams()
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    // 👇 Вставка достижений
    suspend fun populateInitialAchievements() {
        val achievements = listOf(
            AchievementEntity(title = "Эко Герой", description = "1000 баллов", imageResId = R.drawable.images),
            AchievementEntity(title = "Лучший Волонтер", description = "50 мероприятий", imageResId = R.drawable.l612f4bd3d34ba),
            AchievementEntity(title = "Защитник природы", description = "300 баллов", imageResId = R.drawable.images),
            AchievementEntity(title = "Охотник за мусором", description = "10 мероприятий", imageResId = R.drawable.l612f4bd3d34ba)
        )

        achievements.forEach {
            this.achievementDao().insertAchievement(it)
        }
    }

    // 👇 Вставка команд
    suspend fun populateInitialTeams() {
        val teams = listOf(
            TeamEntity(
                name = "Красные",
                color = 0x66FF0000,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.52830844921757, 37.511560521192806),
                        Point(55.52855491609576, 37.511380813188794),
                        Point(55.52867662756232, 37.511418364115),
                        Point(55.52884702298009, 37.51159270770099),
                        Point(55.52899611836258, 37.51227130658174),
                        Point(55.5289383059347, 37.51252879864718),
                        Point(55.52867358478027, 37.51271655327821),
                        Point(55.52862490023523, 37.51288016802811),
                        Point(55.52837995519995, 37.51304646498706),
                        Point(55.528287149537995, 37.51303305394197),
                        Point(55.52813653004482, 37.512882850237155),
                        Point(55.528060459373584, 37.51258244282747),
                        Point(55.528054373713495, 37.51234372622515),
                        Point(55.52815935121738, 37.512158653803134)
                    )
                )
            ),
            TeamEntity(
                name = "Синие",
                color = 0x660000FF,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.52797892995888, 37.51354621573619),
                        Point(55.52819344931915, 37.513653504096794),
                        Point(55.52830755488318, 37.51377688571148),
                        Point(55.52845513091966, 37.51434283181362),
                        Point(55.52835928262935, 37.51483367606334),
                        Point(55.527899406555925, 37.51473489121221),
                        Point(55.527663585566536, 37.514541772163135),
                        Point(55.527566213905175, 37.51412871197484),
                        Point(55.52766510699683, 37.513715651786534),
                        Point(55.527730528443335, 37.51362713888904)
                    )
                )
            ),
            TeamEntity(
                name = "Зелёные",
                color = 0x6600FF00,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.52757941146031, 37.51496759253141),
                        Point(55.52782284007785, 37.515074880892016),
                        Point(55.52790195405282, 37.51524922447798),
                        Point(55.528025188964726, 37.51585272150633),
                        Point(55.52800845338216, 37.51602438288327),
                        Point(55.52794607523869, 37.51626846390363),
                        Point(55.52748812533868, 37.51614240007993),
                        Point(55.52731163823361, 37.516029747301324),
                        Point(55.527211222801654, 37.51582589941617),
                        Point(55.527157886403515, 37.515523200340255),
                        Point(55.527261344864215, 37.51512623340604),
                        Point(55.52733589564443, 37.51503235609053)
                    )
                )
            ),
            TeamEntity(
                name = "Жёлтые",
                color = 0x66FFFF00,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.52920975752816, 37.51210637997134),
                        Point(55.529410577887575, 37.5119347185944),
                        Point(55.52966008053614, 37.51197763393863),
                        Point(55.52980308743519, 37.51241751621708),
                        Point(55.529906538909, 37.512835940823386),
                        Point(55.52987611203331, 37.51299687336428),
                        Point(55.52983655705956, 37.51307733963473),
                        Point(55.529562713838004, 37.51326509426578)
                    )
                )
            ),
            TeamEntity(
                name = "Фиолетовые",
                color = 0x66FF00FF,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.530058672932924, 37.51265891502842),
                        Point(55.530320342071725, 37.51247116039737),
                        Point(55.53056983892473, 37.51261599968418),
                        Point(55.53076760969111, 37.51338311146244),
                        Point(55.53071284250195, 37.51365133236391),
                        Point(55.530434935277725,37.513848871223956)
                    )
                )
            ),
            TeamEntity(
                name = "Оранжевые",
                color = 0x66FFA500,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.53010127035373, 37.51655884693605),
                        Point(55.52936493698722, 37.51717039059147),
                        Point(55.52930408237068, 37.517454704747045),
                        Point(55.52958705553524, 37.51852222393496),
                        Point(55.52976048969007, 37.51862414787753),
                        Point(55.53011039836644, 37.5185812325333),
                        Point(55.53020167837659, 37.518935284123245),
                        Point(55.530329470033394, 37.51901575039369),
                        Point(55.53107187045852, 37.51853295277101),
                        Point(55.53112359455389, 37.51825400303346),
                        Point(55.53085888816952, 37.517202577099624),
                        Point(55.53059417999602, 37.517063102230864),
                        Point(55.5304085789453, 37.51721867035373),
                        Point(55.53019863571301, 37.51698800037844)
                    )
                )
            ),
            TeamEntity(
                name = "Бирюзовые",
                color = 0x6600FFFF,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.532182944094586, 37.5141611791754),
                        Point(55.531650501306245, 37.51645178567412),
                        Point(55.53193650004672, 37.517567584624324),
                        Point(55.532076456116044, 37.5176373220587),
                        Point(55.532878562189566, 37.516912345692695),
                        Point(55.53279254029454, 37.5165441645828),
                        Point(55.532868601775085, 37.51632422344358),
                        Point(55.53333713723743, 37.516002358361796),
                        Point(55.53341928248767, 37.5157341374603),
                        Point(55.533245864535054, 37.515036763116434),
                        Point(55.53294770555814, 37.51510650055083),
                        Point(55.53277428551721, 37.51415163414152),
                        Point(55.53243657060199, 37.5139853371826)
                    )
                )
            ),
            TeamEntity(
                name = "Фиоловые",
                color = 0x669933CC,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.5309340581643, 37.51329050547962),
                        Point(55.53013080179474, 37.515430908273494),
                        Point(55.53033161743163, 37.51622484214191),
                        Point(55.53042898221827, 37.51628385074025),
                        Point(55.531576042886975, 37.51547382361776),
                        Point(55.53192289281327, 37.51402006633166),
                        Point(55.53176468094723, 37.51334951407796),
                        Point(55.531478680951984, 37.513220768045244),
                        Point(55.53119876404059, 37.51341925151235),
                        Point(55.531007080653715, 37.51317785270101)
                    )
                )
            ),
            TeamEntity(
                name = "Салатовые",
                color = 0x6699FF66,
                areaPoints = serializePoints(
                    listOf(
                        Point(55.52862600762548, 37.51574223899504),
                        Point(55.52895462712072, 37.51597827338835),
                        Point(55.529111660104505, 37.516573723789634),
                        Point(55.529002121018294, 37.517067250248395),
                        Point(55.52884998289031, 37.516981419559926),
                        Point(55.52852136251708, 37.5170887079205),
                        Point(55.528277938240784, 37.51694386863371),
                        Point(55.52816535400134, 37.5165308084454),
                        Point(55.528259681359074, 37.516101655003034),
                        Point(55.528430078590766, 37.51598363780638)
                    )
                )
            )
        )

        teams.forEach {
            this.teamDao().insertTeam(it)
        }
    }


    // 👇 Сериализация списка координат в JSON
    private fun serializePoints(points: List<Point>): String {
        val list = points.map { mapOf("lat" to it.latitude, "lon" to it.longitude) }
        return Gson().toJson(list)
    }
}