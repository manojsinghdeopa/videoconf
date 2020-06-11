package com.appypie.video.app.ui.room

import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.ButterKnife
import butterknife.OnClick
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.data.Preferences
import com.appypie.video.app.ui.home.VideoRoomInitializer
import com.appypie.video.app.ui.room.MyRemoteParticipants.ParticipantClickListener
import com.appypie.video.app.ui.room.ParticipantListAdapter.Companion.participantListAdapter
import com.appypie.video.app.ui.room.RoomEvent.*
import com.appypie.video.app.ui.room.RoomViewEvent.*
import com.appypie.video.app.ui.room.RoomViewModel.RoomViewModelFactory
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CameraCapturerCompat
import com.appypie.video.app.util.CommonMethod.Companion.getHeaderMap
import com.appypie.video.app.util.CommonMethod.Companion.showToast
import com.appypie.video.app.util.Constants
import com.appypie.video.app.util.Constants.isInternetAudioEnable
import com.appypie.video.app.util.ParticipantListener
import com.appypie.video.app.webservices.AuthServiceError
import com.appypie.video.app.webservices.CommonResponse
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.twilio.video.*
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.end_meeting_dialog.view.*
import kotlinx.android.synthetic.main.room_fragment_bottom.*
import kotlinx.android.synthetic.main.room_fragment_header.*
import kotlinx.android.synthetic.main.room_viewpager_layout.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class RoomFragment : BaseFragment(), VideoRoomInitializer, ParticipantClickListener {

    @JvmField
    @Inject
    var sharedPreferences: SharedPreferences? = null

    @JvmField
    @Inject
    var roomManager: RoomManager? = null

    @JvmField
    @Inject
    var audioDeviceSelector: AudioDeviceSelector? = null

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    var endMeetingViewModel: EndMeetingViewModel? = null
    private var restoreLocalVideoCameraTrack = false
    private var localParticipant: LocalParticipant? = null
    var localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
    private var participantController: ParticipantController? = null
    private var primaryVideoView: ParticipantPrimaryView? = null
    private var thumbnailLinearLayout: ViewGroup? = null
    private var joinStatusTextView: TextView? = null
    var roomFragment: RoomFragment? = null
    var roomViewModel: RoomViewModel? = null
    private var isAudioMuted = AppPrefs.getBoolean(Constants.AUDIO_MODE_OFF)
    private var isVideoMuted = AppPrefs.getBoolean(Constants.VIDEO_MODE_OFF)
    private var savedVolumeControlStream = 0
    private var room: Room? = null
    private var videoConstraints: VideoConstraints? = null
    private var localAudioTrack: LocalAudioTrack? = null
    var cameraVideoTrack: LocalVideoTrack? = null
    private var screenVideoTrack: LocalVideoTrack? = null
    private var cameraCapturer: CameraCapturerCompat? = null
    private val intentFilter = IntentFilter(Constants.END_MEETING_BROADCAST)
    private val broadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getStringExtra("data")
            // disconnectButtonClick();
            onSuccessDisconnect(getString(R.string.meeting_ended))
        }
    }


    @OnClick(R.id.ivDisconnect)
    fun showEndMeetingDialog() {

        val builder = AlertDialog.Builder(requireContext(), R.style.transParentBgAlertDialog)
        val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
        val dialogView: View = LayoutInflater.from(requireContext()).inflate(R.layout.end_meeting_dialog, viewGroup, false)
        builder.setView(dialogView)
        val alertDialog = builder.create()

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val abc = Objects.requireNonNull(alertDialog.window)!!.attributes
        abc.gravity = Gravity.TOP or Gravity.END
        abc.x = 25   //x position
        abc.y = 75   //y position
        alertDialog.show()

        if (Constants.APP_TYPE == Constants.USER) {
            dialogView.btnEnd.text = getString(R.string.end_meeting_for_all)
        } else {
            dialogView.btnEnd.text = getString(R.string.leave_meeting)
        }

        dialogView.btnEnd.setOnClickListener {

            alertDialog.dismiss()

            if (Constants.APP_TYPE == Constants.USER) {
                endMeetingViewModel!!.endMeeting(getHeaderMap(), Constants.APP_ID, Constants.HOST_ID, Constants.CURRENT_MEETING_ID)
            } else {
                endMeetingViewModel!!.leftMeeting(getHeaderMap(), Constants.APP_ID, Constants.CURRENT_USER_NAME, Constants.CURRENT_MEETING_ID)
            }

        }

        dialogView.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    @OnClick(R.id.ivSwitchCamera)
    fun switchCamera() {
        if (cameraCapturer != null) {
            val mirror = cameraCapturer!!.cameraSource == CameraCapturer.CameraSource.BACK_CAMERA
            cameraCapturer!!.switchCamera()
            if (participantController!!.primaryItem != null) {
                if (participantController!!.primaryItem.sid == localParticipantSid) {
                    participantController!!.updatePrimaryThumb(mirror)
                }
            } else {
                participantController!!.updateThumb(localParticipantSid, cameraVideoTrack, mirror)
            }
        }
    }


    var participantListener: ParticipantListener? = null
    fun participantListener(participantListener: ParticipantListener?) {
        this.participantListener = participantListener
    }


    lateinit var roomFragmentUtils: RoomFragmentUtils

    override fun layoutRes(): Int {
        return R.layout.activity_room
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomFragment = this
        val factory = RoomViewModelFactory(roomManager!!, audioDeviceSelector!!)
        roomViewModel = ViewModelProvider(this, factory).get(RoomViewModel::class.java)
        roomViewModel!!.roomEvents.observe(requireActivity(), Observer { roomEvent: RoomEvent? -> bindRoomEvents(roomEvent) })
        endMeetingViewModel = ViewModelProvider(requireActivity(), viewModelFactory!!).get(EndMeetingViewModel::class.java)
        observeViewModels()
        if (savedInstanceState != null) {
            isAudioMuted = savedInstanceState.getBoolean(IS_AUDIO_MUTED)
            isVideoMuted = savedInstanceState.getBoolean(IS_VIDEO_MUTED)
        }


        // So calls can be answered when screen is locked
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        ButterKnife.bind(requireActivity())


        // Cache volume control stream
        savedVolumeControlStream = requireActivity().volumeControlStream


        roomFragmentUtils = RoomFragmentUtils(roomFragment!!, roomView!!)
        roomFragmentUtils.loadViewPager(this)

    }

    private fun observeViewModels() {

        endMeetingViewModel!!.response.observe(requireActivity(), Observer { response: CommonResponse? ->
            if (response != null) {
                if (response.status == 200) {
                    disconnectButtonClick()
                } else {
                    showToast(requireContext(), response.message)
                }
            }
        })
        endMeetingViewModel!!.error.observe(requireActivity(), Observer { isError: Boolean ->
            if (isError) {
                showToast(requireContext(), Constants.SERVER_ERROR)
            }
        })

        endMeetingViewModel!!.isloading.observe(requireActivity(), Observer { isLoading: Boolean ->
            if (isLoading) pbEndMeeting!!.visibility = View.VISIBLE else pbEndMeeting!!.visibility = View.GONE
            if (isLoading) ivDisconnect!!.visibility = View.GONE else pbEndMeeting!!.visibility = View.VISIBLE
        })
    }

    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(broadcastReceiver, intentFilter)
        restoreCameraTrack()
        publishMediaTracks()
        addParticipantViews()
    }

    override fun onVideoLayoutInitialized(primaryVideoView: ParticipantPrimaryView, thumbnailLinearLayout: ViewGroup, joinStatusTextView: TextView) {
        this.primaryVideoView = primaryVideoView
        this.thumbnailLinearLayout = thumbnailLinearLayout
        this.joinStatusTextView = joinStatusTextView
        obtainVideoConstraints()
        bottomLayout
        if (AppPrefs.getBoolean(Constants.SHOW_BOTTOM_PREVIEW)) {
            RoomFragmentUtils(roomFragment!!, roomView!!).setupPreview()
        } else {
            connectToRoom()
        }
    }

    private fun bindRoomEvents(roomEvent: RoomEvent?) {

        if (!Constants.shouldBindRoom) return
        if (roomEvent != null) {
            room = roomEvent.room
            if (room != null) {
                if (roomEvent is RoomState) {
                    when (room!!.state) {
                        Room.State.CONNECTED -> {
                            onStartVideo()
                            toggleAudioDevice(true)
                        }
                        Room.State.DISCONNECTED -> {
                            localParticipant = null
                            room!!.disconnect()
                            room = null
                            removeAllParticipants()
                            localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
                            toggleAudioDevice(false)
                            isInternetAudioEnable = false
                            onSuccessDisconnect("Disconnected")
                        }
                    }
                }
                if (roomEvent is ConnectFailure) {
                    AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
                            .setTitle(getString(R.string.room_screen_connection_failure_title))
                            .setMessage(getString(R.string.room_screen_connection_failure_message))
                            .setNeutralButton("Retry") { dialog: DialogInterface?, which: Int -> connectToRoom() }
                            .show()
                    removeAllParticipants()
                    toggleAudioDevice(false)
                    isInternetAudioEnable = false
                }
                if (roomEvent is ParticipantConnected) {
                    /*val remoteParticipant = roomEvent.remoteParticipant
                    val renderAsPrimary = room!!.remoteParticipants.size == 1
                    addParticipant(remoteParticipant, renderAsPrimary)*/
                    addParticipantViews()
                }
                if (roomEvent is ParticipantDisconnected) {
                    val remoteParticipant = roomEvent.remoteParticipant
                    removeParticipant(remoteParticipant)
                    addParticipantViews()
                }
                if (roomEvent is DominantSpeakerChanged) {
                    val remoteParticipant = roomEvent.remoteParticipant
                    if (remoteParticipant == null) {
                        participantController!!.setDominantSpeaker(null)
                        return
                    }
                    val videoTrack: VideoTrack? = if (remoteParticipant.remoteVideoTracks.size > 0) remoteParticipant
                            .remoteVideoTracks[0]
                            .remoteVideoTrack else null
                    if (videoTrack != null) {
                        val participantView = participantController!!.getThumb(remoteParticipant.sid, videoTrack)
                        if (participantView != null) {
                            participantController!!.setDominantSpeaker(participantView)
                        } else {
                            remoteParticipant.identity
                            val primaryParticipantView = participantController!!.primaryView
                            if (primaryParticipantView.identity == remoteParticipant.identity) {
                                participantController!!.setDominantSpeaker(participantController!!.primaryView)
                            } else {
                                participantController!!.setDominantSpeaker(null)
                            }
                        }
                    }
                }
            } else {
                if (roomEvent is TokenError) {
                    val error = roomEvent.serviceError
                    handleTokenError(error)
                }
            }
            updateUi(room, roomEvent)
        }
    }


    fun connectToRoom() {
        Constants.shouldBindRoom = true
        val viewEvent = Connect("", "", isNetworkQualityEnabled)
        roomViewModel!!.processInput(viewEvent)
    }

    override fun onGridClick(item: MyRemoteParticipants.Item) {
        participantController!!.renderAsPrimary(item.sid, item.identity, item.videoTrack, /*item.muted*/false, item.mirror)
        roomViewPager.setCurrentItem(0, true)
    }

    private fun onStartVideo() {
        initializeVideoLayout()
        initializeRoom()
    }

    private fun initializeVideoLayout() {
        participantController = ParticipantController(thumbnailLinearLayout, primaryVideoView)
        participantController!!.setListener {
            // renderItemAsPrimary(item);
        }
    }

    private fun initializeRoom() {
        if (room != null) {
            localParticipant = room!!.localParticipant
            setupLocalMediaTrack()
            publishMediaTracks()
            addParticipantViews()
        }
    }

    /**
     * Initialize local media and provide stub participant for primary view.
     */
    private fun setupLocalMediaTrack() {
        if (localAudioTrack == null && !isAudioMuted) {
            localAudioTrack = LocalAudioTrack.create(requireActivity(), true, MICROPHONE_TRACK_NAME)
        }
        if (cameraVideoTrack == null && !isVideoMuted) {
            setupLocalVideoTrack()
            renderLocalParticipantStub()
        }
    }

    private fun publishMediaTracks() {
        if (localParticipant != null) {
            if (localAudioTrack != null) {
                localParticipant!!.publishTrack(localAudioTrack!!)
            }
            if (cameraVideoTrack != null) {
                localParticipant!!.publishTrack(cameraVideoTrack!!)
            }
        }
    }

    private fun addParticipantViews() {
        if (room != null && localParticipant != null) {
            localParticipantSid = localParticipant!!.sid

            // newly added for remove my local thumb if already there..
            participantController!!.removeThumbs(localParticipantSid)
            participantController!!.removeAllThumbs()
            // remove primary view
            participantController!!.removePrimary()

            var mirror = false
            if (cameraCapturer != null) {
                mirror = cameraCapturer!!.cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA
            }

            if (room!!.remoteParticipants.isEmpty()) {

                roomFragmentUtils.disableViewPager()

                participantController!!.thumbsViewContainer.visibility = View.GONE
                participantController!!.renderAsPrimary(localParticipantSid, "You", cameraVideoTrack, localAudioTrack == null, mirror)
            } else {

                roomFragmentUtils.enableViewPager()

                remoteParticipantsList = room!!.remoteParticipants
                participantController!!.addThumb(
                        localParticipantSid,
                        "You",
                        cameraVideoTrack,
                        localAudioTrack == null,
                        mirror)

                if (!isVideoMuted) {
                    participantController!!.thumbsViewContainer.visibility = View.VISIBLE
                }
                // add existing room participants thumbs
                var isFirstParticipant = true
                for (remoteParticipant in room!!.remoteParticipants) {
                    addParticipant(remoteParticipant, isFirstParticipant)
                    isFirstParticipant = false
                    if (room!!.dominantSpeaker != null) {
                        if (room!!.dominantSpeaker!!.sid == remoteParticipant.sid) {
                            val videoTrack: VideoTrack? = if (remoteParticipant.remoteVideoTracks.size > 0) remoteParticipant.remoteVideoTracks[0].remoteVideoTrack else null
                            if (videoTrack != null) {
                                val participantView = participantController!!.getThumb(remoteParticipant.sid, videoTrack)
                                participantController!!.setDominantSpeaker(participantView)
                            }
                        }
                    }
                }
            }

        }
    }

    private fun setupLocalVideoTrack() {
        // initialize capturer only once if needed
        if (cameraCapturer == null) {
            cameraCapturer = CameraCapturerCompat(requireActivity(), CameraCapturer.CameraSource.FRONT_CAMERA)
        }
        cameraVideoTrack = LocalVideoTrack.create(
                requireActivity(),
                true,
                cameraCapturer!!.videoCapturer,
                videoConstraints,
                CAMERA_TRACK_NAME)
    }

    private fun toggleAudioDevice(enableAudioDevice: Boolean) {
        setVolumeControl(enableAudioDevice)
        val viewEvent = if (enableAudioDevice) ActivateAudioDevice else DeactivateAudioDevice
        roomViewModel!!.processInput(viewEvent)
    }

    private fun toggleLocalAudio() {
        val icon: Int
        val text: String
        if (localAudioTrack == null) {
            AppPrefs.putBoolean(Constants.AUDIO_MODE_OFF, false)
            isAudioMuted = false
            localAudioTrack = LocalAudioTrack.create(requireActivity(), true, MICROPHONE_TRACK_NAME)
            if (localParticipant != null && localAudioTrack != null) {
                localParticipant!!.publishTrack(localAudioTrack!!)
            }
            icon = R.drawable.unmute
            text = getString(R.string.mute)
        } else {
            AppPrefs.putBoolean(Constants.AUDIO_MODE_OFF, true)
            isAudioMuted = true
            if (localParticipant != null) {
                localParticipant!!.unpublishTrack(localAudioTrack!!)
            }
            localAudioTrack!!.release()
            localAudioTrack = null
            icon = R.drawable.mute
            text = getString(R.string.unmute)
        }
        tvMute!!.text = text
        ivMute!!.setImageResource(icon)
    }

    private fun toggleLocalVideo() {
        if (cameraCapturer == null) {
            cameraCapturer = CameraCapturerCompat(requireActivity(), CameraCapturer.CameraSource.FRONT_CAMERA)
        }

        // remember old video reference for updating thumb in room
        val oldVideo: VideoTrack? = cameraVideoTrack
        if (cameraVideoTrack == null) {
            AppPrefs.putBoolean(Constants.VIDEO_MODE_OFF, false)
            isVideoMuted = false
            // add local camera track
            cameraVideoTrack = LocalVideoTrack.create(
                    requireActivity(),
                    true,
                    cameraCapturer!!.videoCapturer,
                    videoConstraints,
                    CAMERA_TRACK_NAME)
            if (localParticipant != null && cameraVideoTrack != null) {
                localParticipant!!.publishTrack(cameraVideoTrack!!)
            }
        } else {
            AppPrefs.putBoolean(Constants.VIDEO_MODE_OFF, true)
            isVideoMuted = true
            // remove local camera track
            cameraVideoTrack!!.removeRenderer(primaryVideoView!!)
            if (localParticipant != null) {
                localParticipant!!.unpublishTrack(cameraVideoTrack!!)
            }
            cameraVideoTrack!!.release()
            cameraVideoTrack = null
        }
        if (room != null && room!!.state == Room.State.CONNECTED) {

            // update local participant thumb
            participantController!!.updateThumb(localParticipantSid, oldVideo, cameraVideoTrack)
            if (participantController!!.primaryItem != null) {
                if (participantController!!.primaryItem.sid == localParticipantSid) {

                    // local video was rendered as primary view - refreshing
                    participantController!!.renderAsPrimary(
                            localParticipantSid,
                            getString(R.string.you),
                            cameraVideoTrack,
                            localAudioTrack == null, cameraCapturer!!.cameraSource
                            == CameraCapturer.CameraSource.FRONT_CAMERA)
                    participantController!!.primaryView.showIdentityBadge(false)

                    // update thumb state
                    participantController!!.updateThumb(localParticipantSid, cameraVideoTrack, ParticipantView.State.SELECTED)
                }
            }
        } else {
            renderLocalParticipantStub()
        }
        if (cameraVideoTrack != null) {
            tvVideoOn!!.text = getString(R.string.videoOff)
            ivVideoOn!!.setImageResource(R.drawable.video_on)
            ivSwitchCamera!!.visibility = View.VISIBLE
            if (remoteParticipantsList.isNotEmpty()) {
                participantController!!.thumbsViewContainer.visibility = View.VISIBLE
            }
        } else {
            tvVideoOn!!.text = getString(R.string.videoOn)
            ivVideoOn!!.setImageResource(R.drawable.video_off)
            ivSwitchCamera!!.visibility = View.GONE
            if (remoteParticipantsList.isNotEmpty()) {
                participantController!!.thumbsViewContainer.visibility = View.GONE
            }
        }
    }

    /**
     * Render local video track.
     *
     *
     * NOTE: Stub participant is created in controller. Make sure to remove it when connected to
     * room.
     */
    private fun renderLocalParticipantStub() {
        if (cameraCapturer == null) return
        participantController!!.renderAsPrimary(
                localParticipantSid,
                getString(R.string.you),
                cameraVideoTrack,
                localAudioTrack == null,
                cameraCapturer!!.cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA)
        primaryVideoView!!.showIdentityBadge(false)
    }

    private fun updateUi(room: Room?, roomEvent: RoomEvent) {
        var joinStatus = ""
        if (roomEvent is Connecting) {
            joinStatus = getString(R.string.joining)
        }
        if (room != null) {
            if (room.state == Room.State.CONNECTED) {
                joinStatus = ""
            }
        }
        joinStatusTextView!!.text = joinStatus
        if (isVideoMuted) {
            ivVideoOn!!.setImageResource(R.drawable.video_off)
            tvVideoOn!!.text = getString(R.string.videoOn)
            ivSwitchCamera!!.visibility = View.GONE
        } else {
            ivVideoOn!!.setImageResource(R.drawable.video_on)
            tvVideoOn!!.text = getString(R.string.videoOn)
            ivSwitchCamera!!.visibility = View.VISIBLE
        }
        if (isAudioMuted) {
            tvMute!!.text = getString(R.string.unmute)
            ivMute!!.setImageResource(R.drawable.mute)
        } else {
            tvMute!!.text = getString(R.string.mute)
            ivMute!!.setImageResource(R.drawable.unmute)
        }
        if (!isInternetAudioEnable) {
            ivUpArrow!!.visibility = View.GONE
            tvMute!!.text = getString(R.string.join_audio)
            ivMute!!.setImageResource(R.drawable.microphone)
            audioEnable(false)
        } else {
            ivUpArrow!!.visibility = View.VISIBLE
        }
    }

    private fun addParticipant(remoteParticipant: RemoteParticipant, renderAsPrimarys: Boolean) {
        var renderAsPrimary = renderAsPrimarys
        val muted = remoteParticipant.remoteAudioTracks.size <= 0 || !remoteParticipant.remoteAudioTracks[0].isTrackEnabled
        val remoteVideoTrackPublications = remoteParticipant.remoteVideoTracks
        if (remoteVideoTrackPublications.isEmpty()) {

                participantListener!!.controlParticipant(remoteParticipant, true)

            addParticipantVideoTrack(remoteParticipant, muted, null, renderAsPrimary)
        } else {
            for (remoteVideoTrackPublication in remoteVideoTrackPublications) {
                addParticipantVideoTrack(remoteParticipant, muted, remoteVideoTrackPublication.remoteVideoTrack, renderAsPrimary)
                renderAsPrimary = false
            }
        }
    }

    private fun addParticipantVideoTrack(remoteParticipant: RemoteParticipant, muted: Boolean, remoteVideoTrack: RemoteVideoTrack?, renderAsPrimary: Boolean) {

        val listener = RemoteParticipantListener()
        remoteParticipant.setListener(listener)

        if (renderAsPrimary) {
            participantController!!.renderAsPrimary(remoteParticipant.sid, remoteParticipant.identity, remoteVideoTrack, muted, false)
        }

    }

    private inner class RemoteParticipantListener : RemoteParticipant.Listener {

        override fun onAudioTrackSubscribed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, remoteAudioTrack: RemoteAudioTrack) {
            Timber.e(
                    "onAudioTrackSubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.identity,
                    remoteAudioTrackPublication.trackSid,
                    remoteAudioTrackPublication.isTrackEnabled,
                    remoteAudioTrackPublication.isTrackSubscribed)
            val newAudioState = !remoteAudioTrackPublication.isTrackEnabled
            if (participantController!!.primaryItem.sid == remoteParticipant.sid) {
                // update audio state for primary view
                participantController!!.primaryItem.muted = newAudioState
                participantController!!.primaryView.setMuted(newAudioState)
            }

            updateParticipantAudioStatus(remoteParticipant.sid, false)


        }

        override fun onAudioTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, remoteAudioTrack: RemoteAudioTrack) {
            Timber.e(
                    "onAudioTrackUnsubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.identity,
                    remoteAudioTrackPublication.trackSid,
                    remoteAudioTrackPublication.isTrackEnabled,
                    remoteAudioTrackPublication.isTrackSubscribed)
            if (participantController!!.primaryItem.sid == remoteParticipant.sid) {
                // update audio state for primary view
                participantController!!.primaryItem.muted = true
                participantController!!.primaryView.setMuted(true)
            }

            updateParticipantAudioStatus(remoteParticipant.sid, true)

        }

        override fun onVideoTrackSubscribed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, remoteVideoTrack: RemoteVideoTrack) {
            Timber.e(
                    "onVideoTrackSubscribed: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.identity,
                    remoteVideoTrackPublication.trackSid,
                    remoteVideoTrackPublication.isTrackEnabled,
                    remoteVideoTrackPublication.isTrackSubscribed)
            val primary = participantController!!.primaryItem
            if (primary != null && primary.sid == remoteParticipant.sid && primary.videoTrack == null) {
                // no thumb needed - render as primary
                primary.videoTrack = remoteVideoTrack
                participantController!!.renderAsPrimary(primary)
            }

            participantListener!!.controlParticipant(remoteParticipant, true)

            updateParticipantVideoStatus(remoteParticipant.sid, true)


        }

        override fun onVideoTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, remoteVideoTrack: RemoteVideoTrack) {
            Timber.e(
                    "onVideoTrackUnsubscribed: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.identity,
                    remoteVideoTrackPublication.trackSid,
                    remoteVideoTrackPublication.isTrackEnabled)
            val primary = participantController!!.primaryItem
            if (primary != null && primary.sid == remoteParticipant.sid && primary.videoTrack === remoteVideoTrack) {

                // Remove primary video track
                primary.videoTrack = null

                // Try to find another video track to render as primary
                val remoteVideoTracks = remoteParticipant.remoteVideoTracks
                for (newRemoteVideoTrackPublication in remoteVideoTracks) {
                    val newRemoteVideoTrack = newRemoteVideoTrackPublication.remoteVideoTrack
                    if (newRemoteVideoTrack !== remoteVideoTrack) {
                        participantController!!.removeThumb(remoteParticipant.sid, newRemoteVideoTrack)
                        primary.videoTrack = newRemoteVideoTrack
                        break
                    }
                }
                participantController!!.renderAsPrimary(primary)
            }

            updateParticipantVideoStatus(remoteParticipant.sid, false)
        }

        override fun onNetworkQualityLevelChanged(remoteParticipant: RemoteParticipant, networkQualityLevel: NetworkQualityLevel) {}
        override fun onAudioTrackPublished(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}
        override fun onAudioTrackUnpublished(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}
        override fun onVideoTrackPublished(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}
        override fun onVideoTrackUnpublished(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}
        override fun onAudioTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, twilioException: TwilioException) {}
        override fun onVideoTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, twilioException: TwilioException) {}
        override fun onDataTrackPublished(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication) {}
        override fun onDataTrackUnpublished(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication) {}
        override fun onDataTrackSubscribed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, remoteDataTrack: RemoteDataTrack) {}
        override fun onDataTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, twilioException: TwilioException) {}
        override fun onDataTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, remoteDataTrack: RemoteDataTrack) {}
        override fun onAudioTrackEnabled(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}
        override fun onAudioTrackDisabled(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}
        override fun onVideoTrackEnabled(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}
        override fun onVideoTrackDisabled(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}
    }

    private fun updateParticipantAudioStatus(sid: String, audioState: Boolean) {
        try {
            if (participantListAdapter == null) return
            if (participantListAdapter!!.list.isEmpty()) return
            participantListAdapter?.list?.find { it.sid == sid }?.muted = audioState
            participantListAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun updateParticipantVideoStatus(sid: String, videoState: Boolean) {
        try {
            if (participantListAdapter == null) return
            if (participantListAdapter!!.list.isEmpty()) return
            participantListAdapter?.list?.find { it.sid == sid }?.video = videoState
            participantListAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //toggleLocalVideoTrackState();

    //toggleLocalAudioTrackState();
    private val bottomLayout: Unit
        get() {
            ivMute!!.setOnClickListener { v: View? ->
                //toggleLocalAudioTrackState();
                if (tvMute!!.text.toString() == getString(R.string.join_audio)) {
                    if (isAudioMuted) {
                        tvMute!!.text = getString(R.string.unmute)
                        ivMute!!.setImageResource(R.drawable.mute)
                    } else {
                        tvMute!!.text = getString(R.string.mute)
                        ivMute!!.setImageResource(R.drawable.unmute)
                    }
                    audioEnable(true)
                    ivUpArrow!!.visibility = View.VISIBLE
                } else {
                    toggleLocalAudio()
                }
            }


            ivUpArrow!!.setOnClickListener { v: View? ->
                disconnectAudioBubbleLayout.visibility = View.VISIBLE
            }


            ivBubbleCross.setOnClickListener {
                disconnectAudioBubbleLayout.visibility = View.GONE
            }

            tvAudioDisconnect.setOnLongClickListener { v: View? ->
                showDisconnectAudioAlert()
                false
            }


            videoOnLayout!!.setOnClickListener { v: View? ->
                if (AppPrefs.getBoolean(Constants.SHOW_BOTTOM_PREVIEW)) {
                    RoomFragmentUtils(roomFragment!!, roomView!!).setupPreview()
                } else {
                    //toggleLocalVideoTrackState();
                    toggleLocalVideo()
                }
            }
            participantLayout!!.setOnClickListener { v: View? -> RoomFragmentUtils(roomFragment!!).showParticipantList() }
            encryptionView!!.setOnClickListener { v: View? -> RoomFragmentUtils(roomFragment!!).showEncryptionLayout() }
            ivSpeaker!!.tag = Constants.SPEAKER_PHONE
            ivSpeaker!!.setOnClickListener { v: View? ->
                val viewState = roomViewModel!!.audioViewState.value
                val selectedDevice = viewState!!.selectedDevice
                val audioDevices = viewState.availableAudioDevices
                if (selectedDevice != null && audioDevices != null) {
                    val index = audioDevices.indexOf(selectedDevice)
                    val audioDeviceNames = ArrayList<String>()
                    for (a in audioDevices) {
                        audioDeviceNames.add(a.name)
                    }
                    var i = 0
                    if (ivSpeaker!!.tag == Constants.EAR_PIECE) {
                        i = audioDeviceNames.indexOf(Constants.SPEAKER_PHONE)
                        ivSpeaker!!.tag = Constants.SPEAKER_PHONE
                        ivSpeaker!!.setImageResource(R.drawable.speaker_on)
                    } else {
                        i = audioDeviceNames.indexOf(Constants.EAR_PIECE)
                        ivSpeaker!!.tag = Constants.EAR_PIECE
                        ivSpeaker!!.setImageResource(R.drawable.speaker_off)
                    }
                    val viewEvent = SelectAudioDevice(audioDevices[i])
                    roomViewModel!!.processInput(viewEvent)
                }
            }
        }

    private fun audioEnable(b: Boolean) {
        isInternetAudioEnable = b
        val audioFlag = if (b) 1 else 0
        val ringerMode = if (b) AudioManager.RINGER_MODE_NORMAL else AudioManager.RINGER_MODE_SILENT
        val audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.mode = AudioManager.MODE_CURRENT/*MODE_IN_CALL*/
        audioManager.isMicrophoneMute = !b

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!audioManager.isVolumeFixed) {
                // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, audioFlag)
                // audioManager.ringerMode = ringerMode
                audioManager.adjustVolume(AudioManager.ADJUST_MUTE, audioFlag)
            } else {
                showToast(requireContext(), "Device is Not Supporting Volume Control")
            }
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, b)
        }
    }

    private fun showDisconnectAudioAlert() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
        builder.setTitle(getString(R.string.disconnect_audio_title))
        builder.setMessage(getString(R.string.disconnect_audio_message))
        builder.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface?, which: Int ->
            disconnectAudioBubbleLayout.visibility = View.GONE
            audioEnable(false)
            ivUpArrow!!.visibility = View.GONE
            tvMute!!.text = getString(R.string.join_audio)
            ivMute!!.setImageResource(R.drawable.microphone)
        }
        builder.setNegativeButton(getString(R.string.no), null)
        val alertDialog = builder.create()
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.show()
    }

    private fun setVolumeControl(setVolumeControl: Boolean) {
        if (setVolumeControl) {
            /*
             * Enable changing the volume using the up/down keys during a conversation
             */
            requireActivity().volumeControlStream = AudioManager.STREAM_VOICE_CALL
        } else {
            requireActivity().volumeControlStream = savedVolumeControlStream
        }
    }

    private val isNetworkQualityEnabled: Boolean
        get() = sharedPreferences!!.getBoolean(Preferences.ENABLE_NETWORK_QUALITY_LEVEL, Preferences.ENABLE_NETWORK_QUALITY_LEVEL_DEFAULT)

    private fun obtainVideoConstraints() {
        val builder = VideoConstraints.Builder()
        // setup aspect ratio
        val aspectRatio = sharedPreferences!!.getString(Preferences.ASPECT_RATIO, "0")
        val aspectRatioIndex = aspectRatio!!.toInt()
        builder.aspectRatio(Constants.aspectRatios[aspectRatioIndex])
        // setup video dimensions
        val minVideoDim = sharedPreferences!!.getInt(Preferences.MIN_VIDEO_DIMENSIONS, 0)
        val maxVideoDim = sharedPreferences!!.getInt(Preferences.MAX_VIDEO_DIMENSIONS, Constants.videoDimensions.size - 1)
        if (maxVideoDim != -1 && minVideoDim != -1) {
            builder.minVideoDimensions(Constants.videoDimensions[minVideoDim])
            builder.maxVideoDimensions(Constants.videoDimensions[maxVideoDim])
        }
        // setup fps
        val minFps = sharedPreferences!!.getInt(Preferences.MIN_FPS, 0)
        val maxFps = sharedPreferences!!.getInt(Preferences.MAX_FPS, 30)
        if (maxFps != -1 && minFps != -1) {
            builder.minFps(minFps)
            builder.maxFps(maxFps)
        }
        videoConstraints = builder.build()
    }

    private fun handleTokenError(error: AuthServiceError?) {
        val errorMessage = if (error === AuthServiceError.EXPIRED_PASSCODE_ERROR) R.string.room_screen_token_expired_message else R.string.room_screen_token_retrieval_failure_message
        Toast.makeText(requireActivity(), "" + errorMessage, Toast.LENGTH_SHORT).show()
        /* new AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.room_screen_connection_failure_title))
                .setMessage(getString(errorMessage))
                .setNeutralButton("OK", null)
                .show();*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_AUDIO_MUTED, isAudioMuted)
        outState.putBoolean(IS_VIDEO_MUTED, isVideoMuted)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        removeCameraTrack()
        removeAllParticipants()
        if (broadcastReceiver != null) {
            requireActivity().unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Teardown tracks
        if (localAudioTrack != null) {
            localAudioTrack!!.release()
            localAudioTrack = null
        }
        if (cameraVideoTrack != null) {
            cameraVideoTrack!!.release()
            cameraVideoTrack = null
        }
        if (screenVideoTrack != null) {
            screenVideoTrack!!.release()
            screenVideoTrack = null
        }
        participantListener = null
    }

    private fun onSuccessDisconnect(message: String) {
        removeCameraTrack()
        removeAllParticipants()
        clearRoomInstance()
        AppPrefs.clearAllPref()
        Constants.shouldMeetingRefresh = true
        MyRemoteParticipants.thumbs.clear()
        remoteParticipantsList.clear()
        requireActivity().finish()
        showToast(requireContext(), message)
    }

    private fun clearRoomInstance() {
        try {
            Constants.shouldBindRoom = false
            roomManager!!.disconnect()
            roomFragment!!.onDestroy()
            roomViewModel = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disconnectButtonClick() {
        joinStatusTextView!!.visibility = View.VISIBLE
        joinStatusTextView!!.setTextColor(Color.RED)
        joinStatusTextView!!.setText(R.string.disconnecting)
        roomViewModel!!.processInput(Disconnect)
    }

    private fun removeAllParticipants() {
        if (room != null) {
            if (participantController == null) return
            participantController!!.removeAllThumbs()
            participantController!!.removePrimary()
            renderLocalParticipantStub()
        }
    }

    private fun removeParticipant(remoteParticipant: RemoteParticipant) {
        remoteParticipantsList.remove(remoteParticipant)
        participantController!!.removeThumbs(remoteParticipant.sid)
        participantListener!!.controlParticipant(remoteParticipant, false)
    }

    private fun removeCameraTrack() {
        if (cameraVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant!!.unpublishTrack(cameraVideoTrack!!)
            }
            cameraVideoTrack!!.release()
            restoreLocalVideoCameraTrack = true
            cameraVideoTrack = null
        }
    }

    private fun restoreCameraTrack() {
        if (restoreLocalVideoCameraTrack) {
            obtainVideoConstraints()
            setupLocalVideoTrack()
            renderLocalParticipantStub()
            restoreLocalVideoCameraTrack = false
        }
    }

    companion object {
        private const val LOCAL_PARTICIPANT_STUB_SID = ""
        private const val MICROPHONE_TRACK_NAME = "microphone"
        private const val CAMERA_TRACK_NAME = "camera"
        private const val IS_AUDIO_MUTED = "IS_AUDIO_MUTED"
        private const val IS_VIDEO_MUTED = "IS_VIDEO_MUTED"
        var remoteParticipantsList = mutableListOf<RemoteParticipant>()
    }
}