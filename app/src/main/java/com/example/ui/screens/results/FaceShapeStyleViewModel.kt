package com.example.ui.screens.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LumoraApp
import com.example.face.shape.FaceShapeAnalysisResult
import com.example.face.shape.FaceShapeEngine
import com.example.face.shape.FacialMeasurements
import com.example.face.shape.ShapeStyleGuide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FaceShapeStyleState {
    data object Loading : FaceShapeStyleState
    data class Success(
        val analysis: FaceShapeAnalysisResult,
        val activeShapeGuide: ShapeStyleGuide?,
        val allShapes: List<ShapeStyleGuide>
    ) : FaceShapeStyleState
    data class Error(val message: String) : FaceShapeStyleState
}

class FaceShapeStyleViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LumoraApp
    private val repo = app.container.styleGuideRepository

    private val _state = MutableStateFlow<FaceShapeStyleState>(FaceShapeStyleState.Loading)
    val state: StateFlow<FaceShapeStyleState> = _state.asStateFlow()

    private val _isTeenMode = MutableStateFlow(false)
    val isTeenMode: StateFlow<Boolean> = _isTeenMode.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    init {
        loadData()
    }

    fun setTeenMode(isTeen: Boolean) {
        _isTeenMode.value = isTeen
    }

    fun loadData() {
        viewModelScope.launch {
            val shapes = repo.getShapes()
            val sampleMeasurements = FacialMeasurements(
                faceWidthCheekbones = 100f,
                jawWidth = 82f,
                faceHeight = 155f,
                foreheadWidth = 86f,
                chinAngle = 85f,
                photoConfidence = 0.92f
            )
            val analysis = FaceShapeEngine.classify(sampleMeasurements)
            val topShapeType = analysis.topMatches.firstOrNull()?.shape?.displayName ?: "Oval"
            val matchingGuide = shapes.firstOrNull { it.name.equals(topShapeType, ignoreCase = true) } ?: shapes.firstOrNull()

            _state.value = FaceShapeStyleState.Success(
                analysis = analysis,
                activeShapeGuide = matchingGuide,
                allShapes = shapes
            )
        }
    }

    fun selectShape(shapeId: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is FaceShapeStyleState.Success) {
                val matchingGuide = currentState.allShapes.firstOrNull { it.id.equals(shapeId, ignoreCase = true) || it.name.equals(shapeId, ignoreCase = true) }
                if (matchingGuide != null) {
                    _state.value = currentState.copy(activeShapeGuide = matchingGuide)
                }
            }
        }
    }

    fun toggleFavorite(itemId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _favorites.value = current
    }
}
