package app.todo.participants

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import app.todo.participant.ParticipantEditFragment
import com.example.childrencompetition.R
import core.TAG
import kotlinx.android.synthetic.main.view_participant.view.*
import todo.data.local.Participant

class ParticipantListAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<ParticipantListAdapter.ViewHolder>() {

    var items = emptyList<Participant>()
        set(value) {
            field = value
            notifyDataSetChanged();
        }



    private var onItemClick: View.OnClickListener;

    init {
        onItemClick = View.OnClickListener { view ->
            val item = view.tag as Participant
            fragment.findNavController().navigate(R.id.fragment_participant_edit, Bundle().apply {
                putString(ParticipantEditFragment.ITEM_ID, item._id.toString())
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_participant, parent, false)
        Log.v(TAG, "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v(TAG, "onBindViewHolder $position")
        val item = items[position]
        holder.itemView.tag = item
        holder.textView.text = item.name
        holder.itemView.setOnClickListener(onItemClick)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.text
    }
}
