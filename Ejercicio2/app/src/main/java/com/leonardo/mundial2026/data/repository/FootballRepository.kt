package com.leonardo.mundial2026.data.repository

import com.leonardo.mundial2026.data.model.PlayerContainer
import com.leonardo.mundial2026.data.model.TeamContainer
import com.leonardo.mundial2026.data.network.RetrofitClient
import com.leonardo.mundial2026.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Esta clase se encarga de decidir de dónde sacamos los datos
class FootballRepository {
    private val apiService by lazy { RetrofitClient.instance }

    // Función para obtener la lista de países que participan
    suspend fun getTeams(): List<TeamContainer>? = withContext(Dispatchers.IO) {
        try {
            // Usamos el endpoint alternativo para equipos
            val fullUrl = "${Constants.ALTERNATIVE_TEAMS_URL}?league=1&season=2026"
            val response = apiService.getTeams(fullUrl)
            
            // Si la respuesta es buena, regresamos la lista de equipos
            if (response.isSuccessful) response.body()?.response else null
        } catch (e: Exception) {
            null // Si hay un error de red regresamos null para que el ViewModel lo maneje
        }
    }

    // Función para obtener los jugadores de un equipo específico usando su ID
    suspend fun getPlayers(teamId: Int): List<PlayerContainer>? = withContext(Dispatchers.IO) {
        try {
            // Usamos la API oficial
            val officialUrl = "${Constants.BASE_URL}players/squads"
            val response = apiService.getPlayers(officialUrl, teamId, Constants.API_KEY)

            if (response.isSuccessful) response.body()?.response else null
        } catch (e: Exception) {
            null
        }
    }
}
