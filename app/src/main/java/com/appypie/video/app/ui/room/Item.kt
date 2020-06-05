package com.appypie.video.app.ui.room

import com.twilio.video.VideoTrack

class Item internal constructor(
        /** RemoteParticipant unique identifier.  */
        var sid: String,
        /** RemoteParticipant name.  */
        var identity: String,
        /** RemoteParticipant audio state.  */
        var muted: Boolean,
        /** Video track enabled/disabled.  */
        var video: Boolean)