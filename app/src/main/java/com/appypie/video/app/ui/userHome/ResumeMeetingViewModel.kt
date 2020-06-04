package com.appypie.video.app.ui.userHome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appypie.video.app.ui.addEditMeeting.ResumeMeetingResponse
import com.appypie.video.app.webservices.ApiRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ResumeMeetingViewModel
@Inject
constructor(private val apiRepository: ApiRepository) : ViewModel() {

    var disposable: CompositeDisposable?
    val result = MutableLiveData<ResumeMeetingResponse>()
    val repoLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()


    val response: LiveData<ResumeMeetingResponse>
        get() = result

    val error: LiveData<Boolean>
        get() = repoLoadError

    fun getLoading(): LiveData<Boolean> {
        return loading
    }

    fun call(headers: Map<String, String>, app_id: String?, username: String?, email: String?, password: String?, meeting_id: String?) {
        loading.value = true
        disposable!!.add(apiRepository.resumeMeeting(headers, app_id, username, email, password, meeting_id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<ResumeMeetingResponse?>() {
                    override fun onSuccess(response: ResumeMeetingResponse) {
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