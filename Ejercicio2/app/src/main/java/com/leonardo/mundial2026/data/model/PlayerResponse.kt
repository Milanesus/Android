package com.leonardo.mundial2026.data.model

import com.google.gson.annotations.SerializedName

data class PlayerResponse(
    @SerializedName("get") val endpoint: String?,
    @SerializedName("parameters") val parameters: PlayerParameters?,
    @SerializedName("errors") val errors: Any?, // Puede ser lista o mapa
    @SerializedName("results") val results: Int?,
    @SerializedName("paging") val paging: Paging?,
    @SerializedName("response") val response: List<PlayerContainer>?
)

data class PlayerParameters(
    @SerializedName("team") val team: String?
)

data class PlayerContainer(
    @SerializedName("team") val team: PlayerTeam?,
    @SerializedName("players") val players: List<Player>?
)

data class PlayerTeam(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("logo") val logo: String?
)

data class Player(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int?,
    @SerializedName("number") val number: Int?,
    @SerializedName("position") val position: String?,
    @SerializedName("photo") val photo: String?
)
