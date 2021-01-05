package todo.data.remote

import todo.data.local.OfflineOperation
import todo.data.local.Participant

object OfflineParticipantRepository {
    private var offlineParticipants:HashMap<Int,OfflineOperation> = HashMap()

    fun add(participant:Participant){
        offlineParticipants[participant._id] = OfflineOperation("CREATED",
            Participant(0,participant.name,participant.age,participant.participationsNo))
    }
    fun delete(id:Int){
        offlineParticipants[id] = OfflineOperation("DELETED", Participant(id,"","",""))
    }
    fun update(participant: Participant){
        offlineParticipants[participant._id] = OfflineOperation("UPDATED",
            Participant(participant._id,participant.name,participant.age,participant.participationsNo)
        )
    }
    fun remove(id:Int){
        offlineParticipants.remove(id);
    }
    fun getOfflineParticipants():HashMap<Int,OfflineOperation>{
        return offlineParticipants
    }
}