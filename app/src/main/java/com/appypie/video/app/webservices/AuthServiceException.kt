package com.appypie.video.app.webservices

import com.appypie.video.app.webservices.AuthServiceError

class AuthServiceException(
        throwable: Throwable? = null,
        val error: AuthServiceError? = null,
        message: String? = null
) : RuntimeException(message, throwable)