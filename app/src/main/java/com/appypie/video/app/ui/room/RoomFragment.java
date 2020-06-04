/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appypie.video.app.ui.room;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.lifecycle.ViewModelProvider;

import com.appypie.video.app.R;
import com.appypie.video.app.ViewModelFactory;
import com.appypie.video.app.base.BaseFragment;
import com.appypie.video.app.data.Preferences;
import com.appypie.video.app.ui.home.VideoRoomInitializer;
import com.appypie.video.app.ui.room.RoomEvent.ConnectFailure;
import com.appypie.video.app.ui.room.RoomEvent.Connecting;
import com.appypie.video.app.ui.room.RoomEvent.DominantSpeakerChanged;
import com.appypie.video.app.ui.room.RoomEvent.ParticipantConnected;
import com.appypie.video.app.ui.room.RoomEvent.ParticipantDisconnected;
import com.appypie.video.app.ui.room.RoomEvent.RoomState;
import com.appypie.video.app.ui.room.RoomEvent.TokenError;
import com.appypie.video.app.ui.room.RoomViewEvent.ActivateAudioDevice;
import com.appypie.video.app.ui.room.RoomViewEvent.SelectAudioDevice;
import com.appypie.video.app.util.AppPrefs;
import com.appypie.video.app.util.CameraCapturerCompat;
import com.appypie.video.app.util.CommonMethod;
import com.appypie.video.app.util.Constants;
import com.appypie.video.app.util.ParticipantListener;
import com.appypie.video.app.webservices.AuthServiceError;
import com.twilio.audioswitch.selection.AudioDevice;
import com.twilio.audioswitch.selection.AudioDeviceSelector;
import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.NetworkQualityLevel;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.Room.State;
import com.twilio.video.TwilioException;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.appypie.video.app.util.Constants.APP_ID;
import static com.appypie.video.app.util.Constants.CURRENT_MEETING_ID;
import static com.appypie.video.app.util.Constants.CURRENT_USER_NAME;
import static com.appypie.video.app.util.Constants.END_MEETING_BROADCAST;
import static com.appypie.video.app.util.Constants.HOST_ID;
import static com.appypie.video.app.util.Constants.isInternetAudioEnable;
import static com.appypie.video.app.util.Constants.shouldBindRoom;
import static com.appypie.video.app.util.Constants.shouldMeetingRefresh;
import static com.appypie.video.app.webservices.AuthServiceError.EXPIRED_PASSCODE_ERROR;
import static com.twilio.video.Room.State.CONNECTED;

public class RoomFragment extends BaseFragment implements VideoRoomInitializer, MyRemoteParticipants.ParticipantClickListener {

    @BindView(R.id.videoOnLayout)
    LinearLayoutCompat videoOnLayout;

    @BindView(R.id.shareLayout)
    LinearLayoutCompat shareLayout;

    @BindView(R.id.participantLayout)
    LinearLayoutCompat participantLayout;

    @BindView(R.id.chatLayout)
    LinearLayoutCompat chatLayout;

    @BindView(R.id.tvMute)
    TextView tvMute;

    @BindView(R.id.ivMute)
    ImageView ivMute;

    @BindView(R.id.ivUpArrow)
    ImageView ivUpArrow;

    @BindView(R.id.tvVideoOn)
    TextView tvVideoOn;

    @BindView(R.id.ivVideoOn)
    ImageView ivVideoOn;

    @BindView(R.id.ivSpeaker)
    ImageView imgSpeaker;


    @BindView(R.id.roomView)
    RelativeLayout roomView;

    @BindView(R.id.encryptionView)
    LinearLayoutCompat encryptionView;

    @BindView(R.id.ivSwitchCamera)
    ImageView ivSwitchCamera;

    @BindView(R.id.pbEndMeeting)
    ProgressBar pbEndMeeting;

    @BindView(R.id.ivDisconnect)
    ImageView ivDisconnect;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    RoomManager roomManager;

    @Inject
    AudioDeviceSelector audioDeviceSelector;

    @Inject
    ViewModelFactory viewModelFactory;

    EndMeetingViewModel endMeetingViewModel;


    private boolean restoreLocalVideoCameraTrack = false;
    private LocalParticipant localParticipant;
    public String localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
    private static final String LOCAL_PARTICIPANT_STUB_SID = "";
    private ParticipantController participantController;
    private ParticipantPrimaryView primaryVideoView;
    private ViewGroup thumbnailLinearLayout;
    private TextView joinStatusTextView;

    public RoomFragment roomFragment;

    public RoomViewModel roomViewModel;

