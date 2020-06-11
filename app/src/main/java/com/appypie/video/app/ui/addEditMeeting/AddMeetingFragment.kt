package com.appypie.video.app.ui.addEditMeeting

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.CommonMethod.Companion.convertMinuteToHour
import com.appypie.video.app.util.CommonMethod.Companion.displayOnlyDate
import com.appypie.video.app.util.CommonMethod.Companion.getRandomString
import com.appypie.video.app.util.CommonMethod.Companion.validateEditText
import com.appypie.video.app.util.Constants.*
import kotlinx.android.synthetic.main.add_meeting_fragment.*
import kotlinx.android.synthetic.main.common_toolbar.*
import kotlinx.android.synthetic.main.duration_dialog_layout.view.*
import kotlinx.android.synthetic.main.recycler_with_edittext.*
import java.util.*
import javax.inject.Inject


class AddMeetingFragment : BaseFragment() {

    private var isMeetingPasswordEnable = "false"
    var hour = 1
    var minute = 0
    val currentDate = displayOnlyDate()
    var currTime = ""
    var duration = 0

    var tzIdList = mutableListOf<TimeZoneModel>()

    companion object {
        var isEdit = false
        var timeZone = ""
    }


    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: AddMeetingViewModel? = null

    override fun layoutRes(): Int {
        return R.layout.add_meeting_fragment
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory!!).get(AddMeetingViewModel::class.java)

        setHeader()

        tvPersonalMeetingId.text = getString(R.string.use_personal_meeting_id) + "\n" + "(" + PERSONAL_MEETING_ID + ")"

        val tz = TimeZone.getDefault()

        etTimeZone.setText(formatTimeZone(tz))

        timeZone = tz.id

        listeners()

