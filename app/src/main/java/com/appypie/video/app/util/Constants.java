package com.appypie.video.app.util;

import com.appypie.video.app.ui.common.MeetingData;
import com.twilio.video.AspectRatio;
import com.twilio.video.VideoDimensions;

import java.util.regex.Pattern;

import static com.twilio.video.AspectRatio.ASPECT_RATIO_11_9;
import static com.twilio.video.AspectRatio.ASPECT_RATIO_16_9;
import static com.twilio.video.AspectRatio.ASPECT_RATIO_4_3;

public class Constants {

    public static AspectRatio[] aspectRatios = new AspectRatio[]{ASPECT_RATIO_4_3, ASPECT_RATIO_16_9, ASPECT_RATIO_11_9};

    public static VideoDimensions[] videoDimensions =
            new VideoDimensions[]{
                    VideoDimensions.CIF_VIDEO_DIMENSIONS,
                    VideoDimensions.VGA_VIDEO_DIMENSIONS,
                    VideoDimensions.WVGA_VIDEO_DIMENSIONS,
                    VideoDimensions.HD_540P_VIDEO_DIMENSIONS,
                    VideoDimensions.HD_720P_VIDEO_DIMENSIONS,
                    VideoDimensions.HD_960P_VIDEO_DIMENSIONS,
                    VideoDimensions.HD_S1080P_VIDEO_DIMENSIONS,
                    VideoDimensions.HD_1080P_VIDEO_DIMENSIONS
            };


    public static String APP_TYPE = "APP_TYPE";
    public static final String GUEST = "GUEST";
    public static final String USER = "USER";
    public static final String FROM_NOTIFICATION = "FROM_NOTIFICATION";

    public static final String SERVER_ERROR = "503 Service Temporarily Unavailable, Try Again !";
    public static final String TWILIO_TOKEN = "TWILIO_TOKEN";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    public static final String ACCESS_TOKEN_EXPIRE_TIME = "ACCESS_TOKEN_EXPIRE_TIME";
    public static final String REFRESH_TOKEN_EXPIRE_TIME = "REFRESH_TOKEN_EXPIRE_TIME";

    public static final String AUDIO_MODE_OFF = "AUDIO_MODE_OFF";
    public static final String VIDEO_MODE_OFF = "VIDEO_MODE_OFF";

    public static final String HOST = "videoconfer.pbodev.info";
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String ACCEPT = "*/*";
    public static final String AUTH_TOKEN_HEADER = "Basic YXBwbGljYXRpb246c2VjcmV0";

    public static final String API_SUCCESS_MSG = "success";


    public static final String SHOW_BOTTOM_PREVIEW = "SHOW_BOTTOM_PREVIEW";
    public static final String SHOW_VIDEO_PREVIEW = "SHOW_VIDEO_PREVIEW";


    public static final String REFRESH_GRANT_TYPE = "refresh_token";
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String AUTH_TOKEN_USER = "pedroetb";
    public static final String AUTH_TOKEN_PASSWORD = "password";


    public static final String EAR_PIECE = "Earpiece";
    public static final String SPEAKER_PHONE = "Speakerphone";

    public static final String APP_ID = "f3a3f207d3f6";
    public static final String HOST_ID = "176384";
    public static final String HOST_EMAIL = "ruchi@appypiellp.com";
    public static final String HOST_NAME = "Ruchi";

    public static Boolean isInternetAudioEnable = false;
    public static Boolean IsMeetingScheduled = false;

    public static Boolean shouldMeetingRefresh = false;

    public static Boolean shouldBindRoom = false;
    public static Boolean resumeMeeting = false;

    public static String PERSONAL_MEETING_ID = "PERSONAL_MEETING_ID";
    public static String PERSONAL_MEETING_LINK = "PERSONAL_MEETING_LINK";
    public static String PERSONAL_MEETING_NAME = "PERSONAL_MEETING_NAME";

    public static String SELECTED_MEETING_ID = "SELECTED_MEETING_ID";
    public static String SELECTED_MEETING_LINK = "SELECTED_MEETING_LINK";

    public static final String TITLE = "TITLE";

    public static String DEVICE_TOKEN = "DEVICE_TOKEN";
    public static String DEVICE_TYPE = "ANDROID";
    public static String DEVICE_ID = "DEVICE_ID";


    public static String CURRENT_MEETING_ID = "CURRENT_MEETING_ID";
    public static String CURRENT_MEETING_PASSWORD = "CURRENT_MEETING_PASSWORD";
    public static String CURRENT_USER_NAME = "CURRENT_USER_NAME";
    public static String CURRENT_USER_EMAIL = "CURRENT_USER_EMAIL";

    public static String END_MEETING_BROADCAST = "END_MEETING_BROADCAST";

    public static String FROM = "FROM";
    public static String EMPTY = "EMPTY";

    public static Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
    public static final String RANDOM_ALLOWED_CHARACTERS = "0123456789QWERTYabcfghUIOUPASDFkhshsdGHJKLZXCRxcbnxcmnBNM";


    public static MeetingData personalMeetingData = null;
    public static MeetingData meetingData = null;


}
