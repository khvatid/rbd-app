package data.entity

import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonSerialize
data class DatabaseEntity(
    val name : String,
    val sizeOnDisk: Int,
    val empty : Boolean
)
