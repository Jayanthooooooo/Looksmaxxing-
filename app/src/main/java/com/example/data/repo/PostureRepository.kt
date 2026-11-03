package com.example.data.repo

import android.content.Context
import com.example.core.AppDispatchers
import com.example.data.local.dao.PostureDao
import com.example.data.local.entity.PostureLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class PostureExercise(
    val id: String,
    val name: String,
    val category: String,
    val sets: String,
    val reps: String,
    val duration: String,
    val steps: List<String>,
    val mistakesToAvoid: List<String>,
    val safetyWarning: String
)

data class PosturePlanWeek(
    val week: Int,
    val title: String,
    val focus: String,
    val dailyExercises: List<String>
)

data class PostureExerciseData(
    val disclaimer: String,
    val exercises: List<PostureExercise>,
    val fourWeekPlan: List<PosturePlanWeek>
)

class PostureRepository(
    private val context: Context,
    private val postureDao: PostureDao,
    var dispatchers: AppDispatchers
) {
    private var cachedData: PostureExerciseData? = null

    val observePostureHistory: Flow<List<PostureLogEntity>> = postureDao.observeAllPostureLogs()

    suspend fun loadExerciseData(): PostureExerciseData = withContext(dispatchers.io) {
        cachedData?.let { return@withContext it }
        try {
            val jsonString = context.assets.open("posture_exercises.json").bufferedReader().use { it.readText() }
            val parsed = parsePostureJson(jsonString)
            cachedData = parsed
            parsed
        } catch (e: Exception) {
            e.printStackTrace()
            PostureExerciseData(
                disclaimer = "Posture routines may help improve ergonomic awareness.",
                exercises = emptyList(),
                fourWeekPlan = emptyList()
            )
        }
    }

    suspend fun savePostureCheck(
        craniovertebralAngle: Float,
        forwardOffsetRatio: Float,
        bandName: String
    ): PostureLogEntity = withContext(dispatchers.io) {
        val log = PostureLogEntity(
            id = "posture_${System.currentTimeMillis()}",
            createdAt = System.currentTimeMillis(),
            craniovertebralAngle = craniovertebralAngle,
            forwardOffsetRatio = forwardOffsetRatio,
            bandName = bandName
        )
        postureDao.insertPostureLog(log)
        log
    }

    suspend fun getLatestPostureCheck(): PostureLogEntity? = withContext(dispatchers.io) {
        postureDao.getLatestPostureLog()
    }

    companion object {
        fun parsePostureJson(jsonString: String): PostureExerciseData {
            val root = JSONObject(jsonString)
            val disclaimer = root.optString("disclaimer", "")

            val exArray = root.getJSONArray("exercises")
            val exercises = mutableListOf<PostureExercise>()
            for (i in 0 until exArray.length()) {
                val obj = exArray.getJSONObject(i)
                val stepsArr = obj.getJSONArray("steps")
                val steps = mutableListOf<String>()
                for (j in 0 until stepsArr.length()) {
                    steps.add(stepsArr.getString(j))
                }
                val mistakesArr = obj.getJSONArray("mistakesToAvoid")
                val mistakes = mutableListOf<String>()
                for (j in 0 until mistakesArr.length()) {
                    mistakes.add(mistakesArr.getString(j))
                }

                exercises.add(
                    PostureExercise(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        category = obj.getString("category"),
                        sets = obj.getString("sets"),
                        reps = obj.getString("reps"),
                        duration = obj.getString("duration"),
                        steps = steps,
                        mistakesToAvoid = mistakes,
                        safetyWarning = obj.optString("safetyWarning", "Stop if you feel pain, numbness or dizziness.")
                    )
                )
            }

            val planArray = root.getJSONArray("four_week_plan")
            val planWeeks = mutableListOf<PosturePlanWeek>()
            for (i in 0 until planArray.length()) {
                val obj = planArray.getJSONObject(i)
                val exArr = obj.getJSONArray("dailyExercises")
                val dailyEx = mutableListOf<String>()
                for (j in 0 until exArr.length()) {
                    dailyEx.add(exArr.getString(j))
                }
                planWeeks.add(
                    PosturePlanWeek(
                        week = obj.getInt("week"),
                        title = obj.getString("title"),
                        focus = obj.getString("focus"),
                        dailyExercises = dailyEx
                    )
                )
            }

            return PostureExerciseData(
                disclaimer = disclaimer,
                exercises = exercises,
                fourWeekPlan = planWeeks
            )
        }
    }
}
