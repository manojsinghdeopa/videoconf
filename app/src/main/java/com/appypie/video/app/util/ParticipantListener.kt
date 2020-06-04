package com.appypie.video.app.util

import com.twilio.video.RemoteParticipant
import com.appypie.video.app.ui.room.RoomEvent

interface ParticipantListener {

    fun controlParticipant(remoteParticipant: RemoteParticipant,isAdded:Boolean)

}