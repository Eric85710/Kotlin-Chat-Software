package com.example.login_v3.data.di

import androidx.room.TypeConverter
import com.example.login_v3.data.api.api_class.Reaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class RoomConverters {
    private val moshi = Moshi.Builder().build()
    private val reactionListType = Types.newParameterizedType(List::class.java, Reaction::class.java)
    private val adapter = moshi.adapter<List<Reaction>>(reactionListType)

    @TypeConverter
    fun fromReactionList(reactions: List<Reaction>?): String? {
        return adapter.toJson(reactions ?: emptyList())
    }

    @TypeConverter
    fun toReactionList(json: String?): List<Reaction>? {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}