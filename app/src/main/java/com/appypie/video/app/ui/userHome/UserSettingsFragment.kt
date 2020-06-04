package com.appypie.video.app.ui.userHome

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseFragment
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.Constants.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.cms_dialog_layout.view.*
import kotlinx.android.synthetic.main.user_settings_fragment.*
import javax.inject.Inject


class UserSettingsFragment : BaseFragment() {


    override fun layoutRes(): Int {
        return R.layout.user_settings_fragment
    }


    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: CMSDataViewModel? = null

    var title = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory!!).get(CMSDataViewModel::class.java)

        listeners()

        observeViewModel()


    }


    private fun observeViewModel() {

        viewModel!!.response.observe(viewLifecycleOwner, Observer {

            if (it != null) {

                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {
                        showCmsSheet(it.cmsData!!.value!!)
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
                progressBar!!.visibility = if (it) View.VISIBLE else View.GONE
            }
        })

    }


    private fun listeners() {

        tvUserName.text = HOST_NAME

        tvTermCondition.setOnClickListener {
            title = getString(R.string.terms_and_conditions)
            requestData("term_condition")
        }

        tvPrivacyPolicy.setOnClickListener {
            title = getString(R.string.privacy_policy)
            requestData("privacy_policy")
        }


    }

    private fun requestData(identifier: String) {
        viewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, identifier)

    }


    private fun showCmsSheet(value: String) {

        val bottomSheetDialog = BottomSheetDialog(requireActivity())

        val sheetView: View = requireActivity().layoutInflater.inflate(R.layout.cms_dialog_layout, null)

        bottomSheetDialog.setContentView(sheetView)

        bottomSheetDialog.window!!.attributes.windowAnimations = R.style.dialog_animation


        sheetView.tvTitle.text = title

        sheetView.tvValue.text = value

        bottomSheetDialog.show()


    }


}