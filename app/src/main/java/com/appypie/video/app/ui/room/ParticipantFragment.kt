package com.appypie.video.app.ui.room

import android.os.Bundle
import android.view.View
import com.twilio.video.RemoteParticipant
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.util.ParticipantListener
import kotlinx.android.synthetic.main.participant_grid.*

class ParticipantFragment(var roomFragment: RoomFragment) : BaseFragment(), ParticipantListener {


    override fun layoutRes(): Int {
        return R.layout.participant_grid
    }

    lateinit var myRemoteParticipants: MyRemoteParticipants


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myRemoteParticipants = MyRemoteParticipants(participantGrid, roomFragment)

        roomFragment.setParticipantListener(this)


    }

    override fun controlParticipant(remoteParticipant: RemoteParticipant, isAdded: Boolean) {

        if (isAdded) {
            myRemoteParticipants.addOrUpdate(remoteParticipant)
        } else {
            myRemoteParticipants.removeOrEmpty(remoteParticipant)
        }

    }

}