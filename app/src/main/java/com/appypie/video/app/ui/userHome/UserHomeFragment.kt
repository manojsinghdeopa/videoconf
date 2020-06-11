package com.appypie.video.app.ui.userHome

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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
import com.appypie.video.app.util.CommonMethod.Companion.isMeetingCompleted
import com.appypie.video.app.util.Constants.*
import com.appypie.video.app.util.SwipeController
import com.appypie.video.app.util.SwipeControllerActions
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
    var adapter = UpcomingMeetingsAdapter(list)

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: UpcomingMeetingViewModel? = null


    var password = ""

    private var joinMeetingViewModel: JoinMeetingViewModel? = null
    private var resumeMeetingViewModel: ResumeMeetingViewModel? = null
    private var deleteMeetingViewModel: DeleteMeetingViewModel? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory!!).get(UpcomingMeetingViewModel::class.java)
        joinMeetingViewModel = ViewModelProvider(this, viewModelFactory!!).get(JoinMeetingViewModel::class.java)
        resumeMeetingViewModel = ViewModelProvider(this, viewModelFactory!!).get(ResumeMeetingViewModel::class.java)
        deleteMeetingViewModel = ViewModelProvider(requireActivity(), viewModelFactory!!).get(DeleteMeetingViewModel::class.java)

        requestData(displayOnlyDate())

        tvDate.text = "Today" + CommonMethod.getCurrentDateWithoutDay()

        listeners()

        observeViewModel()


    }


    override fun onResume() {
        super.onResume()
        if (IsMeetingScheduled) {
            requestData(displayOnlyDate())
            IsMeetingScheduled = false
        }

        var text = getString(R.string.new_meeting)
        var icon = R.drawable.join_meeting_img

        if (resumeMeeting) {
            text = getString(R.string.resume_meeting)
            icon = R.drawable.resume_meeting
        }

        tvStartMeeting.text = text
        ivStartMeeting.setImageResource(icon)
    }


    private fun adaptMeetingList(recyclerView: RecyclerView, list: MutableList<MeetingData>) {

        val meetingList: MutableList<MeetingData> = list
        val adapter = UpcomingMeetingsAdapter(meetingList)
        CommonMethod.setRecyclerView(requireContext(), recyclerView)
        recyclerView.adapter = adapter

        val swipeController = SwipeController(meetingList as ArrayList<MeetingData>?, object : SwipeControllerActions() {

            override fun onLeftClicked(position: Int) {
                super.onLeftClicked(position)

                try {

                    if (isMeetingCompleted(meetingList[position].status.toString())) {
                        return
                    }

                    SELECTED_MEETING_ID = meetingList[position].meetingId
                    SELECTED_MEETING_LINK = meetingList[position].meetingLink
                    FROM = getString(R.string.scheduled)
                    meetingData = meetingList[position]
                    startActivity(Intent(requireActivity(), ContainerActivity::class.java).putExtra(TITLE, getString(R.string.start_meeting)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onRightClicked(position: Int) {
                super.onRightClicked(position)

                try {

                    if (isMeetingCompleted(meetingList[position].status.toString())) {
                        return
                    }
                    val builder = AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
                    builder.setTitle(getString(R.string.delete_meeting_title))
                    builder.setMessage(getString(R.string.delete_meeting_text))
                    builder.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface?, which: Int ->
                        deleteMeetingViewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_ID, meetingList[position].meetingId)
                        dialog!!.dismiss()
                    }
                    builder.setNegativeButton(getString(R.string.no), null)
                    val alertDialog = builder.create()
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    /* val abc = Objects.requireNonNull(alertDialog.window)!!.attributes
                     abc.gravity = Gravity.TOP or Gravity.END
                     abc.x = 50 //x position
                     abc.y = 100 //y position*/
                    alertDialog.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        })


        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDraw(c, parent, state)
                swipeController.onDraw(c)
            }
        })
    }

    private fun observeViewModel() {

        viewModel!!.response.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {
                        if (it.data!!.isNotEmpty()) {

                            list.clear()
                            it.data!!.forEach { meetingData ->
                                if (!isMeetingCompleted(meetingData.status.toString())) {
                                    list.add(meetingData)
                                }
                            }

                            if (list.isEmpty()) {
                                updateView(false)
                            } else {
                                updateView(true)
                                adaptMeetingList(rvUpcomingMeetings, list)
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
                            CommonMethod.showToast(requireContext(), response.message.toString())
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
                        CURRENT_MEETING_ID = response.data!!.meetingId.toString()
                        CURRENT_MEETING_PASSWORD = response.data!!.meetingPassword.toString()

                        meetingData = response.data

                        goToHome()

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


        deleteMeetingViewModel!!.response.observe(viewLifecycleOwner, Observer {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (it.status == 200) {
                    requestData(displayOnlyDate())
                }
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
        // viewModel!!.callDashboardList(CommonMethod.getHeaderMap(), APP_ID, HOST_ID, "","", TimeZone.getDefault().id)
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