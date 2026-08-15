package com.bullseyestracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bullseyestracker.match.data.MatchRepository
import com.bullseyestracker.match.model.Match
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns match-history state for the UI (spec 005-match-history User Story 2): the completed-match
 * list (FR-004) and the currently selected match's detail (FR-005), both sourced from
 * [MatchRepository] — same ViewModel shape as [com.bullseyestracker.ui.match.MatchViewModel].
 */
class HistoryViewModel(
    private val matchRepository: MatchRepository,
) : ViewModel() {
    private val _completedMatches = MutableStateFlow<List<Match>>(emptyList())
    val completedMatches: StateFlow<List<Match>> = _completedMatches.asStateFlow()

    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()

    init {
        viewModelScope.launch {
            matchRepository.observeCompletedMatches().collect { matches ->
                _completedMatches.value = matches
            }
        }
    }

    fun selectMatch(matchId: String) {
        viewModelScope.launch {
            _selectedMatch.value = matchRepository.getMatch(matchId)
        }
    }

    fun clearSelection() {
        _selectedMatch.value = null
    }
}

class HistoryViewModelFactory(
    private val matchRepository: MatchRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(matchRepository) as T
}
