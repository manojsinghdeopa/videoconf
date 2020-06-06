package com.appypie.video.app.ui.room

import android.app.Dialog
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.appypie.video.app.R
import com.appypie.video.app.ui.home.VideoRoomFragment
import com.appypie.video.app.ui.home.VideoRoomInitializer
import com.appypie.video.app.util.*
import com.appypie.video.app.util.Constants.meetingData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.twilio.video.CameraCapturer
import com.twilio.video.LocalVideoTrack
import com.twilio.video.VideoConstraints
import kotlinx.android.synthetic.main.activity_room.view.*
import kotlinx.android.synthetic.main.common_header_layout.*
import kotlinx.android.synthetic.main.encryption_layout.view.*
import kotlinx.android.synthetic.main.list_participant_layout.*
import kotlinx.android.synthetic.main.room_fragment_bottom.view.*
import kotlinx.android.synthetic.main.room_viewpager_layout.view.*
import kotlinx.android.synthetic.main.video_preview_bottom.view.*
import java.util.*


internal class RoomFragmentUtils() {

    lateinit var activity: RoomFragment
    lateinit var roomView: RelativeLayout

    constructor(activity: RoomFragment, roomView: RelativeLayout) : this() {
        this.activity = activity
        this.roomView = roomView
    }

    constructor(activity: RoomFragment) : this() {
        this.activity = activity
    }


    private fun isEnabled(fl: Float, b: Boolean) {

        if (b) {
            roomView.ivSwitchCamera.visibility = View.VISIBLE
        } else {
            roomView.ivSwitchCamera.visibility = View.GONE
        }

        roomView.muteLayout.alpha = fl
        roomView.shareLayout.alpha = fl
        roomView.participantLayout.alpha = fl
        roomView.chatLayout.alpha = fl

        roomView.videoOnLayout.isClickable = b

        roomView.muteLayout.isClickable = b
        roomView.ivMute.isClickable = b
        roomView.ivUpArrow.isClickable = b
        roomView.shareLayout.isClickable = b
        roomView.participantLayout.isClickable = b
        roomView.chatLayout.isClickable = b


    }


    fun setupPreview() {

        roomView.preViewLayout.visibility = View.VISIBLE

        isEnabled(.5f, false)

        val participantController = ParticipantController(roomView.preViewThumbBottom, roomView.preViewBottom)
        val cameraCapturer = CameraCapturerCompat(activity.activity, CameraCapturer.CameraSource.FRONT_CAMERA)
        val videoConstraints = VideoConstraints.Builder().build()

        val cameraVideoTrack = LocalVideoTrack.create(
                activity.requireContext(),
                true,
                cameraCapturer.videoCapturer,
                videoConstraints,
                "camera")

        participantController.renderAsPrimary(
                "",
                activity.getString(R.string.you),
                cameraVideoTrack,
                true,
                cameraCapturer.cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA)

        roomView.preViewBottom.showIdentityBadge(false)



        roomView.ivSwitchCameraPreview.setOnClickListener {
            val mirror = cameraCapturer.cameraSource == CameraCapturer.CameraSource.BACK_CAMERA
            cameraCapturer.switchCamera()
            if (participantController.primaryItem.sid == "") {
                participantController.updatePrimaryThumb(mirror)
            } else {
                participantController.updateThumb("", cameraVideoTrack, mirror)
            }
        }


        roomView.ivCross.setOnClickListener {
            roomView.preViewLayout.visibility = View.GONE
            roomView.videoOnLayout.isClickable = true
        }

        roomView.btnStartMyVideo.setOnClickListener {
            try {
                AppPrefs.putBoolean(Constants.SHOW_BOTTOM_PREVIEW, false)
                roomView.preViewLayout.removeAllViews()
                roomView.preViewLayout.visibility = View.GONE
                activity.connectToRoom()
                isEnabled(1f, true)

                participantController.removeAllThumbs()
                participantController.removePrimary()
                cameraCapturer.videoCapturer.stopCapture()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    fun showEncryptionLayout() {

        val bottomSheetDialog = BottomSheetDialog(activity.requireContext())

        val sheetView: View = activity.requireActivity().layoutInflater.inflate(R.layout.encryption_layout, null)

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()

        sheetView.tv_meetingTopic.text = meetingData.topic
        sheetView.tvMeetingId.text = meetingData.meetingId
        sheetView.tvPassword.text = meetingData.meetingPassword.toString()
        sheetView.tvHostName.text = meetingData.hostName
        sheetView.tvLink.text = meetingData.meetingLink
        sheetView.tvParticipantId.text = meetingData.id


        if (meetingData.description!!.isNotEmpty()) {
            sheetView.tvMeetingDescription.text = meetingData.description
        }

        sheetView.tvLink.setOnClickListener {
            // CommonMethod.callBrowserIntent(activity.requireContext(), sheetView.tvLink.text.toString())
        }

    }


    fun loadViewPager(videoRoomInitializer: VideoRoomInitializer) {

        val myAdapter = MyAdapter(activity, videoRoomInitializer)
        roomView.roomViewPager.adapter = myAdapter

        val dotscount = myAdapter.count
        var dots: Array<ImageView?>? = null
        dots = arrayOfNulls(dotscount)


        for (i in 0 until dotscount) {
            dots[i] = ImageView(activity.requireActivity())
            dots[i]!!.setImageDrawable(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.non_active_dots))
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(8, 0, 8, 0)

            roomView.sliderDots.addView(dots[i], params)
        }

        dots[0]!!.setImageDrawable(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.active_dot))

        roomView.roomViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until dotscount) {
                    dots[i]!!.setImageDrawable(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.non_active_dots))
                }

