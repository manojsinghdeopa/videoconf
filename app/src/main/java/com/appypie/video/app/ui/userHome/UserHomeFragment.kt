package com.appypie.video.app.ui.userHome

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.ui.addEditMeeting.MeetingContainerActivity
import com.appypie.video.app.ui.addEditMeeting.ResumeMeetingResponse
import com.appypie.video.app.ui.common.ContainerActivity
import com.appypie.video.app.ui.common.MeetingData
import com.appypie.video.app.ui.home.HomeActivity
import com.appypie.video.app.ui.joinMeeting.JoinMeetingResponse
import com.appypie.video.app.ui.joinMeeting.JoinMeetingViewModel
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.CommonMethod.Companion.displayOnlyDate
import com.appypie.video.app.util.Constants.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.meeting_list_layout.*
import kotlinx.android.synthetic.main.password_sheet_layout.view.*
import kotlinx.android.synthetic.main.user_home_fragment.*
import java.util.*
import javax.inject.Inject


class UserHomeFragment : BaseFragment() {

    override fun layoutRes(): Int {
        return R.layout.user_home_fragment
    }

    var selectedDate = ""
    var list = mutableListOf<MeetingData>()
    lateinit var adapter: UpcomingMeetingsAdapter

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: UpcomingMeetingViewModel? = null


    var password = ""

