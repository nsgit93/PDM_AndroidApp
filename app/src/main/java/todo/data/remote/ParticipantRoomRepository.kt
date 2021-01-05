package todo.data.remote

import android.util.Log
import androidx.lifecycle.LiveData
import core.Result
import todo.data.local.Participant
import todo.data.local.ParticipantDao

class ParticipantRoomRepository(private val participantDAO: ParticipantDao) {
    val items = participantDAO.getAll()

    suspend fun refresh(): Result<Boolean> {
        Log.i("ParticipantRepository","refresh")
        try {
            val items = ParticipantApi.service.find()
            for (item in items) {
                Log.i("ParticipantRepository",item.toString())
                participantDAO.insert(item)
            }
            return Result.Success(true)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    fun getById(itemId: Int): LiveData<Participant> {
        return participantDAO.getById(itemId)
    }

    suspend fun save(item: Participant): Result<Participant> {
        try {
            val createdItem = ParticipantApi.service.create(item)
            participantDAO.insert(createdItem)
            return Result.Success(createdItem)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(item: Participant): Result<Participant> {
        try {
            val updatedItem = ParticipantApi.service.update(item._id, item)
            participantDAO.update(updatedItem)
            return Result.Success(updatedItem)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    /*
    private var cachedParticipants: MutableList<Participant>? = null;

    suspend fun loadAll(): Result<List<Participant>> {
        if (cachedParticipants != null) {
            return Result.Success(cachedParticipants as List<Participant>);
        }
        try {
            val participants = ParticipantApi.service.find()
            cachedParticipants = mutableListOf()
            cachedParticipants?.addAll(participants)
            return Result.Success(cachedParticipants as List<Participant>)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun load(participantId: String): Result<Participant> {
        val participant = cachedParticipants?.find { it._id == participantId }
        if (participant != null) {
            return Result.Success(participant)
        }
        try {
            return Result.Success(ParticipantApi.service.read(participantId))
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun save(participant: Participant): Result<Participant> {
        try {
            val createdarticipant = ParticipantApi.service.create(participant)
            cachedParticipants?.add(createdarticipant)
            return Result.Success(createdarticipant)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(participant: Participant): Result<Participant> {
        try {
            val updatedParticipant = ParticipantApi.service.update(participant._id, participant)
            val index = cachedParticipants?.indexOfFirst { it._id == participant._id }
            if (index != null) {
                cachedParticipants?.set(index, updatedParticipant)
            }
            return Result.Success(updatedParticipant)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }*/
}