                dots[position]!!.setImageDrawable(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.active_dot))
            }

        })


    }


    class MyAdapter(var activity: RoomFragment, var videoRoomInitializer: VideoRoomInitializer) : FragmentPagerAdapter(
            activity.requireActivity().supportFragmentManager) {


        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> {

                    VideoRoomFragment(videoRoomInitializer)
                }
                1 -> {
                    ParticipantFragment(activity)
                }

                else -> VideoRoomFragment(videoRoomInitializer)
            }
        }

        // this counts total number of tabs
        override fun getCount(): Int {
            return 2
        }
    }


    fun showParticipantList() {

        val dialog = Dialog(activity.requireActivity(), android.R.style.Theme_Light)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.list_participant_layout)


        val participantList = getParticipantList()


        dialog.rvParticipantList.setHasFixedSize(true)
        dialog.rvParticipantList.layoutManager = LinearLayoutManager(activity.requireActivity())
        val adapter = ParticipantListAdapter(participantList)
        dialog.rvParticipantList.adapter = adapter


        dialog.tvTitle.text = "Participants(" + participantList.size + ")"
        dialog.ivBack.setOnClickListener {
            dialog.dismiss()
        }


        dialog.tvInvite.setOnClickListener {
            CommonMethod.shareOnMeeting(activity.requireActivity())
        }

        dialog.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString(), participantList, adapter)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        dialog.show()

    }

    private fun getParticipantList(): MutableList<Item> {
        val names = mutableListOf<Item>()

        val myItem = Item(activity.localParticipantSid, "You", AppPrefs.getBoolean(Constants.AUDIO_MODE_OFF), !AppPrefs.getBoolean(Constants.VIDEO_MODE_OFF))

        names.add(0, myItem)
        for ((key, value) in MyRemoteParticipants.thumbs) {
            val item = Item(key.sid, key.identity, key.muted, key.mirror)
            names.add(item)
        }
        return names
    }

    fun filter(text: String, participantList: MutableList<Item>, adapter: ParticipantListAdapter) {
        val filterList = mutableListOf<Item>()
        participantList.forEach {
            if (it.identity.toLowerCase().contains(text.toLowerCase())) {
                filterList.add(it)
            }
        }
        adapter.filterList(filterList)
    }


    fun showListDialog(list: MutableList<String>, listener: ItemClickListener) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity.requireActivity())

        val inflater = activity.requireActivity().layoutInflater
        val convertView = inflater.inflate(R.layout.custom_list, null) as View
        builder.setView(convertView)


        val alertDialog = builder.create()
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val abc = Objects.requireNonNull(alertDialog.window)!!.attributes
        abc.gravity = Gravity.TOP or Gravity.END;
        abc.x = 50;   //x position
        abc.y = 100;   //y position
        alertDialog.show()


        val lv: ListView = convertView.findViewById<View>(R.id.listView) as ListView
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(activity.requireActivity(), android.R.layout.simple_list_item_1, list)
        lv.adapter = adapter
        lv.setOnItemClickListener { parent, view, position, id ->
            alertDialog.dismiss()
            listener.onItemClick(position)
        }

    }


}