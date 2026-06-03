package com.leonardo.mundial2026.data.network

import com.leonardo.mundial2026.data.model.PlayerResponse
import com.leonardo.mundial2026.data.model.TeamResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url

interface FootballApiService {
    // Simplificamos: Si pasamos una URL completa con ?league=1..., no usamos @Query
    @GET
    suspend fun getTeams(
        @Url url: String
    ): Response<TeamResponse>

    @GET
    suspend fun getPlayers(
        @Url url: String,
        @Query("team") teamId: Int,
        @Header("x-apisports-key") apiKey: String
    ): Response<PlayerResponse>
}
