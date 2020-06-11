package com.appypie.video.app.ui.room

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appypie.video.app.R
import com.appypie.video.app.util.Constants.meetingData
import kotlinx.android.synthetic.main.participant_list_adapter.view.*
import java.util.*
import kotlin.collections.ArrayList


class ParticipantListAdapter(var list: MutableList<Item>) : RecyclerView.Adapter<ParticipantListAdapter.ViewHolder>() {

    private var colors: MutableList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.participant_list_adapter, parent, false)
        participantListAdapter = this
        colors.add("#5E97F6")
        colors.add("#9CCC65")
        colors.add("#FF8A65")
        colors.add("#9E9E9E")
        colors.add("#9FA8DA")
        colors.add("#90A4AE")
        colors.add("#AED581")
        colors.add("#F6BF26")
        colors.add("#FFA726")
        colors.add("#4DD0E1")
        colors.add("#BA68C8")
        colors.add("#A1887F")
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewName.text = list[position].identity


        if (list[position].muted) {
            holder.ivAudioStatus.setImageResource(R.drawable.mute_red)
        } else {
            holder.ivAudioStatus.setImageResource(R.drawable.audio_gray)
        }


        if (list[position].video) {
            holder.ivVideoStatus.setImageResource(R.drawable.video_on_gray)
        } else {
            holder.ivVideoStatus.setImageResource(R.drawable.video_off_red)
        }


        val r = Random()
        val i1 = r.nextInt(11 - 0) + 0

        val draw = GradientDrawable()
        draw.shape = GradientDrawable.OVAL
        draw.setColor(Color.parseColor(colors[i1]))


        val test = list[position].identity
        val firstText = test.substring(0, 1)

        holder.ivThumb.background = draw
        holder.ivThumb.text = firstText

        if (list[position].identity.trim() == meetingData.hostName!!.trim()) {
            holder.itemView.tvType.text = "(Host)"
            holder.itemView.tvType.visibility = View.VISIBLE
        } else {
            holder.itemView.tvType.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView = itemView.findViewById<View>(R.id.tvName) as TextView

        var ivAudioStatus = itemView.findViewById<View>(R.id.ivAudioStatus) as ImageView
        var ivVideoStatus = itemView.findViewById<View>(R.id.ivVideoStatus) as ImageView
        var ivThumb = itemView.findViewById<View>(R.id.ivThumb) as TextView

    }

    fun filterList(filterList: MutableList<Item>) {
        list = filterList
        notifyDataSetChanged()
    }

    companion object {
        var participantListAdapter: ParticipantListAdapter? = null
    }


}