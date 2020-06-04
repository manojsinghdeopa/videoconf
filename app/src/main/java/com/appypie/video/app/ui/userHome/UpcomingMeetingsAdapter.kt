package com.appypie.video.app.ui.userHome

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appypie.video.app.R
import com.appypie.video.app.ui.addEditMeeting.MeetingContainerActivity
import com.appypie.video.app.ui.common.ContainerActivity
import com.appypie.video.app.ui.common.MeetingData
import com.appypie.video.app.util.CommonMethod.Companion.convertDateToTime
import com.appypie.video.app.util.CommonMethod.Companion.convertMinuteToHour
import com.appypie.video.app.util.CommonMethod.Companion.formatTimeZone
import com.appypie.video.app.util.Constants.*
import kotlinx.android.synthetic.main.upcoming_meeting_list_adapter.view.*
import java.util.*


class UpcomingMeetingsAdapter(var list: MutableList<MeetingData>) : RecyclerView.Adapter<UpcomingMeetingsAdapter.ViewHolder>() {


    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.upcoming_meeting_list_adapter, parent, false)
        context = v.context
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.itemView.tvMeetingName.text = list[position].topic
        holder.itemView.tvMeetingID.text = "Meeting ID : " + list[position].meetingId
        holder.itemView.tvDuration.text = list[position].startTime + " ( " + convertMinuteToHour(list[position].duration!!) + " ) "


        val tz = TimeZone.getDefault()
        if (list[position].timeZone == tz.id) {
            holder.itemView.tvGlobalTime.visibility = View.GONE
        } else {
            tz.id = list[position].timeZone
            holder.itemView.tvGlobalTime.visibility = View.VISIBLE
            holder.itemView.tvGlobalTime.text = convertDateToTime(list[position].serverStartDatetime.toString()) + " " + formatTimeZone(tz)
        }

        val startDate: String = list[position].serverStartTimestamp.toString()
        val status: String = list[position].status.toString()

        /*if (isPastDate(startDate)) {*/
        if (status == "Completed") {
            holder.itemView.btnStartUpcoming.textSize = 14f
            holder.itemView.btnStartUpcoming.background = null
            holder.itemView.btnStartUpcoming.setTextColor(context.resources.getColor(R.color.blueColor))
            holder.itemView.btnStartUpcoming.text = context.getText(R.string.completed)
            holder.itemView.btnStartUpcoming.isClickable = false
        } else {
            holder.itemView.btnStartUpcoming.setBackgroundResource(R.drawable.border_linear_oval)
            holder.itemView.btnStartUpcoming.setTextColor(context.resources.getColor(R.color.blueColor))
            holder.itemView.btnStartUpcoming.text = context.getString(R.string.start)
            holder.itemView.btnStartUpcoming.isClickable = true
        }
        /*}*/

    }


    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.btnStartUpcoming.setOnClickListener {
                meetingData = list[adapterPosition]
                SELECTED_MEETING_ID = list[adapterPosition].meetingId
                SELECTED_MEETING_LINK = list[adapterPosition].meetingLink
                FROM = context.getString(R.string.scheduled)
                itemView.context.startActivity(Intent(itemView.context, ContainerActivity::class.java).putExtra(TITLE, itemView.context.getString(R.string.start_meeting)))
            }


            itemView.setOnClickListener {
                meetingData = list[adapterPosition]
                context.startActivity(Intent(context, MeetingContainerActivity::class.java).putExtra(FROM, "MeetingList"))
            }
        }
    }

    fun updateList(newList: MutableList<MeetingData>) {
        list = newList
        notifyDataSetChanged()
    }


}