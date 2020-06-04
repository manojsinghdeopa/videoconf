package com.appypie.video.app.ui.userHome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appypie.video.app.webservices.ApiRepository
import com.appypie.video.app.webservices.CommonResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class DeleteMeetingViewModel
@Inject
constructor(private val apiRepository: ApiRepository) : ViewModel() {

    var disposable: CompositeDisposable?
    val result = MutableLiveData<CommonResponse>()
    val repoLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()


    val response: LiveData<CommonResponse>
        get() = result

    val error: LiveData<Boolean>
        get() = repoLoadError

    fun getLoading(): LiveData<Boolean> {
        return loading
    }

    fun call(headers: Map<String, String>, app_id: String?, host_id: String?, meeting_id: String?) {
        loading.value = true
        disposable!!.add(apiRepository.deleteMeeting(headers, app_id, host_id, meeting_id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<CommonResponse?>() {
                    override fun onSuccess(response: CommonResponse) {
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