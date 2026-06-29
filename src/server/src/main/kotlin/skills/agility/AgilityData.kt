package skills.agility

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class ObstacleDef(
    val name: String,
    val xp: Double,
    val objectId: Int,
    val ticks: Int,
    val wiki: String,
)

data class CourseDef(
    val name: String,
    val levelRequired: Int,
    val completionBonusXp: Double,
    val markOfGraceChance: Int?,
    val members: Boolean,
    val wiki: String,
    val obstacles: List<ObstacleDef>,
)

data class PetDef(
    val name: String,
    val itemId: Int,
    val baseRate: Int,
    val scalesWithLevel: Boolean,
    val wiki: String,
)

data class AgilityMeta(
    val skill: String,
    val defaultTicks: Int,
    val markOfGraceItemId: Int,
)

data class AgilityConfig(
    val meta: AgilityMeta,
    val courses: Map<String, CourseDef>,
    val allCourseObjectIds: IntArray,
    val byCourseObjectId: Map<Int, Pair<CourseDef, ObstacleDef>>,
    val pet: PetDef,
)

object AgilityLoader {
    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): AgilityConfig {
        val yaml = Yaml()
        val raw = yaml.load<Map<String, Any>>(Files.newInputStream(path))

        val metaMap = raw["meta"] as Map<String, Any>
        val meta = AgilityMeta(
            skill = metaMap["skill"] as String,
            defaultTicks = metaMap["default_ticks"] as Int,
            markOfGraceItemId = metaMap["mark_of_grace_item_id"] as Int,
        )

        val coursesRaw = raw["courses"] as Map<String, Map<String, Any>>
        val courses = linkedMapOf<String, CourseDef>()

        for ((courseName, courseData) in coursesRaw) {
            val obstacleList = courseData["obstacles"] as List<Map<String, Any>>
            val obstacles = obstacleList.map { obs ->
                ObstacleDef(
                    name = obs["name"] as String,
                    xp = (obs["xp"] as Number).toDouble(),
                    objectId = obs["object_id"] as Int,
                    ticks = obs["ticks"] as Int,
                    wiki = obs["wiki"] as String,
                )
            }
            courses[courseName] = CourseDef(
                name = courseName,
                levelRequired = courseData["level_required"] as Int,
                completionBonusXp = (courseData["completion_bonus_xp"] as Number).toDouble(),
                markOfGraceChance = courseData["mark_of_grace_chance"] as? Int,
                members = courseData["members"] as Boolean,
                wiki = courseData["wiki"] as String,
                obstacles = obstacles,
            )
        }

        val petMap = raw["pet"] as Map<String, Any>
        val pet = PetDef(
            name = petMap["name"] as String,
            itemId = petMap["item_id"] as Int,
            baseRate = petMap["base_rate"] as Int,
            scalesWithLevel = petMap["scales_with_level"] as Boolean,
            wiki = petMap["wiki"] as String,
        )

        val allObjectIds = mutableListOf<Int>()
        val byObjectId = mutableMapOf<Int, Pair<CourseDef, ObstacleDef>>()
        for (course in courses.values) {
            for (obstacle in course.obstacles) {
                if (obstacle.objectId != 0) {
                    allObjectIds.add(obstacle.objectId)
                    byObjectId[obstacle.objectId] = Pair(course, obstacle)
                }
            }
        }

        return AgilityConfig(
            meta = meta,
            courses = courses,
            allCourseObjectIds = allObjectIds.toIntArray(),
            byCourseObjectId = byObjectId,
            pet = pet,
        )
    }
}

object AgilityDefs {
    lateinit var config: AgilityConfig private set

    fun init(yamlPath: Path): AgilityConfig {
        config = AgilityLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
