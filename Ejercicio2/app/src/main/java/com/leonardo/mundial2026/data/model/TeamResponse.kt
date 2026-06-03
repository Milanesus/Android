package com.leonardo.mundial2026.data.model

import com.google.gson.annotations.SerializedName

data class TeamResponse(
    @SerializedName("get") val endpoint: String?,
    @SerializedName("parameters") val parameters: Parameters?,
    @SerializedName("errors") val errors: List<Any>?,
    @SerializedName("results") val results: Int?,
    @SerializedName("paging") val paging: Paging?,
    @SerializedName("response") val response: List<TeamContainer> = emptyList()
)

data class Parameters(
    @SerializedName("league") val league: String?,
    @SerializedName("season") val season: String?
)

data class Paging(
    @SerializedName("current") val current: Int?,
    @SerializedName("total") val total: Int?
)

data class TeamContainer(
    @SerializedName("team") val team: Team,
    @SerializedName("venue") val venue: Venue?
)

data class Team(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("founded") val founded: Int?,
    @SerializedName("national") val national: Boolean?,
    @SerializedName("logo") val logo: String?
)

data class Venue(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("capacity") val capacity: Int?,
    @SerializedName("surface") val surface: String?,
    @SerializedName("image") val image: String?
)
