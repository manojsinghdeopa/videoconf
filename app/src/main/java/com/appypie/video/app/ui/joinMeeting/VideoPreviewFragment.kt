package com.appypie.video.app.ui.joinMeeting

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.ui.home.HomeActivity
import com.appypie.video.app.ui.room.ParticipantController
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CameraCapturerCompat
import com.appypie.video.app.util.Constants
import com.twilio.video.CameraCapturer
import com.twilio.video.LocalVideoTrack
import com.twilio.video.VideoConstraints
import kotlinx.android.synthetic.main.common_header_layout.*
import kotlinx.android.synthetic.main.video_preview_fragment.*

class VideoPreviewFragment : BaseFragment() {

    private val CAMERA_TRACK_NAME = "camera"
    private val PERMISSIONS_REQUEST_CODE = 100

    private var participantController: ParticipantController? = null
    private lateinit var cameraCapturer: CameraCapturerCompat

    override fun layoutRes(): Int {
        return R.layout.video_preview_fragment
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHeader()

        requestPermissions()

        switchVideoPreview.setOnCheckedChangeListener { buttonView, isChecked ->
            AppPrefs.putString(Constants.SHOW_VIDEO_PREVIEW, "" + isChecked)
        }

        btnJoinVideo.setOnClickListener {
            goToHome(false)
        }

        btnJoinWithoutVideo.setOnClickListener {
            goToHome(true)
        }


    }

    private fun goToHome(b: Boolean) {
        try {
            participantController!!.removeAllThumbs()
            participantController!!.removePrimary()
            requireActivity().finish()
            AppPrefs.putBoolean(Constants.VIDEO_MODE_OFF, b)
            val intent = Intent(requireActivity(), HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            cameraCapturer.videoCapturer.stopCapture()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupPreview() {
        participantController = ParticipantController(preViewThumb, preView)
        cameraCapturer = CameraCapturerCompat(requireActivity(), CameraCapturer.CameraSource.FRONT_CAMERA)
        val videoConstraints = VideoConstraints.Builder().build()

        val cameraVideoTrack = LocalVideoTrack.create(
                requireActivity(),
                true,
                cameraCapturer.videoCapturer,
                videoConstraints,
                CAMERA_TRACK_NAME)

        participantController!!.renderAsPrimary(
                "",
                getString(R.string.you),
                cameraVideoTrack,
                true,
                cameraCapturer.cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA)

        preView.showIdentityBadge(false)
    }


    private fun setHeader() {
        tvTitle.text = getString(R.string.video_preview)
        ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsGranted()) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CODE)
            } else {
                setupPreview()
            }
        } else {
            setupPreview()
        }
    }


    private fun permissionsGranted(): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
        return (resultCamera == PackageManager.PERMISSION_GRANTED)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val cameraPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED

            val permissionsGranted = (cameraPermissionGranted)
            if (permissionsGranted) {
                setupPreview()
            } else {
                showAlert()
            }
        }
    }


    private fun showAlert() {
        val alertDialog = AlertDialog.Builder(requireActivity(), R.style.DatePickerDialogThemes)
        alertDialog.setMessage(getString(R.string.permissions_required_camera))
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("Ok") { dialog, which ->
            dialog.dismiss()
            requireActivity().supportFragmentManager.popBackStack()
        }
        alertDialog.show()
    }


}