package com.appypie.video.app.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appypie.video.app.webservices.ApiRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AuthTokenViewModel @Inject constructor(private val repoRepository: ApiRepository) : ViewModel() {
    private var disposable: CompositeDisposable?
    private val response = MutableLiveData<AuthTokenResponse>()
    private val repoLoadError = MutableLiveData<Boolean>()
    private val loading = MutableLiveData<Boolean>()
    fun getResponse(): LiveData<AuthTokenResponse> {
        return response
    }

    val error: LiveData<Boolean>
        get() = repoLoadError

    fun getLoading(): LiveData<Boolean> {
        return loading
    }

    fun getAuthToken(headers: Map<String, String>, grant_type: String?, username: String?, password: String?) {
        loading.value = true
        disposable!!.add(repoRepository.getAuthToken(headers, grant_type, username, password).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<AuthTokenResponse?>() {
                    override fun onSuccess(value: AuthTokenResponse) {
                        repoLoadError.value = false
                        response.value = value
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        repoLoadError.value = true
                        loading.value = false
                    }
                }))
    }


    fun refreshToken(headers: Map<String, String>, grant_type: String?, refresh_token: String?) {
        loading.value = true
        disposable!!.add(repoRepository.refreshToken(headers, grant_type, refresh_token).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<AuthTokenResponse?>() {
                    override fun onSuccess(value: AuthTokenResponse) {
                        repoLoadError.value = false
                        response.value = value
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        repoLoadError.value = true
                        loading.value = false
                    }
                }))
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable != null) {
            disposable!!.clear()
            disposable = null
        }
    }

    init {
        disposable = CompositeDisposable()
    }
}