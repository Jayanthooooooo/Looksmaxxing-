package com.example.ui.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.SkillNodeDao
import com.example.data.local.entity.SkillNodeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JourneyViewModel(private val skillNodeDao: SkillNodeDao) : ViewModel() {
    val skillNodes: StateFlow<List<SkillNodeEntity>> = skillNodeDao.getAllNodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unlockSkill(id: String) {
        viewModelScope.launch {
            skillNodeDao.updateNodeStatus(id, "DONE")
        }
    }
}
