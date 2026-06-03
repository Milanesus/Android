package com.leonardo.mundial2026.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leonardo.mundial2026.data.model.Player
import com.leonardo.mundial2026.data.model.TeamContainer
import com.leonardo.mundial2026.data.repository.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TeamsUiState {
    data object Loading : TeamsUiState()
    data class Success(val teams: List<TeamContainer>) : TeamsUiState()
    data object Empty : TeamsUiState()
    data class Error(val isNoInternet: Boolean = false) : TeamsUiState()
}

sealed class PlayersUiState {
    data object Loading : PlayersUiState()
    data class Success(val players: List<Player>) : PlayersUiState()
    data class Error(val isNoInternet: Boolean = false) : PlayersUiState()
    data object Empty : PlayersUiState()
}

class FootballViewModel : ViewModel() {
    private val TAG = "Mundial2026_VM"
    private val repository = FootballRepository()

    // Manejamos los estados de la lista de equipos (Cargando, Éxito, Error)
    private val _teamsState = MutableStateFlow<TeamsUiState>(TeamsUiState.Loading)
    val teamsState: StateFlow<TeamsUiState> = _teamsState

    // Manejamos los estados de los jugadores
    private val _playersState = MutableStateFlow<PlayersUiState>(PlayersUiState.Loading)
    val playersState: StateFlow<PlayersUiState> = _playersState

    // Estado para guardar lo que el usuario escribe en el buscador
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Función para traer los países desde la API
    fun fetchTeams() {
        viewModelScope.launch {
            _teamsState.value = TeamsUiState.Loading
            try {
                val result = repository.getTeams()
                if (result != null) {
                    if (result.isEmpty()) {
                        _teamsState.value = TeamsUiState.Empty
                    } else {
                        _teamsState.value = TeamsUiState.Success(result)
                    }
                } else {
                    _teamsState.value = TeamsUiState.Error()
                }
            } catch (e: Exception) {
                // Si falla la conexión, avisamos a la interfaz para mostrar el mensaje de "sin internet"
                val isNoInternet = e is java.net.UnknownHostException || e is java.net.ConnectException
                _teamsState.value = TeamsUiState.Error(isNoInternet)
            }
        }
    }

    // Función para traer los jugadores de un país específico por su ID
    fun fetchPlayers(teamId: Int) {
        viewModelScope.launch {
            _playersState.value = PlayersUiState.Loading
            try {
                val result = repository.getPlayers(teamId)
                val playersList = result?.firstOrNull()?.players
                
                if (playersList != null && playersList.isNotEmpty()) {
                    _playersState.value = PlayersUiState.Success(playersList)
                } else if (playersList != null) {
                    _playersState.value = PlayersUiState.Empty
                } else {
                    _playersState.value = PlayersUiState.Error()
                }
            } catch (e: Exception) {
                val isNoInternet = e is java.net.UnknownHostException || e is java.net.ConnectException
                _playersState.value = PlayersUiState.Error(isNoInternet)
            }
        }
    }
}
