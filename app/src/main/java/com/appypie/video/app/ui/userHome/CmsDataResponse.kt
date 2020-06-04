package com.appypie.video.app.ui.userHome

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CmsDataResponse {
    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("status_msg")
    @Expose
    var statusMsg: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("cms_data")
    @Expose
    var cmsData: CmsData? = null

    class CmsData {
        @SerializedName("_id")
        @Expose
        var id: String? = null

        @SerializedName("identifire")
        @Expose
        var identifire: String? = null

        @SerializedName("value")
        @Expose
        var value: String? = null

        @SerializedName("appId")
        @Expose
        var appId: String? = null

        @SerializedName("status")
        @Expose
        var status: Int? = null
    }
}