package todo.data.local

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import todo.data.remote.OfflineParticipantRepository
import todo.data.remote.ParticipantServerRepository

class OfflineWorker(
        context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {


    @RequiresApi(Build.VERSION_CODES.N)
    override fun doWork(): Result {

        // perform long running operation
        val offlineParticipants:HashMap<Int,OfflineOperation>  = OfflineParticipantRepository.getOfflineParticipants()

        if(offlineParticipants.entries.size>0) {
            offlineParticipants.entries.removeIf {
                when (it.value.type) {
                    "DELETED" -> {
//                        CoroutineScope(Dispatchers.Main).launch { ParticipantServerRepository.delete(it.key) }
//                        return@removeIf true
                    }
                    "UPDATED" -> {
                        CoroutineScope(Dispatchers.Main).launch { ParticipantServerRepository.update(it.value.participant) }
                        return@removeIf true
                    }
                    "CREATED" -> {
                        val participant = Participant(0, it.value.participant.name,
                            it.value.participant.age, it.value.participant.participationsNo)
                        CoroutineScope(Dispatchers.Main).launch { ParticipantServerRepository.save(participant) }
                        OfflineParticipantRepository.delete(it.key)
                        return@removeIf false
                    }
                }
                return@removeIf true
            }
        }
/*
        if(offlineOperations?.size!! > 0) {
            offlineOperations.forEach {
                when (it.type) {
                    "CREATED" -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            val part = participantRepo.getById(it.idParticipant).value
                            participantRepo.save(part!!)
                        }
                    }
                    "DELETED" -> {
                        Log.i("OfflineWorker","delete participant offline")
                    }
                    "UPDATED" -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            val part = participantRepo.getById(it.idParticipant).value
                            participantRepo.update(part!!)
                        }
                    }
                }
            }
        }

 */


        return Result.success()
    }
}