package com.appypie.video.app.ui.room

import android.net.Uri

class UriWrapper(uri: Uri?) {

    val pathSegments: MutableList<String>? = uri?.pathSegments
}