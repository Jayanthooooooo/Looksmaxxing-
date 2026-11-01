package com.example.ui.screens.results

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LumoraApp
import com.example.data.local.entity.PostureLogEntity
import com.example.data.repo.PostureExerciseData
import com.example.face.posture.PostureAnalysisResult
import com.example.face.posture.PostureBand
import com.example.face.posture.PostureConfidence
import com.example.face.posture.PostureEngine
import com.example.face.posture.PostureLandmarkPoint
import com.example.face.posture.PosturePoseInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PostureUiState {
    data object Loading : PostureUiState
    data class Success(
        val analysis: PostureAnalysisResult,
        val exerciseData: PostureExerciseData,
        val history: List<PostureLogEntity>,
        val currentBitmap: Bitmap? = null
    ) : PostureUiState
    data class Error(val message: String) : PostureUiState
}

class PostureAnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LumoraApp
    private val repo = app.container.postureRepository
    private val habitRepo = app.container.habitRepository

    private val _uiState = MutableStateFlow<PostureUiState>(PostureUiState.Loading)
    val uiState: StateFlow<PostureUiState> = _uiState.asStateFlow()

    private val _planAdded = MutableStateFlow(false)
    val planAdded: StateFlow<Boolean> = _planAdded.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val exercises = repo.loadExerciseData()
            repo.observePostureHistory.collect { history ->
                val sampleInput = PosturePoseInput(
                    ear = PostureLandmarkPoint(180f, 130f, 0.9f),
                    shoulder = PostureLandmarkPoint(100f, 220f, 0.9f),
                    hip = PostureLandmarkPoint(100f, 480f, 0.9f)
                )
                val analysis = PostureEngine.analyze(sampleInput)

                val current = _uiState.value
                val bitmap = if (current is PostureUiState.Success) current.currentBitmap else null

                _uiState.value = PostureUiState.Success(
                    analysis = analysis,
                    exerciseData = exercises,
                    history = history,
                    currentBitmap = bitmap
                )
            }
        }
    }

    fun processSideBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            val input = PosturePoseInput(
                ear = PostureLandmarkPoint(bitmap.width * 0.55f, bitmap.height * 0.25f, 0.92f),
                shoulder = PostureLandmarkPoint(bitmap.width * 0.45f, bitmap.height * 0.42f, 0.92f),
                hip = PostureLandmarkPoint(bitmap.width * 0.45f, bitmap.height * 0.75f, 0.92f)
            )
            val analysis = PostureEngine.analyze(input)

            if (analysis.errorMessage == null && analysis.band != null) {
                repo.savePostureCheck(
                    craniovertebralAngle = analysis.craniovertebralAngle,
                    forwardOffsetRatio = analysis.forwardOffsetRatio,
                    bandName = analysis.band.displayName
                )
            }

            val exercises = repo.loadExerciseData()
            val latestHistory = (uiState.value as? PostureUiState.Success)?.history ?: emptyList()

            _uiState.value = PostureUiState.Success(
                analysis = analysis,
                exerciseData = exercises,
                history = latestHistory,
                currentBitmap = bitmap
            )
        }
    }

    fun add4WeekPlanToHabits() {
        viewModelScope.launch {
            val exData = repo.loadExerciseData()
            val chinTuckEx = exData.exercises.firstOrNull { it.id == "chin_tucks" }
            val screenResetEx = exData.exercises.firstOrNull { it.id == "screen_break_resets" }

            val habitList = listOf(
                com.example.data.local.entity.HabitEntity(
                    id = "habit_chin_tucks",
                    label = "Chin Tucks (10 reps)",
                    pillar = "Posture/Sleep",
                    scheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
                    active = true,
                    orderIdx = 10
                ),
                com.example.data.local.entity.HabitEntity(
                    id = "habit_screen_resets",
                    label = "Hourly Screen Break Reset",
                    pillar = "Posture/Sleep",
                    scheduleJson = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]",
                    active = true,
                    orderIdx = 11
                )
            )

            habitList.forEach { habitRepo.saveHabit(it) }
            _planAdded.value = true
        }
    }
}
