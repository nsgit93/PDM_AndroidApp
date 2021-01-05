package app.todo.participant

//import android.content.ClipData
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import core.Result
import core.TAG
import kotlinx.coroutines.launch
import todo.data.local.Participant
import todo.data.local.ParticipantDatabase
import todo.data.remote.OfflineParticipantRepository
import todo.data.remote.ParticipantRoomRepository
import todo.data.remote.ParticipantServerRepository

class ParticipantEditViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    val participantRoomRepository: ParticipantRoomRepository

    var connected:Boolean = true


    init {
        val participantDao = ParticipantDatabase.getDatabase(application, viewModelScope).participantDao()
        participantRoomRepository = ParticipantRoomRepository(participantDao)

    }

    fun getItemById(itemId: String): LiveData<Participant> {
        Log.v(TAG, "getParticipantById...")
        return participantRoomRepository.getById(itemId.toInt())
    }

    fun saveOrUpdateItem(item: Participant) {
        viewModelScope.launch {
            Log.v(TAG, "saveOrUpdateItem...");
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Participant>
            if (item._id != 0) {
                if(connected) {
                    Log.i("ParticipantEditViewMod","Connected - update")
                    result = participantRoomRepository.update(item)
                }
                else{
                    Log.i("ParticipantEditViewMod","Offline - update")
                    OfflineParticipantRepository.update(item)
                    result = ParticipantServerRepository.updateInMemory(item)
                }
            } else {
                if(connected) {
                    Log.i("ParticipantEditViewMod","Connected - create")
                    result = participantRoomRepository.save(item)
                }
                else{
                    Log.i("ParticipantEditViewMod","Offline - create")
                    OfflineParticipantRepository.add(item)
                    result = ParticipantServerRepository.saveInMemory(item)
                }
            }
            when(result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdateItem succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdateItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }
    /*
    private val mutableItem = MutableLiveData<Participant>().apply { value = Participant("", "", "", "") }
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val item: LiveData<Participant> = mutableItem
    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    fun loadItem(participantId: String) {
        viewModelScope.launch {
            Log.v(TAG, "loadItem...");
            mutableFetching.value = true
            mutableException.value = null
            when (val result = ParticipantRepository.load(participantId)) {
                is Result.Success -> {
                    Log.d(TAG, "loadItem succeeded");
                    mutableItem.value = result.data
                }
                is Result.Error -> {
                    Log.w(TAG, "loadItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableFetching.value = false
        }
    }

    fun saveOrUpdateItem(name: String, age: String) {
        viewModelScope.launch {
            Log.v(TAG, "saveOrUpdateItem...");
            val item = mutableItem.value ?: return@launch
            item.name = name
            item.age = age
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Participant>
            if (item._id.isNotEmpty()) {
                result = ParticipantRepository.update(item)
            } else {
                result = ParticipantRepository.save(item)
            }
            when (result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdateItem succeeded");
                    mutableItem.value = result.data
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdateItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }*/
}
