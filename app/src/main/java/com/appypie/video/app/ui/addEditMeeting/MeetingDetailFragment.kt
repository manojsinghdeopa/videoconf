package com.appypie.video.app.ui.addEditMeeting

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.ui.common.ContainerActivity
import com.appypie.video.app.ui.userHome.DeleteMeetingViewModel
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.CommonMethod.Companion.convertMinuteToHour
import com.appypie.video.app.util.Constants.*
import kotlinx.android.synthetic.main.common_toolbar.*
import kotlinx.android.synthetic.main.meeting_detail_fragment.*
import java.util.*
import javax.inject.Inject

class MeetingDetailFragment() : BaseFragment() {

    override fun layoutRes(): Int {
        return R.layout.meeting_detail_fragment
    }

    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var deleteMeetingViewModel: DeleteMeetingViewModel? = null


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteMeetingViewModel = ViewModelProvider(this, viewModelFactory!!).get(DeleteMeetingViewModel::class.java)

        setHeader()

        val tz = TimeZone.getDefault()

        tz.id = meetingData.timeZone

        tvMeetingTitle.text = meetingData.topic

        etMeetingTime.text = "Meeting Time : " + meetingData.startTime

        etMeetingId.setText(meetingData.meetingId.toString())


        if (meetingData.description!!.trim().isNotEmpty()) {
            meetingDescriptionInput.visibility = View.VISIBLE
            etMeetingDescription.setText(meetingData.description.toString())
        }

        etInvitationLink.setText(meetingData.meetingLink.toString())

        val date = CommonMethod.convertDate(meetingData.startDate)
        val timeZone = CommonMethod.formatTimeZone(tz)
        val time = meetingData.startTime + " (" + convertMinuteToHour(meetingData.duration!!) + ")"

        etWhen.setText(timeZone + "\n\n" + date + "\n\n" + time)

        if (meetingData.passwordEnabled!!) {
            passwordLayout.visibility = View.VISIBLE
            etPassword.setText(meetingData.meetingPassword.toString())
        }

        btnStart.setOnClickListener {
            SELECTED_MEETING_ID = meetingData.meetingId
            SELECTED_MEETING_LINK = meetingData.meetingLink
            FROM = getString(R.string.scheduled)
            startActivity(Intent(requireContext(), ContainerActivity::class.java).putExtra(TITLE, getString(R.string.start_meeting)))
        }

        myClipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        ivCopy.setOnClickListener {
            myClip = ClipData.newPlainText("text", meetingData.meetingLink)
            myClipboard?.setPrimaryClip(myClip!!)
            Toast.makeText(requireActivity(), "Link Copied", Toast.LENGTH_SHORT).show();
        }

        tvAddInvitee.setOnClickListener {
            CommonMethod.shareIntent(requireActivity())
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        deleteMeetingViewModel!!.response.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {
                        shouldMeetingRefresh = true
                        requireActivity().finish()
                    } else {
                        CommonMethod.showToast(requireActivity(), it.message.toString())
                    }
                }
            }
        })

        deleteMeetingViewModel!!.error.observe(viewLifecycleOwner, Observer {
            if (it != null) if (it) {
                Toast.makeText(requireActivity(), SERVER_ERROR, Toast.LENGTH_SHORT).show()
            }
        })


        deleteMeetingViewModel!!.loading.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                ivIcon2.isClickable = !it
                progressBar!!.visibility = if (it) View.VISIBLE else View.GONE
            }
        })
    }


    private fun setHeader() {
        ivIcon1.visibility = View.VISIBLE
        ivIcon2.visibility = View.VISIBLE
        ivIcon1.setImageResource(R.drawable.ic_edit)
        ivIcon2.setImageResource(R.drawable.ic_delete)
        tvTitle.text = getString(R.string.meeting_details)

        ivBack.setOnClickListener {
            requireActivity().finish()
        }

        ivIcon1.setOnClickListener {
            AddMeetingFragment.isEdit = true
            CommonMethod.replaceFragment(requireActivity(), AddMeetingFragment(), R.id.meetingContainer)
        }

        ivIcon2.setOnClickListener {
            showDeleteAlert()
        }

    }


    private fun showDeleteAlert() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
        builder.setTitle(getString(R.string.delete_meeting_title))
        builder.setMessage(getString(R.string.delete_meeting_text))
        builder.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface?, which: Int ->
            deleteMeetingViewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_ID, meetingData.meetingId)
        }
        builder.setNegativeButton(getString(R.string.no), null)
        val alertDialog = builder.create()
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        /* val abc = Objects.requireNonNull(alertDialog.window)!!.attributes
         abc.gravity = Gravity.TOP or Gravity.END
         abc.x = 50 //x position
         abc.y = 100 //y position*/
        alertDialog.show()
    }


}