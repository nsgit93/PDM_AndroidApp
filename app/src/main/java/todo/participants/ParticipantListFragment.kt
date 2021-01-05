package app.todo.participants

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import auth.data.AuthRepository
import com.example.childrencompetition.R
import core.TAG
import kotlinx.android.synthetic.main.fragment_participant_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import todo.data.remote.ParticipantServerRepository

class ParticipantListFragment : Fragment() {
    private lateinit var itemListAdapter: ParticipantListAdapter
    private lateinit var itemsModel: ParticipantListViewModel

    companion object{
        var listenForNotifications = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onStart() {
        super.onStart()
        listenForNotifications = true
        CoroutineScope(Dispatchers.Main).launch { ParticipantServerRepository.receiveNotifications() }
    }

    override fun onStop() {
        super.onStop()
        listenForNotifications = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_participant_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn) {
            findNavController().navigate(R.id.fragment_login)
            return;
        }
        setupItemList()
        fab_add.setOnClickListener {
            Log.v(TAG, "add new item")
            findNavController().navigate(R.id.action_ItemListFragment_to_ItemEditFragment)
        }
    }

    private fun setupItemList() {
        itemListAdapter = ParticipantListAdapter(this)
        participant_list.adapter = itemListAdapter
        itemsModel = ViewModelProvider(this).get(ParticipantListViewModel::class.java)

        itemsModel.items.observe(viewLifecycleOwner, { items ->
            Log.v(TAG, "update items")
            itemListAdapter.items = items
        })

        itemsModel.loading.observe(viewLifecycleOwner, { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        })

        itemsModel.loadingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        })

        itemsModel.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }
}


/*
class ParticipantListFragment : Fragment() {
    private lateinit var participantListAdapter: ParticipantListAdapter
    private lateinit var participantsModel: ParticipantListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_participant_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn) {
            findNavController().navigate(R.id.fragment_login)
            return;
        }
        setupParticipantList()
        fab.setOnClickListener {
            Log.v(TAG, "add new item")
            findNavController().navigate(R.id.fragment_participant_edit)
        }
    }

    private fun setupParticipantList() {
        participantListAdapter = ParticipantListAdapter(this)
        participant_list.adapter = participantListAdapter
        participantsModel = ViewModelProvider(this).get(ParticipantListViewModel::class.java)
        participantsModel.items.observe(viewLifecycleOwner, { items ->
            Log.v(TAG, "update items")
            participantListAdapter.items = items
        })
        participantsModel.loading.observe(viewLifecycleOwner, { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        })
        participantsModel.loadingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        })
        participantsModel.loadItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }
}
 */