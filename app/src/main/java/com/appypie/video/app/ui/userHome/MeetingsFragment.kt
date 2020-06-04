package com.appypie.video.app.ui.userHome

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.ui.common.ContainerActivity
import com.appypie.video.app.ui.common.MeetingData
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.CommonMethod.Companion.getCurrentDateWithoutDay
import com.appypie.video.app.util.CommonMethod.Companion.getTomorrowDateWithoutDay
import com.appypie.video.app.util.Constants.*
import com.appypie.video.app.util.SwipeController
import com.appypie.video.app.util.SwipeControllerActions
import kotlinx.android.synthetic.main.meeting_list_layout.*
import kotlinx.android.synthetic.main.meetings_fragment.*
import kotlinx.android.synthetic.main.today_layout.*
import kotlinx.android.synthetic.main.tomorrow_layout.*
import java.util.*
import javax.inject.Inject


class MeetingsFragment : BaseFragment() {


    override fun layoutRes(): Int {
        return R.layout.meetings_fragment
    }

    var selectedDate = ""
    var list = mutableListOf<MeetingData>()
    lateinit var adapter: UpcomingMeetingsAdapter


    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: MeetingListViewModel? = null

    private var upComingViewModel: UpcomingMeetingViewModel? = null

    private var deleteMeetingViewModel: DeleteMeetingViewModel? = null


    var title = ""
    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this, viewModelFactory!!).get(MeetingListViewModel::class.java)

        upComingViewModel = ViewModelProvider(this, viewModelFactory!!).get(UpcomingMeetingViewModel::class.java)

        deleteMeetingViewModel = ViewModelProvider(requireActivity(), viewModelFactory!!).get(DeleteMeetingViewModel::class.java)

        listeners()

        observeViewModel()

        getMeetingList()
    }


    override fun onResume() {
        super.onResume()

        if (shouldMeetingRefresh) {
            shouldMeetingRefresh = false

            getUpcomingMeetingList(selectedDate)
            getMeetingList()
        }

    }


    private fun observeViewModel() {

        viewModel!!.response.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {

                        loadMeetingList(it.data)

                    } else {
                        CommonMethod.showToast(requireContext(), it.message!!)
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



        upComingViewModel!!.response.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {
                        if (it.data!!.isNotEmpty()) {
                            updateView(true)
                            adapter.updateList(it.data as MutableList<MeetingData>)
                        } else {
                            updateView(false)
                        }
                    } else {
                        updateView(false)
                    }
                }
            }
        })

        upComingViewModel!!.error.observe(viewLifecycleOwner, Observer {
            if (it != null) if (it) {
                CommonMethod.showToast(requireContext(), SERVER_ERROR)
            }
        })


        upComingViewModel!!.loading.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                ivRefresh.visibility = if (it) View.GONE else View.VISIBLE
                progressMeeting!!.visibility = if (it) View.VISIBLE else View.GONE
            }
        })


    }


    private fun updateView(visibility: Boolean) {

        rvUpcomingMeetings.visibility = if (visibility) View.VISIBLE else View.GONE

        ivNoMeetings.visibility = if (visibility) View.GONE else View.VISIBLE
        tvNoMeetings.visibility = if (visibility) View.GONE else View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun loadMeetingList(data: MeetingListResponse.Data?) {

        if (data!!.today!!.isNotEmpty()) {
            tvTodayDate.text = "Today" + getCurrentDateWithoutDay()
            addTodayTomorrowMeetingList(rvTodayMeetings, data.today as MutableList<MeetingData>)
        } else {
            tvTodayDate.text = "Today" + getCurrentDateWithoutDay() + "\n\n" + "No Meeting Found"
        }

        if (data.tomorrow!!.isNotEmpty()) {
            tvTomorrowDate.text = "Tomorrow" + getTomorrowDateWithoutDay()
            addTodayTomorrowMeetingList(rvTomorrowMeetings, data.tomorrow as MutableList<MeetingData>)
        } else {
            tvTomorrowDate.text = "Tomorrow" + getTomorrowDateWithoutDay() + "\n\n" + "No Meeting Found"
        }

    }


    private fun addTodayTomorrowMeetingList(recyclerView: RecyclerView, list: MutableList<MeetingData>) {

        val meetingList: MutableList<MeetingData> = list
        val adapter = UpcomingMeetingsAdapter(meetingList)
        CommonMethod.setRecyclerView(requireContext(), recyclerView)
        recyclerView.adapter = adapter

        val swipeController = SwipeController(meetingList as ArrayList<MeetingData>?, object : SwipeControllerActions() {

            override fun onLeftClicked(position: Int) {
                super.onLeftClicked(position)

                try {
                    if (meetingList[position].status == "Completed") {
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
                    if (meetingList[position].status == "Completed") {
                        return
                    }
                    val builder = AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
                    builder.setTitle(getString(R.string.delete_meeting_title))
                    builder.setMessage(getString(R.string.delete_meeting_text))
                    builder.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface?, which: Int ->
                        deleteMeetingViewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_ID, meetingList[position].meetingId)
                        dialog!!.dismiss()
                        meetingList.remove(meetingList[position])
                        adapter.updateList(meetingList)
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

        recyclerView.addItemDecoration(object : ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDraw(c, parent, state)
                swipeController.onDraw(c)
            }
        })
    }


    private fun listeners() {

        CommonMethod.setRecyclerView(requireContext(), rvUpcomingMeetings)
        adapter = UpcomingMeetingsAdapter(list)
        rvUpcomingMeetings.adapter = adapter

        tvMeetingTitle.text = PERSONAL_MEETING_NAME
        tvInvitationLink.text = PERSONAL_MEETING_LINK
        tvPersonalId.text = "Personal ID : $PERSONAL_MEETING_ID"

        ivCopy.setOnClickListener {
            myClip = ClipData.newPlainText("text", PERSONAL_MEETING_LINK)
            myClipboard?.setPrimaryClip(myClip!!)
            Toast.makeText(requireActivity(), "Link Copied", Toast.LENGTH_SHORT).show();
        }

        btnStart.setOnClickListener {
            SELECTED_MEETING_ID = PERSONAL_MEETING_ID
            SELECTED_MEETING_LINK = PERSONAL_MEETING_LINK
            meetingData = personalMeetingData
            FROM = getString(R.string.meetings)
            startActivity(Intent(requireContext(), ContainerActivity::class.java).putExtra(TITLE, getString(R.string.start_meeting)))
        }


        ivDatePicker.setOnClickListener {
            showDatePicker()
        }

        tvDate.setOnClickListener {
            showDatePicker()
        }

        ivRefresh.setOnClickListener {
            getUpcomingMeetingList(selectedDate)
            getMeetingList()
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

                    getUpcomingMeetingList("$dayOfMonth/$month/$year")

                }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
        //datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
    }


    private fun getMeetingList() {
        viewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_ID, TimeZone.getDefault().id)

    }

    private fun getUpcomingMeetingList(date: String) {
        if (date.isEmpty()) return
        selectedDate = date
        upComingViewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_ID, date, TimeZone.getDefault().id)
    }
}