    private var joinMeetingViewModel: JoinMeetingViewModel? = null
    private var resumeMeetingViewModel: ResumeMeetingViewModel? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory!!).get(UpcomingMeetingViewModel::class.java)
        joinMeetingViewModel = ViewModelProvider(this, viewModelFactory!!).get(JoinMeetingViewModel::class.java)
        resumeMeetingViewModel = ViewModelProvider(this, viewModelFactory!!).get(ResumeMeetingViewModel::class.java)

        requestData(displayOnlyDate())

        tvDate.text = "Today" + CommonMethod.getCurrentDateWithoutDay()

        CommonMethod.setRecyclerView(requireContext(), rvUpcomingMeetings)

        adapter = UpcomingMeetingsAdapter(list)

        rvUpcomingMeetings.adapter = adapter

        listeners()

        observeViewModel()


    }


    override fun onResume() {
        super.onResume()
        if (IsMeetingScheduled) {
            requestData(displayOnlyDate())
            IsMeetingScheduled = false
        }

        var text = getString(R.string.start_meeting)
        var icon = R.drawable.join_meeting_img

        if (resumeMeeting) {
            text = getString(R.string.resume_meeting)
            icon = R.drawable.resume_meeting
        }

        tvStartMeeting.text = text
        ivStartMeeting.setImageResource(icon)
    }

    private fun observeViewModel() {

        viewModel!!.response.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {
                        if (it.data!!.isNotEmpty()) {

                            list.clear()
                            it.data!!.forEach { meetingData ->
                                if (meetingData.status != "Completed") {
                                    list.add(meetingData)
                                }
                            }

                            if (list.isEmpty()) {
                                updateView(false)
                            } else {
                                updateView(true)
                                adapter.updateList(list)
                            }
                        } else {
                            updateView(false)
                        }
                    } else {
                        updateView(false)
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
                ivRefresh.visibility = if (it) View.GONE else View.VISIBLE
                progressMeeting!!.visibility = if (it) View.VISIBLE else View.GONE
            }
        })


        joinMeetingViewModel!!.reponse.observe(viewLifecycleOwner, Observer { response: JoinMeetingResponse? ->
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
                        CURRENT_USER_NAME = HOST_NAME
                        CURRENT_USER_EMAIL = HOST_EMAIL

                        startActivity(Intent(requireContext(), ContainerActivity::class.java).putExtra(TITLE, getString(R.string.join_meeting)))

                    } else {

                        if (response.message.toString() == "Password is enabled") {
                            showPasswordSheet()
                        } else {
                            password = ""
                            Toast.makeText(requireActivity(), "" + response.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        joinMeetingViewModel!!.error.observe(viewLifecycleOwner, Observer { isError: Boolean? ->
            if (isError != null) if (isError) {
                CommonMethod.showToast(requireContext(), SERVER_ERROR)
            }
        })

        joinMeetingViewModel!!.loading.observe(viewLifecycleOwner, Observer { isLoading: Boolean? ->
            if (isLoading != null) {
                pbJoinMeeting!!.visibility = if (isLoading) View.VISIBLE else View.GONE
                tvJoin!!.visibility = if (isLoading) View.GONE else View.VISIBLE

            }
        })



        resumeMeetingViewModel!!.response.observe(viewLifecycleOwner, Observer { response: ResumeMeetingResponse? ->
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

                        meetingData = response.data

                        goToHome()

                    } else {

                        if (response.message.toString() == "Password is enabled") {
                            showPasswordSheet()
                        } else {
                            password = ""
                            Toast.makeText(requireActivity(), "" + response.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        })

        resumeMeetingViewModel!!.error.observe(viewLifecycleOwner, Observer { isError: Boolean? ->
            if (isError != null) if (isError) {
                CommonMethod.showToast(requireContext(), SERVER_ERROR)
            }
        })

        resumeMeetingViewModel!!.loading.observe(viewLifecycleOwner, Observer { isLoading: Boolean? ->
            if (isLoading != null) {
                pbStartResume!!.visibility = if (isLoading) View.VISIBLE else View.GONE
                viewStartMeeting.isClickable = !isLoading
            }
        })

    }


    private fun goToHome() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun updateView(visibility: Boolean) {

        rvUpcomingMeetings.visibility = if (visibility) View.VISIBLE else View.GONE

        ivNoMeetings.visibility = if (visibility) View.GONE else View.VISIBLE
        tvNoMeetings.visibility = if (visibility) View.GONE else View.VISIBLE
    }

    private fun requestData(date: String) {
        selectedDate = date
        viewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_ID, date, TimeZone.getDefault().id)
    }


    private fun callJoinMeeting() {
        joinMeetingViewModel!!.joinMeeting(CommonMethod.getHeaderMap(), etMeetingId.text.toString(), HOST_NAME, HOST_EMAIL, password, APP_ID)
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
            if (password.isEmpty()) {
                sheetView.etPasswordMeeting.error = getString(R.string.field_required)
                sheetView.etPasswordMeeting.requestFocus()
            } else if (password.length < 8) {
                sheetView.etPasswordMeeting.error = getString(R.string.password_validation)
                sheetView.etPasswordMeeting.requestFocus()
            } else {
                callJoinMeeting()
                bottomSheetDialog.dismiss()
            }
        }
    }

    private fun listeners() {

        tvJoin.setOnClickListener {

            if (etMeetingId.text.toString().isEmpty()) {
                etMeetingId.requestFocus()
                etMeetingId.error = getString(R.string.field_required)
            } else {
                callJoinMeeting()
            }

        }


        viewStartMeeting.setOnClickListener {

            if (resumeMeeting) {
                resumeMeetingViewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_NAME, HOST_EMAIL, CURRENT_MEETING_PASSWORD, CURRENT_MEETING_ID)
            } else {
                SELECTED_MEETING_ID = EMPTY
                SELECTED_MEETING_LINK = PERSONAL_MEETING_LINK
                meetingData = personalMeetingData
                FROM = getString(R.string.dash_board)
                startActivity(Intent(requireContext(), ContainerActivity::class.java).putExtra(TITLE, getString(R.string.start_meeting)))
            }
        }

        viewScheduleMeeting.setOnClickListener {
            startActivity(Intent(requireActivity(), MeetingContainerActivity::class.java))
        }

        ivDatePicker.setOnClickListener {
            showDatePicker()
        }

        tvDate.setOnClickListener {
            showDatePicker()
        }

        ivRefresh.setOnClickListener {
            requestData(selectedDate)
        }

    }


    @SuppressLint("SetTextI18n")
    fun showDatePicker() {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
                requireActivity(), R.style.DatePickerDialogThemes,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                    var month = "" + (monthOfYear + 1)
                    if ((monthOfYear + 1) < 10) {
                        month = "0" + (monthOfYear + 1)
                    }

                    tvDate.text = CommonMethod.convertDate("$dayOfMonth/$month/$year")

                    requestData("$dayOfMonth/$month/$year")

                }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
        //datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
    }


}