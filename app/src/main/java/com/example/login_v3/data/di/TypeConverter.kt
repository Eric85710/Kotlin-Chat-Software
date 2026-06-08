package com.example.login_v3.data.di

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory // 👈 確保有 import 這個
import com.example.login_v3.data.api.api_class.Reaction

class RoomConverters {
    // 💡 核心修正：加入 KotlinJsonAdapterFactory 賦予 Moshi 反射辨識 Kotlin Data Class 的能力
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

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