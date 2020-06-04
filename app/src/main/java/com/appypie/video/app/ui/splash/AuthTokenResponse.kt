package com.appypie.video.app.ui.splash

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AuthTokenResponse {
    @SerializedName("accessToken")
    @Expose
    var accessToken: String? = null

    @SerializedName("accessTokenExpiresAt")
    @Expose
    var accessTokenExpiresAt: String? = null

    @SerializedName("refreshToken")
    @Expose
    var refreshToken: String? = null

    @SerializedName("refreshTokenExpiresAt")
    @Expose
    var refreshTokenExpiresAt: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("status_msg")
    @Expose
    var statusMsg: String? = null

    @SerializedName("client")
    @Expose
    var client: Client? = null

    @SerializedName("user")
    @Expose
    var user: User? = null

    class Client {
        @SerializedName("id")
        @Expose
        var id: String? = null
    }

    class User {
        @SerializedName("username")
        @Expose
        var username: String? = null
    }
}