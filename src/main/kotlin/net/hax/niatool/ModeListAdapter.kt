package net.hax.niatool

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.hax.niatool.modes.ModeRegistry

class ModeListAdapter : RecyclerView.Adapter<ModeListAdapter.Holder>() {

    private val modeNames = ModeRegistry.getModeNames().toList()

    private var selected: Holder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.mode_list_item, parent, false))

    override fun getItemCount(): Int = modeNames.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.modeId = modeNames[position]

        val modeInfo = ModeRegistry.getModeModule(holder.modeId)!!
                .getModeMetadata(holder.itemView.context.resources)

        holder.tvName.text = modeInfo.name
        holder.tvDescription.text = modeInfo.description
        holder.layout.setOnClickListener {
            if (holder == selected) return@setOnClickListener

            selected!!.layout.isSelected = false
            it.isSelected = !it.isSelected
            selected = holder

            ApplicationSettings.currentMode = holder.modeId
            ApplicationSettings.saveApplicationPreferences()
        }

        if (selected == null && holder.modeId == ApplicationSettings.currentMode) {
            holder.layout.isSelected = true
            selected = holder
        }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var modeId: String = "?"
        val layout = itemView.findViewById(R.id.card_inner) as ViewGroup
        val tvName = itemView.findViewById(R.id.mode_name) as TextView
        val tvDescription = itemView.findViewById(R.id.mode_description) as TextView
    }

}
