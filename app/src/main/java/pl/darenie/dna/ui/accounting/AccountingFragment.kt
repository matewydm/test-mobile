package pl.darenie.dna.ui.accounting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import kotlinx.android.synthetic.main.fragment_accounting.*
import pl.darenie.R
import pl.darenie.dna.callback.SettlementCallback
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.adapter.SettlementListAdapter
import pl.darenie.dna.model.json.Settlement
import pl.darenie.dna.model.json.SettlementStatusRequest
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class AccountingFragment: BaseFragment(), SettlementCallback {

    val TAG = "AccountingFragment"
    lateinit var fragmentView: View
    lateinit var topView : BottomNavigationViewEx
    @Inject
    lateinit var retrofit: Retrofit

    @BindView(R.id.listView)
    lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity.application as App).restComponent!!.inject(this)
        fragmentView = inflater?.inflate(R.layout.fragment_accounting, container, false)!!
        fragmentView.let { ButterKnife.bind(this, it) }
        setupTopAccountingNavigationView()
        return fragmentView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateToAccountListView()
    }


    private fun setupTopAccountingNavigationView() {
        topView = fragmentView.findViewById(R.id.topAccountingNavigationView)
        topView.enableAnimation(false)
        topView.enableShiftingMode(false)
        topView.enableItemShiftingMode(false)
        topView.setOnNavigationItemSelectedListener { item ->
            updateView(item.itemId)
            item.isChecked = true
            false
        }
    }

    private fun updateView(itemId: Int) {
        when (itemId){
            R.id.topToAccount -> {
                updateToAccountListView()
            }

            R.id.topToPay ->{
                updateToPayListView()
            }

            R.id.topDues ->{
                updateDuesListView()
            }

            else ->
                Log.d(TAG,"activity without topAccountingNavigationView")
        }
    }

    private fun updateDuesListView() {
        val call : Call<List<Settlement>> = retrofit.create(RestProvider::class.java).getDues()
        call.enqueue(object : Callback<List<Settlement>> {
            override fun onResponse(call : Call<List<Settlement>>, response: Response<List<Settlement>>) {
                if (response.isSuccessful) {
                    val settlements: List<Settlement> = response.body()!!
                    val settlementsAdapter = SettlementListAdapter(activity.applicationContext, R.layout.row_settlement_due, settlements)
                    settlementsAdapter.callback = this@AccountingFragment
                    listView.adapter = settlementsAdapter
                    listView.emptyView = emptyView
                } else {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<List<Settlement>>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun updateToPayListView() {
        val call : Call<List<Settlement>> = retrofit.create(RestProvider::class.java).getUnpaidSettlements()
        call.enqueue(object : Callback<List<Settlement>> {
            override fun onResponse(call : Call<List<Settlement>>, response: Response<List<Settlement>>) {
                if (response.isSuccessful) {
                    val settlements: List<Settlement> = response.body()!!
                    val settlementsAdapter = SettlementListAdapter(activity.applicationContext, R.layout.row_settlement_pay, settlements)
                    settlementsAdapter.callback = this@AccountingFragment
                    listView.adapter = settlementsAdapter
                    listView.emptyView = emptyView
                } else {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<List<Settlement>>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun updateToAccountListView() {
        val call : Call<List<Settlement>> = retrofit.create(RestProvider::class.java).getAwaitingSettlements()
        call.enqueue(object : Callback<List<Settlement>> {
            override fun onResponse(call : Call<List<Settlement>>, response: Response<List<Settlement>>) {
                if (response.isSuccessful) {
                    val settlements: List<Settlement> = response.body()!!
                    val settlementsAdapter = SettlementListAdapter(activity.applicationContext, R.layout.row_settlement_account, settlements)
                    settlementsAdapter.callback = this@AccountingFragment
                    listView.adapter = settlementsAdapter
                    listView.emptyView = emptyView
                } else {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<List<Settlement>>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }


    override fun call(settlementStatusRequest: SettlementStatusRequest) {
        val call: Call<Void> = retrofit.create(RestProvider::class.java).postSettlement(settlementStatusRequest)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Settlement status changed successfully", Toast.LENGTH_SHORT)
                    toast.show()
                    updateView(topView.selectedItemId)
                } else {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    override fun notify(settlementStatusRequest: SettlementStatusRequest) {
        val call: Call<Void> = retrofit.create(RestProvider::class.java).notifySettlement(settlementStatusRequest)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Notification sent successfully", Toast.LENGTH_SHORT)
                    toast.show()
                } else {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }
}