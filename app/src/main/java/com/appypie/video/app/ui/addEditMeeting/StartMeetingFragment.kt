package com.appypie.video.app.ui.addEditMeeting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.ui.home.HomeActivity
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.CommonMethod.Companion.displayOnlyDate
import com.appypie.video.app.util.Constants.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.common_header_layout.*
import kotlinx.android.synthetic.main.password_sheet_layout.view.*
import kotlinx.android.synthetic.main.start_meeting_fragment.*
import java.util.*
import javax.inject.Inject


class StartMeetingFragment : BaseFragment() {

    override fun layoutRes(): Int {
        return R.layout.start_meeting_fragment
    }

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: StartMeetingViewModel? = null
    private var addMeetingViewModel: AddMeetingViewModel? = null

    var meetingId = SELECTED_MEETING_ID
    var password = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory!!).get(StartMeetingViewModel::class.java)

        addMeetingViewModel = ViewModelProvider(this, viewModelFactory!!).get(AddMeetingViewModel::class.java)

        setHeader()

        addListeners()

        observeViewModel()

    }

    private fun observeViewModel() {

        viewModel!!.response.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                    if (it.status == 200) {

                        AppPrefs.putString(TWILIO_TOKEN, it.token.toString())

                        CURRENT_MEETING_ID = it.data!!.meetingId.toString()
                        CURRENT_MEETING_PASSWORD = it.data!!.meetingPassword.toString()

                        meetingData = it.data

                        goToHome()

                    } else {

                        if (it.message.toString() == "Password is enabled") {
                            showPasswordSheet()
                        } else {
                            password = ""
                            CommonMethod.showToast(requireContext(), it.message.toString())
                        }
                    }
                }

            }
        })

        viewModel!!.error.observe(viewLifecycleOwner, Observer {
            if (it != null) if (it) {
                CommonMethod.showToast(requireContext(), SERVER_ERROR)
            }
        })


        viewModel!!.loading.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                progressBar!!.visibility = if (it) View.VISIBLE else View.GONE
                btnStartMeeting.isClickable = !it
            }
        })


        addMeetingViewModel!!.result.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {
                        IsMeetingScheduled = true
                        shouldMeetingRefresh = true
                        meetingId = it.meetingData!!.meetingId
                        requestData(true)

                    } else {
                        CommonMethod.showToast(requireActivity(), it.message.toString())
                    }
                }

            }
        })


        addMeetingViewModel!!.error.observe(viewLifecycleOwner, Observer {
            if (it != null) if (it) {
                CommonMethod.showToast(requireContext(), SERVER_ERROR)
            }
        })

        addMeetingViewModel!!.loading.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                progressBar!!.visibility = if (it) View.VISIBLE else View.GONE
                btnStartMeeting.isClickable = !it
            }
        })

    }


    private fun setHeader() {
        ivNotification.visibility = View.VISIBLE
        ivNotification.setImageResource(R.drawable.ic_invite)
        tvTitle.text = getString(R.string.start_meeting)
        ivBack.setOnClickListener {

            if (APP_TYPE == USER) {
                requireActivity().finish()
            } else {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        ivNotification.setOnClickListener {
            CommonMethod.shareIntent(requireActivity())
        }
    }


    private fun addListeners() {


        if (FROM == getString(R.string.dash_board)) {
            ivNotification.visibility = View.GONE
            pmiLayout.visibility = View.VISIBLE
            spaceView.text = getString(R.string.pmi_text)
            tvPersonalMeetingId.text = getString(R.string.use_personal_meeting_id) + "\n" + "(" + PERSONAL_MEETING_ID + ")"
        }

        if (FROM == getString(R.string.meetings)) {
            spaceView.text = getString(R.string.pmi_text2)
        }

        AppPrefs.putBoolean(SHOW_BOTTOM_PREVIEW, true)


        switchMeetingId.setOnCheckedChangeListener { buttonView, isChecked ->
            meetingId = if (isChecked) PERSONAL_MEETING_ID else SELECTED_MEETING_ID

            ivNotification.visibility = if (isChecked) View.VISIBLE else View.GONE

        }

        switchAudio.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                AppPrefs.putBoolean(AUDIO_MODE_OFF, isChecked)
            }
        }

        switchVideo.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                AppPrefs.putBoolean(VIDEO_MODE_OFF, isChecked)
                AppPrefs.putBoolean(SHOW_BOTTOM_PREVIEW, !isChecked)
            }
        }

        btnStartMeeting.setOnClickListener {

            if (FROM == getString(R.string.dash_board)) {
                if (meetingId == PERSONAL_MEETING_ID) {
                    requestData(true)
                } else {
                    requestData(false)
                }
            } else {
                requestData(true)
            }
        }


    }

    private fun requestData(b: Boolean) {

        if (b) {
            viewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, meetingId, HOST_NAME, HOST_ID, password)
        } else {
            addMeetingViewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, "Meeting",
                    " ", displayOnlyDate(), getCurrentTime(),
                    TimeZone.getDefault().id, "false", "true", "true", "true",
                    "60", "2", "", HOST_ID, HOST_NAME,
                    HOST_EMAIL, "appuser")
        }
    }


    private fun getCurrentTime(): String {

        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val mins = c.get(Calendar.MINUTE)

        var hours = hour
        var timeSet = ""
        when {
            hours > 12 -> {
                hours -= 12
                timeSet = "PM"
            }
            hours == 0 -> {
                hours += 12
                timeSet = "AM"
            }
            hours == 12 -> timeSet = "PM"
            else -> timeSet = "AM"
        }

        val minuteRounded = minuteRoundOff(mins)
        if (mins > 45) {
            hours += 1
        }

        val minutes = if (minuteRounded < 10) "0$minuteRounded" else minuteRounded.toString()

        return StringBuilder().append(hours).append(':').append(minutes).append(" ").append(timeSet).toString()
    }


    private fun minuteRoundOff(minute: Int): Int {
        return when {
            minute == 0 -> {
                0
            }
            minute <= 15 -> {
                15
            }
            minute <= 30 -> {
                30
            }
            minute <= 45 -> {
                45
            }
            else -> {
                0
            }
        }
    }

    private fun showPasswordSheet() {

        val bottomSheetDialog = BottomSheetDialog(requireActivity())

        val sheetView: View = requireActivity().layoutInflater.inflate(R.layout.password_sheet_layout, null)

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()

        sheetView.close_sheet.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        sheetView.continue_sheet.setOnClickListener {
            password = sheetView.etPasswordMeeting.text.toString()
            when {
                password.isEmpty() -> {
                    sheetView.etPasswordMeeting.error = getString(R.string.field_required)
                    sheetView.etPasswordMeeting.requestFocus()
                }
                password.length < 8 -> {
                    sheetView.etPasswordMeeting.error = "Invalid Password"
                    sheetView.etPasswordMeeting.requestFocus()
                }
                else -> {
                    requestData(true)
                    bottomSheetDialog.dismiss()
                }
            }
        }
    }


    private fun goToHome() {
        requireActivity().finish()
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}