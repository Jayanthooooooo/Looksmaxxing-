package com.example.ui.screens.results

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LumoraApp
import com.example.data.local.entity.PostureLogEntity
import com.example.data.repo.PostureExerciseData
import com.example.face.posture.MlKitPoseLandmarkExtractor
import com.example.face.posture.PoseLandmarkExtractor
import com.example.face.posture.PostureAnalysisResult
import com.example.face.posture.PostureBand
import com.example.face.posture.PostureConfidence
import com.example.face.posture.PostureEngine
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

class PostureAnalysisViewModel @JvmOverloads constructor(
    application: Application,
    private val extractor: PoseLandmarkExtractor = MlKitPoseLandmarkExtractor()
) : AndroidViewModel(application) {
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
                val current = _uiState.value
                if (current is PostureUiState.Success) {
                    _uiState.value = current.copy(history = history)
                } else {
                    val initialAnalysis = if (history.isNotEmpty()) {
                        val latest = history.first()
                        val bandEnum = PostureBand.values().firstOrNull { it.displayName.equals(latest.bandName, ignoreCase = true) }
                        PostureAnalysisResult(
                            band = bandEnum,
                            craniovertebralAngle = latest.craniovertebralAngle,
                            forwardOffsetRatio = latest.forwardOffsetRatio,
                            confidence = PostureConfidence.HIGH,
                            reasoning = "Latest saved posture check (${latest.bandName})."
                        )
                    } else {
                        PostureAnalysisResult(
                            band = null,
                            craniovertebralAngle = 0f,
                            forwardOffsetRatio = 0f,
                            confidence = PostureConfidence.LOW,
                            reasoning = "Take a side photo to check your posture."
                        )
                    }

                    _uiState.value = PostureUiState.Success(
                        analysis = initialAnalysis,
                        exerciseData = exercises,
                        history = history,
                        currentBitmap = null
                    )
                }
            }
        }
    }

    fun processSideBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            val poseInput = extractor.extractPoseLandmarks(bitmap)

            val analysis = if (poseInput != null) {
                val res = PostureEngine.analyze(poseInput)
                if (res.errorMessage == null && res.band != null) {
                    repo.savePostureCheck(
                        craniovertebralAngle = res.craniovertebralAngle,
                        forwardOffsetRatio = res.forwardOffsetRatio,
                        bandName = res.band.displayName
                    )
                }
                res
            } else {
                PostureAnalysisResult(
                    band = null,
                    craniovertebralAngle = 0f,
                    forwardOffsetRatio = 0f,
                    confidence = PostureConfidence.LOW,
                    reasoning = "Landmark detection could not identify ear, shoulder, or hip points clearly.",
                    errorMessage = "Not enough data",
                    retakeTip = "Stand side-on, camera at chest height, 2 metres away, arms relaxed, shoulders and ears visible."
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
