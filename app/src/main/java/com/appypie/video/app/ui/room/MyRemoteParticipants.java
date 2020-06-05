package com.appypie.video.app.ui.room;

import android.view.ViewGroup;

import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.VideoTrack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyRemoteParticipants {


    private Item primaryItem;

    /**
     * Each participant thumb click listener.
     */
    private ParticipantClickListener listener;

    public static Map<Item, ParticipantView> thumbs = new HashMap<>();
    private ViewGroup thumbsViewContainer;


    MyRemoteParticipants(ViewGroup thumbsViewContainer, ParticipantClickListener listener) {
        this.thumbsViewContainer = thumbsViewContainer;
        this.listener = listener;
    }


    public void addOrUpdate(@NotNull RemoteParticipant remoteParticipant) {


        boolean muted = remoteParticipant.getRemoteAudioTracks().size() <= 0 || !remoteParticipant.getRemoteAudioTracks().get(0).isTrackEnabled();

        List<RemoteVideoTrackPublication> remoteVideoTrackPublications = remoteParticipant.getRemoteVideoTracks();


        RemoteVideoTrack videoTrack = null;

        if (!remoteVideoTrackPublications.isEmpty()) {
            videoTrack = remoteVideoTrackPublications.get(0).getRemoteVideoTrack();
        }

        addOrUpdateThumb(remoteParticipant.getSid(), remoteParticipant.getIdentity(), videoTrack, muted);

    }


    public void removeOrEmpty(@NotNull RemoteParticipant remoteParticipant) {


        List<RemoteVideoTrackPublication> remoteVideoTrackPublications = remoteParticipant.getRemoteVideoTracks();

        RemoteVideoTrack videoTrack = null;

        if (!remoteVideoTrackPublications.isEmpty()) {
            videoTrack = remoteVideoTrackPublications.get(0).getRemoteVideoTrack();
        }


        removeThumb(remoteParticipant.getSid(), videoTrack);

    }


    private void addOrUpdateThumb(String sid, String identity, VideoTrack newVideo, boolean muted) {

        if (hasThumb(sid, null)) {
            updateThumb(sid, newVideo);
        } else {
            boolean videoStatus = newVideo != null;
            addThumb(sid, identity, newVideo, muted, videoStatus);
        }
    }


    private boolean hasThumb(String sid, VideoTrack videoTrack) {
        return getThumb(sid, videoTrack) != null;
    }


    private ParticipantView getThumb(String sid, VideoTrack videoTrack) {
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey() != null && entry.getKey().sid.equals(sid) /*&& entry.getKey().videoTrack == videoTrack*/) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void addThumb(String sid, String identity, VideoTrack videoTrack, boolean muted, boolean mirror) {
        Item item = new Item(sid, identity, videoTrack, muted, mirror);
        ParticipantView view = createThumb(item);
        thumbs.put(item, view);
        thumbsViewContainer.addView(view);
    }


    void updateThumb(String sid, VideoTrack newVideo) {
        Item target = findItem(sid);
        if (target != null) {
            ParticipantView view = getThumb(sid, null);

            removeRender(target.videoTrack, view);

            target.videoTrack = newVideo;

            if (target.videoTrack != null) {
                view.setState(ParticipantView.State.VIDEO);
                target.videoTrack.addRenderer(view);
            } else {
                view.setState(ParticipantView.State.NO_VIDEO);
            }
        }
    }


    public static class Item {

        /**
         * RemoteParticipant unique identifier.
         */
        public String sid = "";

        /**
         * RemoteParticipant name.
         */
        String identity;

        /**
         * RemoteParticipant video track.
         */
        VideoTrack videoTrack;

        /**
         * RemoteParticipant audio state.
         */
        boolean muted;

        /**
         * Video track mirroring enabled/disabled.
         */
        boolean mirror;

        Item(String sid, String identity, VideoTrack videoTrack, boolean muted, boolean mirror) {

            this.sid = sid;
            this.identity = identity;
            this.videoTrack = videoTrack;
            this.muted = muted;
            this.mirror = mirror;
        }
    }


    private Item findItem(String sid) {
        for (Item item : thumbs.keySet()) {
            if (item.sid.equals(sid)) {
                return item;
            }
        }
        return null;
    }


    private void removeRender(VideoTrack videoTrack, ParticipantView view) {
        if (videoTrack == null || !videoTrack.getRenderers().contains(view)) return;
        videoTrack.removeRenderer(view);
    }


    private ParticipantView createThumb(final Item item) {

        final ParticipantView view = new ParticipantThumbView(thumbsViewContainer.getContext());

        view.setIdentity(item.identity);
        view.setMuted(item.muted);
        view.setMirror(item.mirror);

        view.setOnClickListener(
                participantView -> {
                    if (listener != null) {
                        listener.onGridClick(item);
                    }
                });

        if (item.videoTrack != null) {
            item.videoTrack.addRenderer(view);
            view.setState(ParticipantView.State.VIDEO);
        } else {
            view.setState(ParticipantView.State.NO_VIDEO);
        }

        return view;
    }


    private ArrayList<ParticipantView> getThumbs(String sid) {
        ArrayList<ParticipantView> views = new ArrayList<>();
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey().sid.equals(sid)) {
                views.add(entry.getValue());
            }
        }
        return views;
    }

    private void removeThumb(String sid, VideoTrack videoTrack) {
        Item target = findItem(sid);
        if (target != null) {
            ParticipantView view = getThumb(sid, videoTrack);

            removeRender(target.videoTrack, view);

            thumbsViewContainer.removeView(view);
            thumbs.remove(target);
        }
    }


    public interface ParticipantClickListener {
        void onGridClick(Item item);
    }

}