        observableViewModel()

    }


    private fun observableViewModel() {

        viewModel!!.result.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {
                        IsMeetingScheduled = true
                        isEdit = false
                        shouldMeetingRefresh = true
                        meetingData = it.meetingData
                        replaceFragment(MeetingDetailFragment())
                    } else {
                        CommonMethod.showToast(requireActivity(), it.message.toString())
                    }
                }

            }
        })


        viewModel!!.error.observe(viewLifecycleOwner, Observer { isError: Boolean? ->
            if (isError != null) if (isError) {
                CommonMethod.showToast(requireActivity(), SERVER_ERROR)
            }
        })

        viewModel!!.loading.observe(viewLifecycleOwner, Observer { isLoading: Boolean? ->
            if (isLoading != null) {
                progressBar!!.visibility = if (isLoading) View.VISIBLE else View.GONE
                tvDone.isClickable = !isLoading
            }
        })

    }


    private fun replaceFragment(meetingDetailFragment: MeetingDetailFragment) {
        requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.meetingContainer, meetingDetailFragment)
                .commit()

    }

    private fun listeners() {

        val c = Calendar.getInstance()
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)

        currTime = getTime(mHour, mMinute)

        etStartingTime.setText(currTime)

        etDate.setText(currentDate)

        etDuration.setText("$hour Hour")

        etDate.setOnClickListener {
            CommonMethod.showDatePickerEditText(requireActivity(), etDate)
        }

        etDuration.setOnClickListener {
            showDurationListDialog()
        }

        tvDone.setOnClickListener {

            if (isValidate()) {


                if (tvTitle.text.toString() == getString(R.string.schedule_meeting)) {
                    viewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, etMeetingTopic.text.toString(),
                            etMeetingDescription.text.toString(), etDate.text.toString(), etStartingTime.text.toString(),
                            timeZone, isMeetingPasswordEnable, "true", "true", "true",
                            duration.toString(), "2", etMeetingPassword.text.toString(), HOST_ID, HOST_NAME,
                            HOST_EMAIL, "appuser")
                } else {
                    viewModel!!.edit(CommonMethod.getHeaderMap(), APP_ID, etMeetingTopic.text.toString(),
                            etMeetingDescription.text.toString(), etDate.text.toString(), etStartingTime.text.toString(),
                            timeZone, isMeetingPasswordEnable, "true", "true", "true",
                            duration.toString(), etMeetingPassword.text.toString(), meetingData!!.meetingId, HOST_ID)
                }
            }
        }

        etStartingTime.setOnClickListener {
            showTimePicker(mHour, mMinute)
        }


        etTimeZone.setOnClickListener {
            showTZListDialog()
        }



        switchMeetingPassword.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isMeetingPasswordEnable = "true"
                meetingPasswordLayout.visibility = View.VISIBLE

                if (etMeetingPassword.text.toString().isEmpty()) {
                    etMeetingPassword.setText(getRandomString(8))
                }

            } else {
                isMeetingPasswordEnable = "false"
                meetingPasswordLayout.visibility = View.GONE
            }
        }


        if (isEdit) {

            tvTitle.text = getString(R.string.edit_meeting)
            etMeetingTopic.setText(meetingData!!.topic)

            if (meetingData!!.description.isNotEmpty()) {
                etMeetingDescription.setText(meetingData!!.description)
            }

            etDate.setText(meetingData!!.startDate)

            etStartingTime.setText(meetingData!!.startTime)

            if (meetingData!!.duration != null) {
                etDuration.setText(convertMinuteToHour(meetingData!!.duration!!))
            }

            val tz = TimeZone.getDefault()
            tz.id = meetingData!!.timeZone

            etTimeZone.setText(formatTimeZone(tz))

            if (meetingData!!.passwordEnabled!!) {
                switchMeetingPassword.isChecked = true
                meetingPasswordLayout.visibility = View.VISIBLE
                etMeetingPassword.setText(meetingData!!.meetingPassword.toString())
            }

            tvAddInvitee.visibility = View.VISIBLE
            tvAddInvitee.setOnClickListener {
                CommonMethod.shareIntent(requireActivity())
            }
        }
    }


    private fun isValidate(): Boolean {
        if (etMeetingTopic.text.toString().trim().isEmpty()) {
            validateEditText(etMeetingTopic, getString(R.string.field_required), meetingTopicLayout)
            return false
        } else if (etMeetingTopic.text.toString().trim().length == 1 && regex.matcher(etMeetingTopic.text.toString()).find()) {
            validateEditText(etMeetingTopic, getString(R.string.invalid_meeting_name), meetingTopicLayout)
            return false
        } else if (durationInputLayout.error != null) {
            return false
        } else if (isMeetingPasswordEnable == "true") {
            if (etMeetingPassword.text.toString().isEmpty()) {
                validateEditText(etMeetingPassword, getString(R.string.field_required), meetingPasswordLayout)
                return false
            } else if (etMeetingPassword.text.toString().length < 8) {
                validateEditText(etMeetingPassword, getString(R.string.password_validation), meetingPasswordLayout)
                return false
            }
        }
        return true
    }

    private fun showTimePicker(hour: Int, minute: Int) {

        val timePickerDialog = TimePickerDialog(requireActivity(), R.style.DatePickerDialogThemes,
                OnTimeSetListener { view, hourOfDay, minuteOfHour ->

                    if (currentDate == etDate.text.toString()) {

                        val datetime = Calendar.getInstance()
                        val c = Calendar.getInstance()
                        datetime[Calendar.HOUR_OF_DAY] = hourOfDay
                        datetime[Calendar.MINUTE] = minuteOfHour

                        if (datetime.timeInMillis >= c.timeInMillis) {
                            etStartingTime.setText(getTime(hourOfDay, minuteOfHour))
                        } else {

                            val c1 = Calendar.getInstance()
                            val mHour = c1.get(Calendar.HOUR_OF_DAY)
                            val mMinute = c1.get(Calendar.MINUTE)

                            etStartingTime.setText(getTime(mHour, mMinute))

                        }
                    } else {
                        etStartingTime.setText(getTime(hourOfDay, minuteOfHour))
                    }


                }, hour, minute, false)
        timePickerDialog.show()
    }


    private fun setHeader() {
        tvDone.visibility = View.VISIBLE
        tvTitle.text = getString(R.string.schedule_meeting)
        ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


    private fun getTimeZoneList(): MutableList<TimeZoneModel> {

        val timeZoneList = mutableListOf<TimeZoneModel>()
        /*val ids = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.icu.util.TimeZone.getAvailableIDs()
        } else {
            TimeZone.getAvailableIDs()
        }*/

        val ids = TimeZone.getAvailableIDs()
        for (id in ids) {
            val tz = TimeZone.getTimeZone(id)
            val formattedTZ = formatTimeZone(tz)

            val model = TimeZoneModel(formattedTZ, tz.id)
            timeZoneList.add(model)
        }

        return timeZoneList
    }

    private fun formatTimeZone(tz: TimeZone): String {
        return CommonMethod.formatTimeZone(tz)
    }

    private fun getTime(hour: Int, mins: Int): String {
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


    private fun showTZListDialog() {

        tzIdList = getTimeZoneList()

        val dialog = Dialog(requireActivity(), R.style.AppTheme_Dialog)
        /* dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)*/
        dialog.setContentView(R.layout.recycler_with_edittext)

        CommonMethod.setRecyclerView(requireContext(), dialog.rvParticipantList)

        val adapter = TimeZoneListAdapter(tzIdList, etTimeZone, dialog)
        dialog.rvParticipantList.adapter = adapter


        dialog.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString(), adapter)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        dialog.show()


    }

    fun filter(text: String, adapter: TimeZoneListAdapter) {
        val filterList = mutableListOf<TimeZoneModel>()
        tzIdList.forEach {
            if (it.timeZoneId.toLowerCase().contains(text.toLowerCase())) {
                filterList.add(it)
            }
        }
        adapter.filterList(filterList)
    }


    private fun showDurationListDialog() {

        val minutes = mutableListOf("0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55")

        val hours = mutableListOf<String>()
        for (i in 0 until 25) {
            hours.add(i.toString())
        }

        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireActivity())

        val inflater = layoutInflater
        val convertView = inflater.inflate(R.layout.duration_dialog_layout, null) as View
        alertDialog.setView(convertView)

        val dialog: AlertDialog = alertDialog.create()
        dialog.show()

        val minutesAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), R.layout.simple_list_item, minutes)
        convertView.lvMinutes.adapter = minutesAdapter


        val hoursAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), R.layout.simple_list_item, hours)
        convertView.lvHours.adapter = hoursAdapter


        convertView.lvMinutes.setOnItemClickListener { parent, view, position, id ->
            minute = minutes[position].toInt()
        }

        convertView.lvHours.setOnItemClickListener { parent, view, position, id ->
            hour = hours[position].toInt()
        }

        convertView.btnDone.setOnClickListener {
            dialog.dismiss()

            when {
                hour == 0 -> {
                    etDuration.setText("$minute Minutes")
                }
                minute == 0 -> {
                    etDuration.setText("$hour Hour")
                }
                else -> {
                    etDuration.setText("$hour Hour $minute Minutes")
                }
            }

            duration = (hour * 60) + minute
            if (duration > 1) {
                durationInputLayout.error = null
            } else {
                durationInputLayout.error = "Invalid Duration Hours"
            }
        }

    }

    override fun onStop() {
        isEdit = false
        super.onStop()
    }

}