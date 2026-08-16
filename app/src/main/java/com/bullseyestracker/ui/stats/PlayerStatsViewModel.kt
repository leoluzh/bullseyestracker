package com.bullseyestracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bullseyestracker.match.data.MatchRepository
import com.bullseyestracker.match.stats.PlayerStats
import com.bullseyestracker.match.stats.PlayerStatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns player-statistics state for the UI (spec 010-player-stats): win rate per player name,
 * recomputed from [MatchRepository.observeCompletedMatches()] via [PlayerStatsCalculator] on
 * every new emission — same ViewModel shape as
 * [com.bullseyestracker.ui.history.HistoryViewModel].
 */
class PlayerStatsViewModel(
    private val matchRepository: MatchRepository,
) : ViewModel() {
    private val _playerStats = MutableStateFlow<List<PlayerStats>>(emptyList())
    val playerStats: StateFlow<List<PlayerStats>> = _playerStats.asStateFlow()

    init {
        viewModelScope.launch {
            matchRepository.observeCompletedMatches().collect { matches ->
                _playerStats.value = PlayerStatsCalculator.compute(matches)
            }
        }
    }
}

class PlayerStatsViewModelFactory(
    private val matchRepository: MatchRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PlayerStatsViewModel(matchRepository) as T
}
