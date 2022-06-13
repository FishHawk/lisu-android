package com.fishhawk.lisu.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val username: String,
    val content: String,
    val createTime: Long? = null,
    val vote: Int? = null,
    val subComments: List<CommentDto>? = null,
)