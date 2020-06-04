package com.appypie.video.app.ui.addEditMeeting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appypie.video.app.webservices.ApiRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AddMeetingViewModel
@Inject
constructor(private val apiRepository: ApiRepository) : ViewModel() {

    var disposable: CompositeDisposable?
    val response = MutableLiveData<AddMeetingResponse>()
    val repoLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()


    val result: LiveData<AddMeetingResponse>
        get() = response

    val error: LiveData<Boolean>
        get() = repoLoadError

    fun getLoading(): LiveData<Boolean> {
        return loading
    }

    fun call(headers: Map<String, String>,
             app_id: String?, topic: String?, description: String?,
             start_date: String?, start_time: String?, time_zone: String?,
             password_enabled: String?, video_host: String?, video_participant: String?,
             audio_host: String?, duration: String?, email: String?,
             meeting_password: String?, host_id: String?, host_name: String?,
             host_email: String?, created_by: String?) {
        loading.value = true
        disposable!!.add(apiRepository.addMeeting(headers, app_id, topic, description,
                start_date, start_time, time_zone, password_enabled, video_host,
                video_participant, audio_host, duration, email, meeting_password,
                host_id, host_name, host_email, created_by).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<AddMeetingResponse?>() {
                    override fun onSuccess(result: AddMeetingResponse) {
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

    fun edit(headers: Map<String, String>,
             app_id: String?, topic: String?, description: String?,
             start_date: String?, start_time: String?, time_zone: String?,
             password_enabled: String?, video_host: String?, video_participant: String?,
             audio_host: String?, duration: String?,
             meeting_password: String?, meeting_id: String?, host_id: String?) {
        loading.value = true
        disposable!!.add(apiRepository.editMeeting(headers, app_id, topic, description,
                start_date, start_time, time_zone, password_enabled,
                video_host, video_participant, audio_host, duration,
                meeting_password, meeting_id, host_id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<AddMeetingResponse?>() {
                    override fun onSuccess(result: AddMeetingResponse) {
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