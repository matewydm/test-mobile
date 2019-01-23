package pl.darenie.dna.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx

import pl.darenie.R
import pl.darenie.dna.configuration.ApplicationParameter
import pl.darenie.dna.ui.base.BaseActivity
import pl.darenie.dna.ui.accounting.AccountingFragment
import pl.darenie.dna.ui.bill.BillFragment
import pl.darenie.dna.ui.social.SocialFragment
import pl.darenie.dna.ui.login.LoginActivity
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.json.DeviceTokenRequest
import pl.darenie.dna.model.json.User
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.user.UserFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class MainActivity : BaseActivity() {

    private val TAG : String = "MainActivity"
    val context : Context = this@MainActivity

    @Inject
    lateinit var retrofit : Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starting.")
        super.onCreate(savedInstanceState)
        (application as App).restComponent!!.inject(this)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(ApplicationParameter.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        setupBottomNavigationView()
        if (auth.currentUser == null) {
            signOut()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = fragmentManager.findFragmentById(R.id.container)
        fragment.onActivityResult(requestCode,resultCode,data)
    }

    private fun setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView")
        val bottomNavigationView : BottomNavigationViewEx = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.enableAnimation(false)
        bottomNavigationView.enableShiftingMode(false)
        bottomNavigationView.enableItemShiftingMode(false)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.btm_user_info -> {
                    openUserFragment()
                    item.isChecked = true
                }
                R.id.btm_friends -> {
                    replaceFragment(R.id.container, SocialFragment(), null)
                    item.isChecked = true
                }

                R.id.btm_bills -> {
                    replaceFragment(R.id.container, BillFragment(), null)
                    item.isChecked = true
                }

                R.id.btm_accounting -> {
                    replaceFragment(R.id.container, AccountingFragment(), null)
                    item.isChecked = true
                }

                else ->
                    Log.d(TAG,"activity without bottomNavigationView")
            }
            false
        }
    }

    private fun deleteDeviceToken() {
        val messagingToken = sharedPreferences.getString(ApplicationParameter.MESSAGING_TOKEN,"")
        val call : Call<Void> = retrofit.create(RestProvider::class.java).separateDevice(DeviceTokenRequest(messagingToken))
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Device token update successful")
                } else {
                    Log.d(TAG, "Cannot update deviceToken")
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                Log.d(TAG, "Connection problem")
            }
        })
    }

    fun openUserFragment() {
        val call : Call<User> = retrofit.create(RestProvider::class.java).getUser()
        call.enqueue(object : Callback<User> {
            override fun onResponse(call : Call<User>, response: Response<User>) =
                    if (response.isSuccessful) {
                        val user: User = response.body()!!
                        replaceFragment(R.id.container, UserFragment.newInstance(user),null)
                    } else {
                        Log.d(TAG, response.code().toString())
                        val toast = Toast.makeText(applicationContext, "Upss...", Toast.LENGTH_SHORT)
                        toast.show()
                        signOut()
                    }
            override fun onFailure(call : Call<User>, t: Throwable) {
                val toast = Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun postDeviceToken() {
        val messagingToken = sharedPreferences.getString(ApplicationParameter.MESSAGING_TOKEN,"")
        val call : Call<Void> = retrofit.create(RestProvider::class.java).pairDevice(DeviceTokenRequest(messagingToken))
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Device token update successful")
                } else {
                    Log.d(TAG, "Cannot update deviceToken")
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                Log.d(TAG, "Connection problem")
            }
        })
    }

    override fun loginAction() {
        openUserFragment()
        postDeviceToken()
    }

    override fun signOutAction() {
        deleteDeviceToken()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        val preferencesEditor = sharedPreferences.edit()
        if (sharedPreferences.contains(ApplicationParameter.TOKEN_HEADER)) {
            preferencesEditor.remove(ApplicationParameter.TOKEN_HEADER)
            preferencesEditor.apply()
        }
        finish()
        onStop()
    }

}
