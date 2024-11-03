package eu.dobschal.model.dto

data class UserRankingDto(
    val id: Int,
    val username: String,
    val points: Int,
    val avatarImageId: Int,
    val x: Int,
    val y: Int
)
