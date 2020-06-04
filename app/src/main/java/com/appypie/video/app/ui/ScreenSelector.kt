package com.appypie.video.app.ui

import com.appypie.video.app.base.BaseActivity

interface ScreenSelector {

    val loginScreen: Class<out BaseActivity>
}