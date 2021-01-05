package app.todo.participants

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import core.Result
import core.TAG
import kotlinx.coroutines.launch
import todo.data.local.OfflineOperation
import todo.data.local.Participant
import todo.data.local.ParticipantDatabase
import todo.data.remote.OfflineParticipantRepository
import todo.data.remote.ParticipantRoomRepository

class ParticipantListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val items: LiveData<List<Participant>>
    val offline_items: HashMap<Int, OfflineOperation>

    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val itemRoomRepository: ParticipantRoomRepository




    init {
        val itemDao = ParticipantDatabase.getDatabase(application, viewModelScope).participantDao()
        itemRoomRepository = ParticipantRoomRepository(itemDao)
        items = itemRoomRepository.items
        offline_items = OfflineParticipantRepository.getOfflineParticipants();
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(TAG, "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = itemRoomRepository.refresh()) {
                is Result.Success -> {
                    Log.d(TAG, "refresh succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "refresh failed", result.exception);
                    mutableException.value = result.exception
                    }

                }
            }
            mutableLoading.value = false
        }

}

/*
class ParticipantListViewModel : ViewModel() {
    private val mutableItems = MutableLiveData<List<Participant>>().apply { value = emptyList() }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val items: LiveData<List<Participant>> = mutableItems
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    fun loadItems() {
        viewModelScope.launch {
            Log.v(TAG, "loadItems...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = ParticipantRepository.loadAll()) {
                is Result.Success -> {
                    Log.d(TAG, "loadItems succeeded");
                    mutableItems.value = result.data
                }
                is Result.Error -> {
                    Log.w(TAG, "loadItems failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }

}
*/