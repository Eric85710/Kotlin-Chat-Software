package com.example.login_v3.data.di

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory // 👈 確保有 import 這個
import com.example.login_v3.data.api.api_class.LastMessage
import com.example.login_v3.data.api.api_class.Partner
import com.example.login_v3.data.api.api_class.Reaction
import com.example.login_v3.data.api.api_class.Attachment

class RoomConverters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val reactionListType = Types.newParameterizedType(List::class.java, Reaction::class.java)
    private val reactionAdapter = moshi.adapter<List<Reaction>>(reactionListType)
    private val partnerAdapter = moshi.adapter(Partner::class.java)
    private val lastMessageAdapter = moshi.adapter(LastMessage::class.java)
    private val attachmentAdapter = moshi.adapter(Attachment::class.java)

    @TypeConverter
    fun fromReactionList(reactions: List<Reaction>?): String? {
        return reactionAdapter.toJson(reactions ?: emptyList())
    }

    @TypeConverter
    fun toReactionList(json: String?): List<Reaction>? {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            reactionAdapter.fromJson(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromPartner(partner: Partner?): String? {
        return partnerAdapter.toJson(partner)
    }

    @TypeConverter
    fun toPartner(json: String?): Partner? {
        if (json.isNullOrBlank()) return null
        return try {
            partnerAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromLastMessage(lastMessage: LastMessage?): String? {
        return lastMessageAdapter.toJson(lastMessage)
    }

    @TypeConverter
    fun toLastMessage(json: String?): LastMessage? {
        if (json.isNullOrBlank()) return null
        return try {
            lastMessageAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return attachmentAdapter.toJson(attachment)
    }

    @TypeConverter
    fun toAttachment(json: String?): Attachment? {
        if (json.isNullOrBlank()) return null
        return try {
            attachmentAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
