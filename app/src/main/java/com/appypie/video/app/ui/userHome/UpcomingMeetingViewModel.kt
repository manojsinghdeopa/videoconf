package com.appypie.video.app.ui.userHome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appypie.video.app.webservices.ApiRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpcomingMeetingViewModel
@Inject
constructor(private val apiRepository: ApiRepository) : ViewModel() {

    var disposable: CompositeDisposable?
    val result = MutableLiveData<MeetingDateListResponse>()
    val repoLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()


    val response: LiveData<MeetingDateListResponse>
        get() = result

    val error: LiveData<Boolean>
        get() = repoLoadError

    fun getLoading(): LiveData<Boolean> {
        return loading
    }

    fun call(headers: Map<String, String>, app_id: String?, host_id: String?, custom_date: String?, zone: String?) {
        loading.value = true
        disposable!!.add(apiRepository.getMeetingDateList(headers, app_id, host_id, custom_date, zone).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<MeetingDateListResponse?>() {
                    override fun onSuccess(meetingResponse: MeetingDateListResponse) {
                        repoLoadError.value = false
                        result.value = meetingResponse
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        repoLoadError.value = true
                        loading.value = false
                    }
                }))
    }


    fun callDashboardList(headers: Map<String, String>, app_id: String?, host_id: String?, start_date: String?, end_date: String?, zone: String?) {
        /* start_date:09/06/2020 00:00 AM
           end_date:09/06/2020 11:59 PM */
        loading.value = true
        disposable!!.add(apiRepository.getDashboardMeetingList(headers, app_id, host_id, start_date, end_date, zone).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<MeetingDateListResponse?>() {
                    override fun onSuccess(meetingResponse: MeetingDateListResponse) {
                        repoLoadError.value = false
                        result.value = meetingResponse
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