    private Boolean isAudioMuted = AppPrefs.getBoolean(Constants.AUDIO_MODE_OFF);

    private Boolean isVideoMuted = AppPrefs.getBoolean(Constants.VIDEO_MODE_OFF);

    private int savedVolumeControlStream;

    private static final String MICROPHONE_TRACK_NAME = "microphone";
    private static final String CAMERA_TRACK_NAME = "camera";
    private static final String IS_AUDIO_MUTED = "IS_AUDIO_MUTED";
    private static final String IS_VIDEO_MUTED = "IS_VIDEO_MUTED";

    private Room room;
    private VideoConstraints videoConstraints;
    private LocalAudioTrack localAudioTrack;
    public LocalVideoTrack cameraVideoTrack;
    private LocalVideoTrack screenVideoTrack;
    private CameraCapturerCompat cameraCapturer;


    private IntentFilter intentFilter = new IntentFilter(END_MEETING_BROADCAST);

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            // disconnectButtonClick();
            onSuccessDisconnect(getString(R.string.meeting_ended));

        }
    };


    @OnClick(R.id.ivDisconnect)
    void disConnectAlert() {

        ArrayList<String> arrayList = new ArrayList<>();
        if (Constants.APP_TYPE.equals(Constants.USER)) {
            arrayList.add(getString(R.string.end_meeting_for_all));
        } else {
            arrayList.add(getString(R.string.leave_meeting));
        }
        arrayList.add(getString(R.string.cancel));


        new RoomFragmentUtils(roomFragment).showListDialog(arrayList, pos -> {

            if (arrayList.get(pos).equals(getString(R.string.leave_meeting))) {
                endMeetingViewModel.leftMeeting(CommonMethod.Companion.getHeaderMap(), APP_ID, CURRENT_USER_NAME, CURRENT_MEETING_ID);
            }

            if (arrayList.get(pos).equals(getString(R.string.end_meeting_for_all))) {
                endMeetingViewModel.endMeeting(CommonMethod.Companion.getHeaderMap(), APP_ID, HOST_ID, CURRENT_MEETING_ID);
            }
        });

    }

    @OnClick(R.id.ivSwitchCamera)
    public void switchCamera() {
        if (cameraCapturer != null) {

            boolean mirror = cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.BACK_CAMERA;

            cameraCapturer.switchCamera();

            if (participantController.getPrimaryItem() != null) {
                if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {
                    participantController.updatePrimaryThumb(mirror);
                }
            } else {
                participantController.updateThumb(localParticipantSid, cameraVideoTrack, mirror);
            }
        }
    }

    public ParticipantListener participantListener;

    public void setParticipantListener(ParticipantListener participantListener) {
        this.participantListener = participantListener;
    }


    @Override
    protected int layoutRes() {
        return R.layout.activity_room;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        roomFragment = this;

        RoomViewModel.RoomViewModelFactory factory = new RoomViewModel.RoomViewModelFactory(roomManager, audioDeviceSelector);

        roomViewModel = new ViewModelProvider(this, factory).get(RoomViewModel.class);

        roomViewModel.getRoomEvents().observe(requireActivity(), this::bindRoomEvents);

        endMeetingViewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(EndMeetingViewModel.class);

        observeViewModels();

        if (savedInstanceState != null) {
            isAudioMuted = savedInstanceState.getBoolean(IS_AUDIO_MUTED);
            isVideoMuted = savedInstanceState.getBoolean(IS_VIDEO_MUTED);
        }


        // So calls can be answered when screen is locked
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        ButterKnife.bind(requireActivity());

        // Cache volume control stream
        savedVolumeControlStream = requireActivity().getVolumeControlStream();


        new RoomFragmentUtils(roomFragment, roomView).loadViewPager(this);


    }

    private void observeViewModels() {

        endMeetingViewModel.getResponse().observe(requireActivity(), response -> {
            if (response != null) {
                if (response.status == 200) {
                    disconnectButtonClick();
                } else {
                    CommonMethod.Companion.showToast(requireContext(), response.message);
                }
            }
        });

        endMeetingViewModel.getError().observe(requireActivity(), isError -> {
            if (isError) {
                CommonMethod.Companion.showToast(requireContext(), Constants.SERVER_ERROR);
            }
        });

        endMeetingViewModel.getIsloading().observe(requireActivity(), isLoading -> {
            if (isLoading) pbEndMeeting.setVisibility(View.VISIBLE);
            else pbEndMeeting.setVisibility(View.GONE);
            if (isLoading) ivDisconnect.setVisibility(View.GONE);
            else pbEndMeeting.setVisibility(View.VISIBLE);

        });

    }


    @Override
    public void onStart() {

        super.onStart();

        requireActivity().registerReceiver(broadcastReceiver, intentFilter);

        restoreCameraTrack();

        publishMediaTracks();

        addParticipantViews();

    }


    @Override
    public void onVideoLayoutInitialized(ParticipantPrimaryView primaryVideoView, ViewGroup thumbnailLinearLayout, TextView joinStatusTextView) {
        this.primaryVideoView = primaryVideoView;
        this.thumbnailLinearLayout = thumbnailLinearLayout;
        this.joinStatusTextView = joinStatusTextView;


        obtainVideoConstraints();

        getBottomLayout();

        if (AppPrefs.getBoolean(Constants.SHOW_BOTTOM_PREVIEW)) {
            new RoomFragmentUtils(roomFragment, roomView).setupPreview();
        } else {
            connectToRoom();
        }
    }

    private void bindRoomEvents(RoomEvent roomEvent) {

        if (!shouldBindRoom) return;


        if (roomEvent != null) {

            this.room = roomEvent.getRoom();

            if (room != null) {

                if (roomEvent instanceof RoomState) {
                    State state = room.getState();
                    switch (state) {
                        case CONNECTED:
                            onStartVideo();
                            toggleAudioDevice(isInternetAudioEnable);
                            break;
                        case DISCONNECTED:
                            localParticipant = null;
                            room.disconnect();
                            room = null;
                            removeAllParticipants();
                            localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
                            toggleAudioDevice(false);
                            isInternetAudioEnable = false;
                            onSuccessDisconnect("Disconnected");
                            break;
                    }
                }

                if (roomEvent instanceof ConnectFailure) {
                    new AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
                            .setTitle(getString(R.string.room_screen_connection_failure_title))
                            .setMessage(getString(R.string.room_screen_connection_failure_message))
                            .setNeutralButton("Retry", (dialog, which) -> connectToRoom())
                            .show();
                    removeAllParticipants();
                    toggleAudioDevice(false);
                    isInternetAudioEnable = false;
                }

                if (roomEvent instanceof ParticipantConnected) {
                    RemoteParticipant remoteParticipant = ((ParticipantConnected) roomEvent).getRemoteParticipant();
                    boolean renderAsPrimary = room.getRemoteParticipants().size() == 1;
                    addParticipant(remoteParticipant, renderAsPrimary);
                }


                if (roomEvent instanceof ParticipantDisconnected) {
                    RemoteParticipant remoteParticipant = ((ParticipantDisconnected) roomEvent).getRemoteParticipant();
                    removeParticipant(remoteParticipant);
                    addParticipantViews();
                }


                if (roomEvent instanceof DominantSpeakerChanged) {
                    RemoteParticipant remoteParticipant = ((DominantSpeakerChanged) roomEvent).getRemoteParticipant();
                    if (remoteParticipant == null) {
                        participantController.setDominantSpeaker(null);
                        return;
                    }

                    VideoTrack videoTrack = (remoteParticipant.getRemoteVideoTracks().size() > 0)
                            ? remoteParticipant
                            .getRemoteVideoTracks()
                            .get(0)
                            .getRemoteVideoTrack()
                            : null;

                    if (videoTrack != null) {
                        ParticipantView participantView = participantController.getThumb(remoteParticipant.getSid(), videoTrack);
                        if (participantView != null) {
                            participantController.setDominantSpeaker(participantView);
                        } else {
                            remoteParticipant.getIdentity();
                            ParticipantPrimaryView primaryParticipantView = participantController.getPrimaryView();
                            if (primaryParticipantView.identity.equals(remoteParticipant.getIdentity())) {
                                participantController.setDominantSpeaker(participantController.getPrimaryView());
                            } else {
                                participantController.setDominantSpeaker(null);
                            }
                        }
                    }
                }

            } else {
                if (roomEvent instanceof TokenError) {
                    AuthServiceError error = ((TokenError) roomEvent).getServiceError();
                    handleTokenError(error);
                }
            }

            updateUi(room, roomEvent);
        }
    }

    public void connectToRoom() {
        shouldBindRoom = true;
        RoomViewEvent.Connect viewEvent = new RoomViewEvent.Connect("", "", isNetworkQualityEnabled());
        roomViewModel.processInput(viewEvent);
    }


    @Override
    public void onGridClick(MyRemoteParticipants.Item item) {
        participantController.renderAsPrimary(item.sid, item.identity, item.videoTrack, item.muted, item.mirror);
    }


    private void onStartVideo() {
        initializeVideoLayout();
        initializeRoom();
    }

    private void initializeVideoLayout() {
        participantController = new ParticipantController(thumbnailLinearLayout, primaryVideoView);
        participantController.setListener(new ParticipantController.ItemClickListener() {
            @Override
            public void onThumbClick(ParticipantController.Item item) {
                // renderItemAsPrimary(item);
            }
        });
    }

    public void initializeRoom() {

        if (room != null) {

            localParticipant = room.getLocalParticipant();

            setupLocalMediaTrack();

            publishMediaTracks();

            addParticipantViews();
        }
    }

    /**
     * Initialize local media and provide stub participant for primary view.
     */
    private void setupLocalMediaTrack() {
        if (localAudioTrack == null && !isAudioMuted) {
            localAudioTrack = LocalAudioTrack.create(requireActivity(), true, MICROPHONE_TRACK_NAME);
        }
        if (cameraVideoTrack == null && !isVideoMuted) {
            setupLocalVideoTrack();
            renderLocalParticipantStub();
        }
    }


    private void publishMediaTracks() {
        if (localParticipant != null) {
            if (localAudioTrack != null) {
                localParticipant.publishTrack(localAudioTrack);
            }
            if (cameraVideoTrack != null) {
                localParticipant.publishTrack(cameraVideoTrack);
            }
        }
    }

    private void addParticipantViews() {

        if (room != null && localParticipant != null) {

            localParticipantSid = localParticipant.getSid();

            // newly added for remove my local thumb if already there..
            participantController.removeThumbs(localParticipantSid);


            // remove primary view
            participantController.removePrimary();


            boolean mirror = false;
            if (cameraCapturer != null) {
                mirror = cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA;
            }


            // add local thumb and "click" on it to make primary
            participantController.addThumb(
                    localParticipantSid,
                    "You",
                    cameraVideoTrack,
                    localAudioTrack == null,
                    mirror);

            participantController.getThumb(localParticipantSid, cameraVideoTrack).callOnClick();

            // add existing room participants thumbs
            boolean isFirstParticipant = true;
            for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {

                addParticipant(remoteParticipant, isFirstParticipant);

                isFirstParticipant = false;

                if (room.getDominantSpeaker() != null) {
                    if (room.getDominantSpeaker().getSid().equals(remoteParticipant.getSid())) {
                        VideoTrack videoTrack = (remoteParticipant.getRemoteVideoTracks().size() > 0) ? remoteParticipant.getRemoteVideoTracks().get(0).getRemoteVideoTrack() : null;
                        if (videoTrack != null) {
                            ParticipantView participantView = participantController.getThumb(remoteParticipant.getSid(), videoTrack);
                            participantController.setDominantSpeaker(participantView);
                        }
                    }
                }
            }
        }
    }

    private void setupLocalVideoTrack() {
        // initialize capturer only once if needed
        if (cameraCapturer == null) {
            cameraCapturer = new CameraCapturerCompat(requireActivity(), CameraCapturer.CameraSource.FRONT_CAMERA);
        }
        cameraVideoTrack = LocalVideoTrack.create(
                requireActivity(),
                true,
                cameraCapturer.getVideoCapturer(),
                videoConstraints,
                CAMERA_TRACK_NAME);

    }

    private void toggleAudioDevice(boolean enableAudioDevice) {
        setVolumeControl(enableAudioDevice);
        RoomViewEvent viewEvent = enableAudioDevice ? ActivateAudioDevice.INSTANCE : RoomViewEvent.DeactivateAudioDevice.INSTANCE;
        roomViewModel.processInput(viewEvent);
    }

    private void toggleLocalAudio() {
        int icon;
        String text;
        if (localAudioTrack == null) {
            AppPrefs.putBoolean(Constants.AUDIO_MODE_OFF, false);
            isAudioMuted = false;
            localAudioTrack = LocalAudioTrack.create(requireActivity(), true, MICROPHONE_TRACK_NAME);
            if (localParticipant != null && localAudioTrack != null) {
                localParticipant.publishTrack(localAudioTrack);
            }
            icon = R.drawable.unmute;
            text = getString(R.string.mute);
        } else {
            AppPrefs.putBoolean(Constants.AUDIO_MODE_OFF, true);
            isAudioMuted = true;
            if (localParticipant != null) {
                localParticipant.unpublishTrack(localAudioTrack);
            }
            localAudioTrack.release();
            localAudioTrack = null;
            icon = R.drawable.mute;
            text = getString(R.string.unmute);
        }

        tvMute.setText(text);
        ivMute.setImageResource(icon);


    }


    private void toggleLocalVideo() {

        if (cameraCapturer == null) {
            cameraCapturer = new CameraCapturerCompat(requireActivity(), CameraCapturer.CameraSource.FRONT_CAMERA);
        }

        // remember old video reference for updating thumb in room
        VideoTrack oldVideo = cameraVideoTrack;

        if (cameraVideoTrack == null) {
            AppPrefs.putBoolean(Constants.VIDEO_MODE_OFF, false);
            isVideoMuted = false;
            // add local camera track
            cameraVideoTrack =
                    LocalVideoTrack.create(
                            requireActivity(),
                            true,
                            cameraCapturer.getVideoCapturer(),
                            videoConstraints,
                            CAMERA_TRACK_NAME);
            if (localParticipant != null && cameraVideoTrack != null) {
                localParticipant.publishTrack(cameraVideoTrack);
            }
        } else {
            AppPrefs.putBoolean(Constants.VIDEO_MODE_OFF, true);
            isVideoMuted = true;
            // remove local camera track
            cameraVideoTrack.removeRenderer(primaryVideoView);

            if (localParticipant != null) {
                localParticipant.unpublishTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            cameraVideoTrack = null;
        }

        if (room != null && room.getState() == CONNECTED) {

            // update local participant thumb
            participantController.updateThumb(localParticipantSid, oldVideo, cameraVideoTrack);


            if (participantController.getPrimaryItem() != null) {

                if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {

                    // local video was rendered as primary view - refreshing
                    participantController.renderAsPrimary(
                            localParticipantSid,
                            getString(R.string.you),
                            cameraVideoTrack,
                            localAudioTrack == null,
                            cameraCapturer.getCameraSource()
                                    == CameraCapturer.CameraSource.FRONT_CAMERA);

                    participantController.getPrimaryView().showIdentityBadge(false);

                    // update thumb state
                    participantController.updateThumb(localParticipantSid, cameraVideoTrack, ParticipantView.State.SELECTED);
                }
            }

        } else {
            renderLocalParticipantStub();
        }

        if (cameraVideoTrack != null) {
            tvVideoOn.setText(getString(R.string.videoOff));
            ivVideoOn.setImageResource(R.drawable.video_on);
            ivSwitchCamera.setVisibility(View.VISIBLE);
            participantController.thumbsViewContainer.setVisibility(View.VISIBLE);
        } else {
            tvVideoOn.setText(getString(R.string.videoOn));
            ivVideoOn.setImageResource(R.drawable.video_off);
            ivSwitchCamera.setVisibility(View.GONE);
            participantController.thumbsViewContainer.setVisibility(View.GONE);
        }
    }


    /**
     * Render local video track.
     *
     * <p>NOTE: Stub participant is created in controller. Make sure to remove it when connected to
     * room.
     */
    private void renderLocalParticipantStub() {

        if (cameraCapturer == null) return;
        participantController.renderAsPrimary(
                localParticipantSid,
                getString(R.string.you),
                cameraVideoTrack,
                localAudioTrack == null,
                cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

        primaryVideoView.showIdentityBadge(false);
    }

    private void updateUi(Room room, RoomEvent roomEvent) {

        String joinStatus = "";
        if (roomEvent instanceof Connecting) {
            joinStatus = getString(R.string.joining);
        }

        if (room != null) {
            if (room.getState() == CONNECTED) {
                joinStatus = "";
            }
        }
        joinStatusTextView.setText(joinStatus);

        if (isVideoMuted) {
            ivVideoOn.setImageResource(R.drawable.video_off);
            tvVideoOn.setText(getString(R.string.videoOn));
            ivSwitchCamera.setVisibility(View.GONE);

            if (participantController != null)
                participantController.thumbsViewContainer.setVisibility(View.GONE);

        } else {
            ivVideoOn.setImageResource(R.drawable.video_on);
            tvVideoOn.setText(getString(R.string.videoOn));
            ivSwitchCamera.setVisibility(View.VISIBLE);
            if (participantController != null)
                participantController.thumbsViewContainer.setVisibility(View.VISIBLE);
        }


        if (isAudioMuted) {
            tvMute.setText(getString(R.string.unmute));
            ivMute.setImageResource(R.drawable.mute);
        } else {
            tvMute.setText(getString(R.string.mute));
            ivMute.setImageResource(R.drawable.unmute);
        }

        if (!isInternetAudioEnable) {
            ivUpArrow.setVisibility(View.GONE);
            tvMute.setText(getString(R.string.join_audio));
            ivMute.setImageResource(R.drawable.microphone);
        } else {
            ivUpArrow.setVisibility(View.VISIBLE);
        }

    }


    private void addParticipant(RemoteParticipant remoteParticipant, boolean renderAsPrimary) {

        boolean muted = remoteParticipant.getRemoteAudioTracks().size() <= 0 || !remoteParticipant.getRemoteAudioTracks().get(0).isTrackEnabled();

        List<RemoteVideoTrackPublication> remoteVideoTrackPublications = remoteParticipant.getRemoteVideoTracks();

        if (remoteVideoTrackPublications.isEmpty()) {
            /*
             * Add placeholder UI by passing null video track for a participant that is not
             * sharing any video tracks.
             */
            participantListener.controlParticipant(remoteParticipant, true);
            addParticipantVideoTrack(remoteParticipant, muted, null, renderAsPrimary);

        } else {
            for (RemoteVideoTrackPublication remoteVideoTrackPublication : remoteVideoTrackPublications) {
                addParticipantVideoTrack(remoteParticipant, muted, remoteVideoTrackPublication.getRemoteVideoTrack(), renderAsPrimary);
                renderAsPrimary = false;
            }
        }
    }

    private void addParticipantVideoTrack(RemoteParticipant remoteParticipant, boolean muted, RemoteVideoTrack remoteVideoTrack, boolean renderAsPrimary) {

        if (renderAsPrimary) {
            participantController.renderAsPrimary(remoteParticipant.getSid(), remoteParticipant.getIdentity(), remoteVideoTrack, muted, false);
        }
        RemoteParticipantListener listener = new RemoteParticipantListener();
        remoteParticipant.setListener(listener);
    }


    private class RemoteParticipantListener implements RemoteParticipant.Listener {

        @Override
        public void onAudioTrackSubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NonNull RemoteAudioTrack remoteAudioTrack) {

            Timber.e(
                    "onAudioTrackSubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());
            boolean newAudioState = !remoteAudioTrackPublication.isTrackEnabled();

            if (participantController.getPrimaryItem().sid.equals(remoteParticipant.getSid())) {
                // update audio state for primary view
                participantController.getPrimaryItem().muted = newAudioState;
                participantController.getPrimaryView().setMuted(newAudioState);

            } else {

                // update thumbs with audio state
                participantController.updateThumbs(remoteParticipant.getSid(), newAudioState);
            }
        }


        @Override
        public void onAudioTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NonNull RemoteAudioTrack remoteAudioTrack) {
            Timber.e(
                    "onAudioTrackUnsubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());

            if (participantController.getPrimaryItem().sid.equals(remoteParticipant.getSid())) {
                // update audio state for primary view
                participantController.getPrimaryItem().muted = true;
                participantController.getPrimaryView().setMuted(true);

            } else {
                // update thumbs with audio state
                participantController.updateThumbs(remoteParticipant.getSid(), true);
            }
        }

        @Override
        public void onVideoTrackSubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication, @NonNull RemoteVideoTrack remoteVideoTrack) {
            Timber.e(
                    "onVideoTrackSubscribed: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled(),
                    remoteVideoTrackPublication.isTrackSubscribed());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null && primary.sid.equals(remoteParticipant.getSid()) && primary.videoTrack == null) {
                // no thumb needed - render as primary
                primary.videoTrack = remoteVideoTrack;
                participantController.renderAsPrimary(primary);
            } else {
                // not a primary remoteParticipant requires thumb
                participantController.addOrUpdateThumb(remoteParticipant.getSid(), remoteParticipant.getIdentity(), null, remoteVideoTrack);
            }
            participantListener.controlParticipant(remoteParticipant, true);
        }

        @Override
        public void onVideoTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication, @NonNull RemoteVideoTrack remoteVideoTrack) {
            Timber.e(
                    "onVideoTrackUnsubscribed: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null && primary.sid.equals(remoteParticipant.getSid()) && primary.videoTrack == remoteVideoTrack) {

                // Remove primary video track
                primary.videoTrack = null;

                // Try to find another video track to render as primary
                List<RemoteVideoTrackPublication> remoteVideoTracks = remoteParticipant.getRemoteVideoTracks();
                for (RemoteVideoTrackPublication newRemoteVideoTrackPublication : remoteVideoTracks) {
                    RemoteVideoTrack newRemoteVideoTrack = newRemoteVideoTrackPublication.getRemoteVideoTrack();
                    if (newRemoteVideoTrack != remoteVideoTrack) {
                        participantController.removeThumb(remoteParticipant.getSid(), newRemoteVideoTrack);
                        primary.videoTrack = newRemoteVideoTrack;
                        break;
                    }
                }
                participantController.renderAsPrimary(primary);
            } else {
                // remove thumb or leave empty video thumb
                participantController.removeOrEmptyThumb(remoteParticipant.getSid(), remoteParticipant.getIdentity(), remoteVideoTrack);

            }

        }

        @Override
        public void onNetworkQualityLevelChanged(@NonNull RemoteParticipant remoteParticipant, @NonNull NetworkQualityLevel networkQualityLevel) {
        }

        @Override
        public void onAudioTrackPublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
        }

        @Override
        public void onAudioTrackUnpublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
        }

        @Override
        public void onVideoTrackPublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
        }

        @Override
        public void onVideoTrackUnpublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {

        }

        @Override
        public void onAudioTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NonNull TwilioException twilioException) {
        }

        @Override
        public void onVideoTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication, @NonNull TwilioException twilioException) {
        }

        @Override
        public void onDataTrackPublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
        }

        @Override
        public void onDataTrackUnpublished(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
        }

        @Override
        public void onDataTrackSubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication, @NonNull RemoteDataTrack remoteDataTrack) {
        }

        @Override
        public void onDataTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication, @NonNull TwilioException twilioException) {
        }

        @Override
        public void onDataTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteDataTrackPublication remoteDataTrackPublication, @NonNull RemoteDataTrack remoteDataTrack) {
        }

        @Override
        public void onAudioTrackEnabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
        }

        @Override
        public void onAudioTrackDisabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
        }

        @Override
        public void onVideoTrackEnabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
        }

        @Override
        public void onVideoTrackDisabled(@NonNull RemoteParticipant remoteParticipant, @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
        }
    }


    private void getBottomLayout() {

        ivMute.setOnClickListener(v -> {
            //toggleLocalAudioTrackState();

            if (tvMute.getText().toString().equals(getString(R.string.join_audio))) {
                if (isAudioMuted) {
                    tvMute.setText(getString(R.string.unmute));
                    ivMute.setImageResource(R.drawable.mute);
                } else {
                    tvMute.setText(getString(R.string.mute));
                    ivMute.setImageResource(R.drawable.unmute);
                }
                isInternetAudioEnable = true;
                toggleAudioDevice(true);
                ivUpArrow.setVisibility(View.VISIBLE);

            } else {
                toggleLocalAudio();
            }
        });

        ivUpArrow.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "ivUpArrow", Toast.LENGTH_SHORT).show();

        });

        videoOnLayout.setOnClickListener(v -> {
            if (AppPrefs.getBoolean(Constants.SHOW_BOTTOM_PREVIEW)) {
                new RoomFragmentUtils(roomFragment, roomView).setupPreview();
            } else {
                //toggleLocalVideoTrackState();
                toggleLocalVideo();
            }
        });


        participantLayout.setOnClickListener(v -> new RoomFragmentUtils(roomFragment).showParticipantList());

        encryptionView.setOnClickListener(v -> new RoomFragmentUtils(roomFragment).showEncryptionLayout());


        imgSpeaker.setTag(Constants.SPEAKER_PHONE);
        imgSpeaker.setOnClickListener(v -> {

            AudioViewState viewState = roomViewModel.getAudioViewState().getValue();
            AudioDevice selectedDevice = viewState.getSelectedDevice();
            List<AudioDevice> audioDevices = viewState.getAvailableAudioDevices();

            if (selectedDevice != null && audioDevices != null) {
                int index = audioDevices.indexOf(selectedDevice);

                ArrayList<String> audioDeviceNames = new ArrayList<>();
                for (AudioDevice a : audioDevices) {
                    audioDeviceNames.add(a.getName());
                }

                int i = 0;
                if (imgSpeaker.getTag().equals(Constants.EAR_PIECE)) {
                    i = audioDeviceNames.indexOf(Constants.SPEAKER_PHONE);
                    imgSpeaker.setTag(Constants.SPEAKER_PHONE);
                    imgSpeaker.setImageResource(R.drawable.speaker_on);
                } else {
                    i = audioDeviceNames.indexOf(Constants.EAR_PIECE);
                    imgSpeaker.setTag(Constants.EAR_PIECE);
                    imgSpeaker.setImageResource(R.drawable.speaker_off);
                }

                SelectAudioDevice viewEvent = new SelectAudioDevice(audioDevices.get(i));
                roomViewModel.processInput(viewEvent);
            }


        });

    }


    private void setVolumeControl(boolean setVolumeControl) {
        if (setVolumeControl) {
            /*
             * Enable changing the volume using the up/down keys during a conversation
             */
            requireActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        } else {
            requireActivity().setVolumeControlStream(savedVolumeControlStream);
        }
    }

    private boolean isNetworkQualityEnabled() {
        return sharedPreferences.getBoolean(Preferences.ENABLE_NETWORK_QUALITY_LEVEL, Preferences.ENABLE_NETWORK_QUALITY_LEVEL_DEFAULT);
    }

    private void obtainVideoConstraints() {
        VideoConstraints.Builder builder = new VideoConstraints.Builder();
        // setup aspect ratio
        String aspectRatio = sharedPreferences.getString(Preferences.ASPECT_RATIO, "0");
        int aspectRatioIndex = Integer.parseInt(aspectRatio);
        builder.aspectRatio(Constants.aspectRatios[aspectRatioIndex]);
        // setup video dimensions
        int minVideoDim = sharedPreferences.getInt(Preferences.MIN_VIDEO_DIMENSIONS, 0);
        int maxVideoDim = sharedPreferences.getInt(Preferences.MAX_VIDEO_DIMENSIONS, Constants.videoDimensions.length - 1);
        if (maxVideoDim != -1 && minVideoDim != -1) {
            builder.minVideoDimensions(Constants.videoDimensions[minVideoDim]);
            builder.maxVideoDimensions(Constants.videoDimensions[maxVideoDim]);
        }
        // setup fps
        int minFps = sharedPreferences.getInt(Preferences.MIN_FPS, 0);
        int maxFps = sharedPreferences.getInt(Preferences.MAX_FPS, 30);
        if (maxFps != -1 && minFps != -1) {
            builder.minFps(minFps);
            builder.maxFps(maxFps);
        }
        videoConstraints = builder.build();
    }

    private void handleTokenError(AuthServiceError error) {
        int errorMessage = error == EXPIRED_PASSCODE_ERROR
                ? R.string.room_screen_token_expired_message
                : R.string.room_screen_token_retrieval_failure_message;

        Toast.makeText(requireActivity(), "" + errorMessage, Toast.LENGTH_SHORT).show();
        /* new AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.room_screen_connection_failure_title))
                .setMessage(getString(errorMessage))
                .setNeutralButton("OK", null)
                .show();*/
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(IS_AUDIO_MUTED, isAudioMuted);
        outState.putBoolean(IS_VIDEO_MUTED, isVideoMuted);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();

        removeCameraTrack();

        removeAllParticipants();

        if (broadcastReceiver != null) {
            requireActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Teardown tracks
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (cameraVideoTrack != null) {
            cameraVideoTrack.release();
            cameraVideoTrack = null;
        }
        if (screenVideoTrack != null) {
            screenVideoTrack.release();
            screenVideoTrack = null;
        }
        participantListener = null;
    }

    private void onSuccessDisconnect(String message) {
        removeCameraTrack();
        removeAllParticipants();
        clearRoomInstance();
        AppPrefs.clearAllPref();
        shouldMeetingRefresh = true;
        requireActivity().finish();
        CommonMethod.Companion.showToast(requireContext(), message);
    }

    public void clearRoomInstance() {
        try {
            shouldBindRoom = false;
            roomManager.disconnect();
            roomFragment.onDestroy();
            roomViewModel = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnectButtonClick() {
        joinStatusTextView.setVisibility(View.VISIBLE);
        joinStatusTextView.setTextColor(Color.RED);
        joinStatusTextView.setText(R.string.disconnecting);
        roomViewModel.processInput(RoomViewEvent.Disconnect.INSTANCE);
    }


    private void removeAllParticipants() {
        if (room != null) {
            if (participantController == null) return;
            participantController.removeAllThumbs();
            participantController.removePrimary();
            renderLocalParticipantStub();
        }
    }


    private void removeParticipant(RemoteParticipant remoteParticipant) {
        if (participantController.getPrimaryItem().sid.equals(remoteParticipant.getSid())) {
            // render local video if primary remoteParticipant has gone
            participantController.getThumb(localParticipantSid, cameraVideoTrack).callOnClick();
        }
        participantController.removeThumbs(remoteParticipant.getSid());
        participantListener.controlParticipant(remoteParticipant, false);
    }


    private void removeCameraTrack() {

        if (cameraVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant.unpublishTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            restoreLocalVideoCameraTrack = true;
            cameraVideoTrack = null;
        }
    }

    private void restoreCameraTrack() {
        if (restoreLocalVideoCameraTrack) {
            obtainVideoConstraints();
            setupLocalVideoTrack();
            renderLocalParticipantStub();
            restoreLocalVideoCameraTrack = false;
        }
    }
}
