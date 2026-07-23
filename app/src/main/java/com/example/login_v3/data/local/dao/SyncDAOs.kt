package com.example.login_v3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.login_v3.data.local.entities.BlockedUserLocalEntity
import com.example.login_v3.data.local.entities.FriendLocalEntity
import com.example.login_v3.data.local.entities.RoomLocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends")
    fun getAllFriendsFlow(): Flow<List<FriendLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(friends: List<FriendLocalEntity>)

    @Query("DELETE FROM friends WHERE userId = :userId")
    suspend fun deleteById(userId: String)

    @Query("DELETE FROM friends")
    suspend fun deleteAll()
}

@Dao
interface RoomLocalDao {
    @Query("SELECT * FROM rooms ORDER BY isPinned DESC")
    fun getAllRoomsFlow(): Flow<List<RoomLocalEntity>>

    @Query("SELECT * FROM rooms WHERE roomId = :roomId")
    fun getRoomFlow(roomId: String): Flow<RoomLocalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rooms: List<RoomLocalEntity>)

    @Query("DELETE FROM rooms WHERE roomId = :roomId")
    suspend fun deleteById(roomId: String)

    @Query("DELETE FROM rooms")
    suspend fun deleteAll()
}

@Dao
interface BlockedUserDao {
    @Query("SELECT * FROM blocks")
    fun getAllBlocksFlow(): Flow<List<BlockedUserLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(blocks: List<BlockedUserLocalEntity>)

    @Query("DELETE FROM blocks WHERE userId = :userId")
    suspend fun deleteById(userId: String)

    @Query("DELETE FROM blocks")
    suspend fun deleteAll()
}
