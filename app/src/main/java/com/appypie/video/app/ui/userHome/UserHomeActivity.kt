package com.appypie.video.app.ui.userHome

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseActivity
import com.appypie.video.app.ui.addEditMeeting.MeetingContainerActivity
import com.appypie.video.app.util.CommonMethod
import kotlinx.android.synthetic.main.common_header_layout.*
import kotlinx.android.synthetic.main.user_home_bottom.*
import java.util.*


class UserHomeActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        addMainFragment()

        setHeader()

        addListeners()

    }


    private fun addMainFragment() {
        CommonMethod.addFragment(this, UserHomeFragment(), R.id.userHomeContainer)
        updateUi(getString(R.string.dash_board))
    }

    private fun updateUi(title: String) {
        tvTitle.text = title
        when (title) {

            getString(R.string.dash_board) -> {
                ivBack.visibility = View.VISIBLE
                ivNotification.visibility = View.GONE
                tvSchedule.visibility = View.GONE
                joinMeeting.alpha = 1f
                settings.alpha = .5f
                meetings.alpha = .5f

            }

            getString(R.string.settings) -> {
                ivBack.visibility = View.GONE
                ivNotification.visibility = View.GONE
                tvSchedule.visibility = View.GONE
                settings.alpha = 1f
                meetings.alpha = .5f
                joinMeeting.alpha = .5f

            }

            else -> {
                ivBack.visibility = View.GONE
                ivNotification.visibility = View.GONE
                tvSchedule.visibility = View.VISIBLE
                meetings.alpha = 1f
                settings.alpha = .5f
                joinMeeting.alpha = .5f
            }
        }


    }

    private fun addListeners() {

        settings.setOnClickListener {
            updateUi(getString(R.string.settings))
            replaceFragment(UserSettingsFragment())
        }


        joinMeeting.setOnClickListener {
            updateUi(getString(R.string.dash_board))
            replaceFragment(UserHomeFragment())
        }

        meetings.setOnClickListener {
            updateUi(getString(R.string.meetings))
            replaceFragment(MeetingsFragment())
        }


    }

    private fun replaceFragment(fragment: Fragment) {
        CommonMethod.replaceFragment(this, fragment, R.id.userHomeContainer)
    }


    private fun setHeader() {
        ivNotification.visibility = View.GONE
        tvTitle.text = getString(R.string.dash_board)
        ivBack.setOnClickListener {
            onBackPressed()
        }
        tvSchedule.setOnClickListener {
            startActivity(Intent(this, MeetingContainerActivity::class.java))
        }
    }

    override fun onBackPressed() {
        exitAlert()
    }


    private fun exitAlert() {
        val builder = AlertDialog.Builder(this, R.style.AppTheme_Dialog)
        builder.setTitle(getString(R.string.sure_to_close_text))
        builder.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface?, which: Int -> onExit() }
        builder.setNegativeButton(getString(R.string.no), null)
        val alertDialog = builder.create()
       /* alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val abc = Objects.requireNonNull(alertDialog.window)!!.attributes
        abc.gravity = Gravity.BOTTOM or Gravity.END
        abc.x = 50 //x position
        abc.y = 100 //y position*/
        alertDialog.show()
    }

    private fun onExit() {
        finish()
    }


}
