package com.appypie.video.app.ui.addEditMeeting

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.appypie.video.app.R
import com.appypie.video.app.ui.addEditMeeting.AddMeetingFragment.Companion.timeZone
import kotlinx.android.synthetic.main.timezone_list_item.view.*


class TimeZoneListAdapter(private var list: MutableList<TimeZoneModel>, var etTimeZone: AppCompatEditText, var dialog: Dialog) : RecyclerView.Adapter<TimeZoneListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.timezone_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tvTimeZone.text = list[position].timeZone


    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                timeZone = list[adapterPosition].timeZoneId
                etTimeZone.setText(list[adapterPosition].timeZone)
                dialog.dismiss()
            }
        }
    }

    fun filterList(filterList: MutableList<TimeZoneModel>) {
        list = filterList
        notifyDataSetChanged()
    }

}