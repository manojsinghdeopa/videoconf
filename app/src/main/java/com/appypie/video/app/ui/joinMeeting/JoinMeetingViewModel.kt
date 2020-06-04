package com.appypie.video.app.ui.joinMeeting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appypie.video.app.webservices.ApiRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class JoinMeetingViewModel
@Inject
constructor(private val apiRepository: ApiRepository) : ViewModel() {

    var disposable: CompositeDisposable?
    val response = MutableLiveData<JoinMeetingResponse>()
    val repoLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()


    val reponse: LiveData<JoinMeetingResponse>
        get() = response

    val error: LiveData<Boolean>
        get() = repoLoadError

    fun getLoading(): LiveData<Boolean> {
        return loading
    }

    fun joinMeeting(headers: Map<String, String>, meeting_id: String?, username: String?, email: String?, password: String?, app_id: String?) {
        loading.value = true
        disposable!!.add(apiRepository.joinMeeting(headers, meeting_id, username, email, password, app_id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<JoinMeetingResponse?>() {
                    override fun onSuccess(result: JoinMeetingResponse) {
                        repoLoadError.value = false
                        response.value = result
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