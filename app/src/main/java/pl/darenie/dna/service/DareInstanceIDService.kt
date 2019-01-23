package pl.darenie.dna.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import pl.darenie.dna.configuration.App
import pl.darenie.dna.configuration.ApplicationParameter
import pl.darenie.dna.model.json.DeviceRefreshRequest
import pl.darenie.dna.rest.RestProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class DareInstanceIDService : FirebaseInstanceIdService() {

    private val TAG = "DareInstanceIDService"
    var sharedPreferences : SharedPreferences = application.getSharedPreferences(ApplicationParameter.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Inject
    lateinit var retrofit: Retrofit

    init {
        (application as App).restComponent!!.inject(this)
    }

    @SuppressLint("ApplySharedPref")
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "Refreshed token: " + refreshedToken!!)
        sendRefreshToken(refreshedToken)
        val preferencesEditor = sharedPreferences.edit()
        preferencesEditor.putString(ApplicationParameter.MESSAGING_TOKEN, refreshedToken)
        preferencesEditor.commit()

    }

    private fun sendRefreshToken(refreshedToken: String) {
        val messagingToken = sharedPreferences.getString(ApplicationParameter.MESSAGING_TOKEN,"")
        val call : Call<Void> = retrofit.create(RestProvider::class.java).refreshDevice(DeviceRefreshRequest(messagingToken, refreshedToken))
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Device token update successfull")
                } else {
                    Log.d(TAG, "Cannot update deviceToken")
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                Log.d(TAG, "Connection problem")
            }
        })
    }
}