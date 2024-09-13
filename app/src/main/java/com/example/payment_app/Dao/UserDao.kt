package com.example.payment_app.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.payment_app.Models.User

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM User LIMIT 1")
    suspend fun getSingleUser(): User?

    // You can also add more methods here for querying or updating users in the future
}
