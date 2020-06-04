package com.appypie.video.app.ui.joinMeeting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.ui.home.HomeActivity
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.Constants.*
import kotlinx.android.synthetic.main.after_join_meeting.*
import kotlinx.android.synthetic.main.common_header_layout.*


class AfterJoinMeeting(var activity_container: Int) : BaseFragment() {


    private val PERMISSIONS_REQUEST_CODE = 121

    override fun layoutRes(): Int {
        return R.layout.after_join_meeting
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // requestPermissions()

        setHeader()

        addListeners()

    }


    private fun setHeader() {
        tvTitle.text = getString(R.string.join_meeting)
        ivBack.setOnClickListener {

            if (APP_TYPE == USER) {
                requireActivity().finish()
            } else {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }


    private fun addListeners() {

        switchAudio.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                AppPrefs.putBoolean(AUDIO_MODE_OFF, isChecked)
            }
        }

        switchVideo.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                AppPrefs.putBoolean(VIDEO_MODE_OFF, isChecked)
            }
        }

        ivBubbleCross.setOnClickListener {
            speechBubbleLayout.visibility = View.GONE
        }


        ivMicroPhone.setOnClickListener {
            isInternetAudioEnable = true
            CommonMethod.showToast(requireContext(), getString(R.string.internet_audio_enabled))
            // requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_CODE)
        }

        btnJoinAfter.setOnClickListener {

            if (AppPrefs.getBoolean(VIDEO_MODE_OFF)) {
                goToHome()
            } else {
                if (AppPrefs.getString(SHOW_VIDEO_PREVIEW).isEmpty()) {
                    goToVideoPreview()
                } else {
                    if (AppPrefs.getString(SHOW_VIDEO_PREVIEW) == "true") {
                        goToVideoPreview()
                    } else {
                        AppPrefs.putBoolean(SHOW_BOTTOM_PREVIEW, true)
                        goToHome()
                    }
                }
            }
        }

    }

    private fun goToVideoPreview() {
        CommonMethod.replaceFragment(requireActivity(), VideoPreviewFragment(), activity_container)
    }

    private fun goToHome() {
        requireActivity().finish()
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsGranted()) {
                updateUI(View.VISIBLE)
            } else {
                updateUI(View.GONE)
            }
        } else {
            updateUI(View.GONE)
        }
    }

    private fun updateUI(b: Int) {
        internetAudioLayout.visibility = b
        speechBubbleLayout.visibility = b
    }

    private fun permissionsGranted(): Boolean {
        val resultMic = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
        return (resultMic == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val cameraPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED

            val permissionsGranted = (cameraPermissionGranted)
            if (permissionsGranted) {
                updateUI(View.GONE)
            }
        }
    }

}