package todo.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ParticipantDao {
    @Query("SELECT * from participants ORDER BY _id ASC")
    fun getAll(): LiveData<List<Participant>>

    @Query("SELECT * FROM participants WHERE _id=:id ")
    fun getById(id: Int): LiveData<Participant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Participant)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: Participant)

    @Query("DELETE FROM participants")
    suspend fun deleteAll()
}