package com.appypie.video.app.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseActivity
import com.appypie.video.app.ui.addEditMeeting.ResumeMeetingResponse
import com.appypie.video.app.ui.room.RoomFragment
import com.appypie.video.app.ui.userHome.ResumeMeetingViewModel
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.Constants.*
import kotlinx.android.synthetic.main.activity_home.*
import javax.inject.Inject


class HomeActivity : BaseActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var savedInstanceState: Bundle? = null


    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var resumeMeetingViewModel: ResumeMeetingViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState

        resumeMeeting = false

        shouldBindRoom = false

        setContentView(R.layout.activity_home)

        requestPermissions()


    }

    private fun loadFragment() {
        if (savedInstanceState == null) {
            if (intent.extras != null) {
                val isFromNotification = intent.extras!!.getBoolean("isFromNotification", false)
                if (isFromNotification) {
                    // VideoService.stopService(this)
                    resumeMeetingViewModel = ViewModelProvider(this, viewModelFactory!!).get(ResumeMeetingViewModel::class.java)
                    observeViewModel()
                    resumeMeetingViewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, CURRENT_USER_NAME, CURRENT_USER_EMAIL, CURRENT_MEETING_PASSWORD, CURRENT_MEETING_ID)
                }
            } else {
                CommonMethod.addFragment(this, RoomFragment(), R.id.homeContainer)
            }
        }
    }

    private fun observeViewModel() {
        resumeMeetingViewModel!!.response.observe(this, Observer { response: ResumeMeetingResponse? ->
            if (response != null) {

                if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                    if (response.status == 200) {
                        AppPrefs.putString(TWILIO_TOKEN, response.token.toString())

                        CURRENT_MEETING_ID = response.data!!.meetingId.toString()
                        CURRENT_MEETING_PASSWORD = response.data!!.meetingPassword.toString()

                        meetingData = response.data

                        CommonMethod.replaceFragment(this, RoomFragment(), R.id.homeContainer)

                    } else {
                        CommonMethod.showToast(this, response.message.toString())
                    }
                }

            }
        })

        resumeMeetingViewModel!!.error.observe(this, Observer { isError: Boolean? ->
            if (isError != null) if (isError) {
                CommonMethod.showToast(this, SERVER_ERROR)
            }
        })

        resumeMeetingViewModel!!.loading.observe(this, Observer { isLoading: Boolean? ->
            if (isLoading != null) {
                pbHomeActivity!!.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        })
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsGranted()) {
                requestPermissions(arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                        PERMISSIONS_REQUEST_CODE)
            } else {
                loadFragment()
            }
        } else {
            loadFragment()
        }
    }


    private fun permissionsGranted(): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val resultModifyAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)
        val resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return (resultCamera == PackageManager.PERMISSION_GRANTED
                && resultMic == PackageManager.PERMISSION_GRANTED
                && resultModifyAudio == PackageManager.PERMISSION_GRANTED
                && resultStorage == PackageManager.PERMISSION_GRANTED)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val recordAudioPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val modifyAudioPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val cameraPermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED
            val writeExternalStoragePermissionGranted = grantResults[2] == PackageManager.PERMISSION_GRANTED
            val permissionsGranted = (recordAudioPermissionGranted
                    && modifyAudioPermissionGranted
                    && cameraPermissionGranted
                    && writeExternalStoragePermissionGranted)
            if (permissionsGranted) {
                loadFragment()
            } else {
                CommonMethod.showFinishAlert(getString(R.string.permissions_required), this)
            }
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
        shouldBindRoom = false
        if (APP_TYPE == USER) {
            resumeMeeting = true
            finish()
        } else {
            finish()
        }
    }

}
