package todo.data.remote

import android.os.Build
import android.util.Log
import app.todo.participants.ParticipantListFragment
import core.RemoveNotification
import core.Result
import core.WebSocketNotifications
import core.getJson
import todo.data.local.Participant

object ParticipantServerRepository {
    private var cachedParticipants: MutableList<Participant>? = null;

    /*fun loadAll(): LiveData<List<Participant>> {
        var participants: List<Participant> = cachedParticipants as List<Participant>
        var data: LiveData<List<Participant>> = MutableLiveData<List<Participant>>(participants)
        return data;
    }*/

    /*suspend fun loadAll(): Result<List<Participant>> {
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
    }*/

    suspend fun load(participantId: Int): Result<Participant> {
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

    suspend fun saveInMemory(participant: Participant): Result<Participant> {
        try {
            cachedParticipants?.add(participant)
            return Result.Success(participant)
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
    }

    suspend fun updateInMemory(participant: Participant): Result<Participant> {
        try {
            val index = cachedParticipants?.indexOfFirst { it._id == participant._id }
            if (index != null) {
                cachedParticipants?.set(index, participant)
            }
            return Result.Success(participant)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun receiveNotifications() {
        while (ParticipantListFragment.listenForNotifications) {
            val notification = WebSocketNotifications.channel.receive();
            Log.d("EventsListFragment", "Notification received in list view model")

            when (notification.type) {
                "deleted" -> {
                    Log.d("delete","delete");
                    val removeNotif = getJson().fromJson(
                        getJson().toJson(notification.payload),
                        RemoveNotification::class.java
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cachedParticipants?.removeIf{it._id.toString()==removeNotif._id}
                    }
                }
                "updated"-> {
                    Log.d("Update","Update")
                    val updatedParticipant:Participant = getJson().fromJson(getJson().toJson(notification.payload),Participant::class.java)
                    val index = cachedParticipants?.indexOfFirst { it._id==updatedParticipant._id }

                    if (index != null)
                        cachedParticipants?.set(index,updatedParticipant)
                    Log.d("Update Index", "Index:$index")
                }
                "created"->{
                    val newParticipant = getJson().fromJson(getJson().toJson(notification.payload),Participant::class.java)
                    cachedParticipants?.add(newParticipant)
                }
            }
        }

    }


}