package com.appypie.video.app.ui.addEditMeeting

import com.twilio.video.VideoTrack

class TimeZoneModel internal constructor(

        var timeZone: String,
        /** RemoteParticipant name.  */
        var timeZoneId: String)