package com.appypie.video.app.ui.addEditMeeting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appypie.video.app.ui.joinMeeting.JoinMeetingResponse
import com.appypie.video.app.util.Constants
import com.appypie.video.app.webservices.ApiRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class StartMeetingViewModel
@Inject
constructor(private val apiRepository: ApiRepository) : ViewModel() {

    var disposable: CompositeDisposable?
    val result = MutableLiveData<JoinMeetingResponse>()
    val repoLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()


    val response: LiveData<JoinMeetingResponse>
        get() = result

    val error: LiveData<Boolean>
        get() = repoLoadError

    fun getLoading(): LiveData<Boolean> {
        return loading
    }

    fun call(headers: Map<String, String>,
             app_id: String?, meeting_id: String?, username: String?,
             host_id: String?, password: String?) {
        loading.value = true
        disposable!!.add(apiRepository.startMeeting(headers, app_id, meeting_id, username, host_id, password).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<JoinMeetingResponse?>() {
                    override fun onSuccess(response: JoinMeetingResponse) {
                        repoLoadError.value = false
                        result.value = response
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
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