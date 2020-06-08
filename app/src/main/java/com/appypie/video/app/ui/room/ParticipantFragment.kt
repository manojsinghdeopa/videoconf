package com.appypie.video.app.ui.room

import android.os.Bundle
import android.view.View
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.util.ParticipantListener
import com.twilio.video.RemoteParticipant
import kotlinx.android.synthetic.main.participant_grid.*

class ParticipantFragment(var roomFragment: RoomFragment) : BaseFragment(), ParticipantListener {


    override fun layoutRes(): Int {
        return R.layout.participant_grid
    }

    lateinit var myRemoteParticipants: MyRemoteParticipants


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myRemoteParticipants = MyRemoteParticipants(participantGrid, roomFragment)

        roomFragment.participantListener(this)
    }

    override fun controlParticipant(remoteParticipant: RemoteParticipant, isAdded: Boolean) {

        if (isAdded) {
            myRemoteParticipants.addOrUpdate(remoteParticipant)
        } else {
            myRemoteParticipants.removeOrEmpty(remoteParticipant)
            MyRemoteParticipants.participantList.remove(remoteParticipant.sid)
        }


        if (MyRemoteParticipants.thumbs.isEmpty()) {
            tvNoParticipants.visibility = View.VISIBLE
        } else {
            tvNoParticipants.visibility = View.GONE
        }

    }

}