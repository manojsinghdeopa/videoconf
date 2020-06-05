package com.appypie.video.app.ui.joinMeeting

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.CommonMethod.Companion.validateEditText
import com.appypie.video.app.util.Constants.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.common_header_layout.*
import kotlinx.android.synthetic.main.join_meeting_fragment.*
import kotlinx.android.synthetic.main.password_sheet_layout.view.*
import javax.inject.Inject


class JoinMeetingFragment(val activity_container: Int) : BaseFragment() {

    var password = ""

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: JoinMeetingViewModel? = null


    override fun layoutRes(): Int {
        return R.layout.join_meeting_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setHeader()

        viewModel = ViewModelProvider(this, viewModelFactory!!).get(JoinMeetingViewModel::class.java)

        observableViewModel()

        btnJoin.setOnClickListener {
            if (isValidate()) {
                callJoinMeeting()
            }
        }

    }

    private fun formatMeetingId(meetingID: String): String {
        if (meetingID.matches("-?\\d+(\\.\\d+)?".toRegex())) {
            var spacedChars = ""
            spacedChars = when {
                meetingID.length >= 7 -> {
                    val firstSixChars = meetingID.substring(0, 7)
                    val lastChars = meetingID.substring(7)
                    firstSixChars.chunked(3).joinToString(" ") + lastChars
                }
                meetingID.length >= 6 -> {
                    val firstSixChars = meetingID.substring(0, 6)
                    val lastChars = meetingID.substring(6)
                    firstSixChars.chunked(3).joinToString(" ") + lastChars
                }
                meetingID.length >= 5 -> {
                    val firstSixChars = meetingID.substring(0, 5)
                    val lastChars = meetingID.substring(5)
                    firstSixChars.chunked(3).joinToString(" ") + lastChars
                }
                meetingID.length >= 4 -> {
                    val firstSixChars = meetingID.substring(0, 4)
                    val lastChars = meetingID.substring(4)
                    firstSixChars.chunked(3).joinToString(" ") + lastChars
                }
                else -> {
                    meetingID
                }
            }
            return spacedChars

        } else {
            return meetingID
        }
    }

    private fun setHeader() {
        tvTitle.text = getString(R.string.join_meeting)
        ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun callJoinMeeting() {
        viewModel!!.joinMeeting(CommonMethod.getHeaderMap(), formatMeetingId(etMeetingIdUrl.text.toString()), etName.text.toString(), etEmail.text.toString(), password, APP_ID)
    }

    private fun showPasswordSheet() {

        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val sheetView: View = requireActivity().layoutInflater.inflate(R.layout.password_sheet_layout, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.window!!.attributes.windowAnimations = R.style.dialog_animation
        bottomSheetDialog.show()

        sheetView.close_sheet.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        sheetView.continue_sheet.setOnClickListener {
            password = sheetView.etPasswordMeeting.text.toString()
            when {
                password.isEmpty() -> {
                    validateEditText(sheetView.etPasswordMeeting, getString(R.string.field_required), sheetView.meetingPasswordInput)
                }
                password.length < 8 -> {
                    validateEditText(sheetView.etPasswordMeeting, getString(R.string.invalid_password_text), sheetView.meetingPasswordInput)
                }
                else -> {
                    callJoinMeeting()
                    bottomSheetDialog.dismiss()
                }
            }
        }
    }

    private fun isValidate(): Boolean {

        if (etMeetingIdUrl.text!!.isEmpty()) {
            validateEditText(etMeetingIdUrl, getString(R.string.meeting_id_validation), meetingIdInput)
            return false
        } else if (etName.text!!.isEmpty()) {
            validateEditText(etName, getString(R.string.meeting_name_validation), meetingNameInput)
            return false
        } else if (etEmail.text!!.isNotEmpty()) {
            if (!CommonMethod.isValidEmaillId(etEmail.text.toString())) {
                validateEditText(etEmail, getString(R.string.invalid_email), meetingEmailInput)
                return false
            }
        }

        return true
    }


    private fun observableViewModel() {
        viewModel!!.reponse.observe(viewLifecycleOwner, Observer { response: JoinMeetingResponse? ->
            if (response != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                    if (response.status == 200) {

                        AppPrefs.putString(TWILIO_TOKEN, response.token.toString())
                        AppPrefs.putString(USER_ID, response.data!!.id.toString())
                        AppPrefs.putString(MEETING_HOST_NAME, response.data!!.hostName.toString())
                        AppPrefs.putString(MEETING_ID, response.data!!.meetingId.toString())
                        AppPrefs.putString(MEETING_LINK, response.data!!.meetingLink.toString())
                        AppPrefs.putString(PASSWORD_MD, response.data!!.meetingPassword.toString())

                        CURRENT_MEETING_ID = response.data!!.meetingId.toString()
                        CURRENT_MEETING_PASSWORD = response.data!!.meetingPassword.toString()
                        CURRENT_USER_NAME = etName.text.toString()
                        CURRENT_USER_EMAIL = etEmail.text.toString()

                        meetingData = response.data
                        CommonMethod.replaceFragment(requireActivity(), AfterJoinMeeting(activity_container), activity_container)

                    } else {

                        if (response.message.toString() == "Password is enabled") {
                            showPasswordSheet()
                        } else {
                            password = ""
                            CommonMethod.showToast(requireContext(), response.message.toString())
                        }
                    }
                }

            }
        })

        viewModel!!.error.observe(viewLifecycleOwner, Observer { isError: Boolean? ->
            if (isError != null) if (isError) {
                CommonMethod.showToast(requireContext(), SERVER_ERROR)
            }
        })

        viewModel!!.loading.observe(viewLifecycleOwner, Observer { isLoading: Boolean? ->
            if (isLoading != null) {
                progressBar!!.visibility = if (isLoading) View.VISIBLE else View.GONE
                btnJoin.isClickable = !isLoading
            }
        })
    }
}