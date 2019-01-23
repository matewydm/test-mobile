package pl.darenie.dna.ui.bill

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import kotlinx.android.synthetic.main.fragment_bill.*
import pl.darenie.R
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.enums.Priority
import pl.darenie.dna.model.adapter.UserCashListAdapter
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.base.BaseFragment
import pl.darenie.dna.ui.extend.SearchDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject
import com.facebook.FacebookSdk.getApplicationContext
import pl.darenie.dna.model.enums.CyclicType
import pl.darenie.dna.model.enums.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList
import android.app.TimePickerDialog
import pl.darenie.dna.model.json.*
import pl.darenie.dna.ui.main.MainActivity


class BillFragment : BaseFragment() {

    private val TAG : String = "BillActivity"

    @Inject
    lateinit var retrofit: Retrofit
    var chargers  = ArrayList<UserDetailCash>()
    var payers  = ArrayList<UserDetailCash>()
    var chargersToPick = ArrayList<User>()
    var payersToPick = ArrayList<User>()
    var exBill : Bill? = null

    private val c: Calendar = Calendar.getInstance()
    private val mHour = c.get(Calendar.HOUR_OF_DAY)
    private val mMinute = c.get(Calendar.MINUTE)

    companion object {
        fun newInstance(bill: Bill, editable: Boolean): BillFragment {
            val fragment = BillFragment()
            val bundle = Bundle()
            bundle.putSerializable("BILL", bill)
            bundle.putSerializable("EDITABLE", editable)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity.application as App).restComponent!!.inject(this)
        val view = inflater?.inflate(R.layout.fragment_bill, container, false)
        view?.let {
            ButterKnife.bind(this, it)
        }
        initFriends()

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prioritySpinner.adapter = ArrayAdapter<Priority>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, Priority.values())

        cycleTypeSpinner.adapter = ArrayAdapter<CyclicType>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, CyclicType.values())
        cycleTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (cycleTypeSpinner.selectedItem == CyclicType.MONTH) {
                    weekLayout.visibility = View.GONE
                    monthLayout.visibility = View.VISIBLE
                } else {
                    monthLayout.visibility = View.GONE
                    weekLayout.visibility = View.VISIBLE
                }
            }
        }

        weekHourButton.setOnClickListener({
            val timePickerDialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                weekHourButton.text = hourOfDay.toString() + ":" + roundMinutes(minute)
                hourWeekLabel.text = hourOfDay.toString() + ":" + roundMinutes(minute)
            }, mHour, mMinute, false)
            timePickerDialog.show()
        })

        weekDaySpinner.adapter = ArrayAdapter<DayOfWeek>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, DayOfWeek.values())
        weekDaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                dayWeekLabel.text = weekDaySpinner.selectedItem.toString()
            }
        }

        monthHourButton.setOnClickListener({
            val timePickerDialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                monthHourButton.text = hourOfDay.toString() + ":" + roundMinutes(minute)
                hourMonthLabel.text = hourOfDay.toString() + ":" + roundMinutes(minute)
            }, mHour, mMinute, false)
            timePickerDialog.show()
        })

        monthDaySeeker.max = 27
        monthDaySeeker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, p2: Boolean) {
                seekBar!!.progress = p1
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                dayMonthLabel.text = (seekBar!!.progress + 1).toString()
            }
        })

        if (arguments?.getSerializable("BILL") != null) {
            exBill = arguments?.getSerializable("BILL") as Bill
        }
        var editable : Boolean = false
        if (arguments?.getSerializable("EDITABLE") != null) {
            editable = arguments?.getSerializable("EDITABLE") as Boolean
        }
        if (exBill != null) {
            val exBill = exBill!!
            billName.setText(exBill.name)
            billName.isEnabled = editable
            paymentField.setText(exBill.payment.toString())
            paymentField.isEnabled = editable
            val priorityPosition = (prioritySpinner.adapter as ArrayAdapter<Priority>).getPosition(exBill.priority)
            prioritySpinner.post(Runnable { prioritySpinner.setSelection(priorityPosition) })
            paymentField.isEnabled = editable
            payersListView.visibility = View.VISIBLE
            chargersListView.visibility = View.VISIBLE
            chargers.addAll(exBill.chargers)
            payers.addAll(exBill.payers)
            payersListView.adapter = UserCashListAdapter(activity.applicationContext, R.layout.row_payment, exBill.payers)
            chargersListView.adapter = UserCashListAdapter(activity.applicationContext, R.layout.row_payment, exBill.chargers)
            if (!editable) {
                chargersAddIcon.isEnabled = false
                payersAddIcon.isEnabled = false
            }
            if (exBill.cyclicBill != null) {
                val cyclic = exBill.cyclicBill!!
                cyclicBillCheckbox.isChecked = true
                cyclicBillCheckbox.isClickable = editable
                cyclicLayout.visibility = View.VISIBLE
                val typePosition = (cycleTypeSpinner.adapter as ArrayAdapter<CyclicType>).getPosition(cyclic.type)
                cycleTypeSpinner.setSelection(typePosition)
                cycleTypeSpinner.isEnabled = editable
                if (cyclic.type == CyclicType.WEEK) {
                    weekLayout.visibility = View.VISIBLE
                    val weekDayPosition = (cycleTypeSpinner.adapter as ArrayAdapter<DayOfWeek>).getPosition(DayOfWeek.getValueByDay(cyclic.day))
                    weekDaySpinner.setSelection(weekDayPosition)
                    weekDaySpinner.isEnabled = editable
                    weekHourButton.text = cyclic.getTime()
                } else {
                    weekLayout.visibility = View.GONE
                    monthDaySeeker.progress = cyclic.day
                    monthHourButton.text = cyclic.getTime()
                }
                hourWeekLabel.text = cyclic.getTime()
            } else {
                cycleBillLayout.visibility = View.GONE
            }
            if (editable) {
                addBillButton.text = resources.getString(R.string.updateBill_label)
            } else {
                addBillButton.visibility = View.GONE
            }
        }
    }

    private fun roundMinutes(minute: Int): String {
        val quartet = (Math.round(minute.toDouble()/15) * 15) % 60
        return when {
            minute > 45 -> "45"
            quartet == 0L -> "00"
            else -> quartet.toString()
        }
    }

    @OnClick(R.id.chargersAddIcon)
    fun chargersAddIconClickAction (view : View) {
        val alert = SearchDialog(activity, chargersToPick)
        alert.setOnDismissListener({
            chargers = composeChargerUserDetailCash(alert.selectionList)
            if (!chargers.isEmpty()) {
                chargersListView.visibility = View.VISIBLE
                chargersListView.adapter = UserCashListAdapter(activity.applicationContext, R.layout.row_payment, chargers)
            }
        })
        alert.show()
    }

    @OnClick(R.id.payersAddIcon)
    fun payersAddIconClickAction (view : View) {
        val alert = SearchDialog(activity, payersToPick)
        alert.setOnDismissListener({
            payers = composePayerUserDetailCash(alert.selectionList)
            if (!payers.isEmpty()) {
                payersListView.visibility = View.VISIBLE
                payersListView.adapter = UserCashListAdapter(activity.applicationContext, R.layout.row_payment, payers)
            }
        })
        alert.show()
    }

    private fun composeChargerUserDetailCash(selectionList: List<User>): ArrayList<UserDetailCash> {
        val returnList = ArrayList<UserDetailCash>()
        returnList.addAll(chargers)
        val payment: Double = if (selectionList.isEmpty() || paymentField.text?.toString().isNullOrEmpty()) {
            0.00
        } else {
            "%.2f".format(Locale.US,paymentField.text?.toString()?.toDouble()?.div(selectionList.size)!!).toDouble()
        }
        selectionList.forEach({
            returnList.add(UserDetailCash(it.firebaseToken,
                    it.firstname,
                    it.lastname,
                    payment))
            chargersToPick.remove(it)
        })
        return returnList
    }

    private fun composePayerUserDetailCash(selectionList: List<User>): ArrayList<UserDetailCash> {
        val returnList = ArrayList<UserDetailCash>()
        returnList.addAll(payers)
        val payment: Double = if (selectionList.isEmpty() || paymentField.text?.toString().isNullOrEmpty()) {
            0.00
        } else {
            "%.2f".format(Locale.US,paymentField.text?.toString()?.toDouble()?.div(selectionList.size)!!).toDouble()
        }
        selectionList.forEach({
            returnList.add(UserDetailCash(it.firebaseToken,
                    it.firstname,
                    it.lastname,
                    payment))
            payersToPick.remove(it)
        })
        return returnList
    }

    @OnClick(R.id.addBillButton)
    fun addBillButtonAction(view: View) {
        if (validateForm()) {
            var billAddRequest = prepareBillAddRequest()
            val call: Call<Void> = retrofit.create(RestProvider::class.java).addBill(billAddRequest)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val toast = Toast.makeText(activity.applicationContext, "Bill added successfully", Toast.LENGTH_SHORT)
                        toast.show()
                        if (activity is MainActivity) (activity as MainActivity).openUserFragment()
                    } else {
                        val toast = Toast.makeText(activity.applicationContext, "Upss... Try again later", Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d(TAG, t.message)
                    val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                    toast.show()
                }
            })
        }
    }

    private fun prepareBillAddRequest(): Bill {
        var bill = Bill(billName.text.toString(),
                paymentField.text.toString().toDouble(),
                prioritySpinner.selectedItem as Priority,
                composeUserCash((payersListView.adapter as UserCashListAdapter).list),
                composeUserCash((chargersListView.adapter as UserCashListAdapter).list))

        if (cyclicBillCheckbox.isChecked) {
            if (cycleTypeSpinner.selectedItem == CyclicType.MONTH){
                bill.cyclicBill = CyclicEntity(CyclicType.MONTH, monthDaySeeker.progress, monthHourButton.text.split(":")[0].toInt(),monthHourButton.text.split(":")[1].toInt())
            } else {
                bill.cyclicBill = CyclicEntity(CyclicType.WEEK, (weekDaySpinner.selectedItem as DayOfWeek).day, weekHourButton.text.split(":")[0].toInt(),weekHourButton.text.split(":")[1].toInt())
            }
        }
        bill.id = exBill?.id
        return bill
    }

    @OnClick(R.id.cyclicBillCheckbox)
    fun addCyclicBillCheckboxAction(view: View) {
        if (cyclicBillCheckbox.isChecked) {
            cyclicLayout.visibility = View.VISIBLE
        } else {
            cyclicLayout.visibility = View.GONE
        }
    }

    private fun composeUserCash(userCashList: List<UserDetailCash>): List<UserDetailCash> {
        var returnList = ArrayList<UserDetailCash>()
        userCashList.forEach({
            returnList.add(UserDetailCash(it.firebaseToken, it.firstname, it.lastname, it.cash))
        })
        return returnList
    }

    private fun initFriends() {
        val call : Call<List<User>> = retrofit.create(RestProvider::class.java).getUserChecks()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call : Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    chargersToPick.addAll(response.body()!!)
                    payersToPick.addAll(response.body()!!)
                    Log.d(TAG, response.code().toString())
                } else{
                    chargersToPick = ArrayList()
                    payersToPick = ArrayList()
                    Log.d(TAG, response.code().toString())
                }
            }
            override fun onFailure(call : Call<List<User>>, t: Throwable) {
                chargersToPick = ArrayList()
                payersToPick = ArrayList()
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun validateForm(): Boolean {
        if(billName.text?.toString().isNullOrEmpty() ){
            billName.error = "Bill must have a name"
            billName.requestFocus()
            return false
        } else {
            billName.error = ""
        }
        if(paymentField.text?.toString().isNullOrEmpty()){
            paymentField.error = "Payment cannot be empty"
            paymentField.requestFocus()
            return false
        } else {
            paymentField.error = ""
        }
        if(payers.isEmpty()){
            val toast = Toast.makeText(activity.applicationContext, "You must select at least one payer", Toast.LENGTH_SHORT)
            toast.show()
            return false
        }
        if(chargers.isEmpty()){
            val toast = Toast.makeText(activity.applicationContext, "You must select at least one charger", Toast.LENGTH_SHORT)
            toast.show()
            return false
        }
        if (sumCash(payers) != paymentField.text.toString().toDouble()) {
            val toast = Toast.makeText(activity.applicationContext, "Sum of payers founds must be equal to payment", Toast.LENGTH_SHORT)
            toast.show()
            return false
        }
        if (sumCash(chargers) != paymentField.text.toString().toDouble()) {
            val toast = Toast.makeText(activity.applicationContext, "Sum of chargers debts must be equal to payment", Toast.LENGTH_SHORT)
            toast.show()
            return false
        }
        if (cyclicBillCheckbox.isChecked) {
            return validateCyclicBill()
        }
        return true
    }

    private fun validateCyclicBill() : Boolean {
        if (cycleTypeSpinner.selectedItem == CyclicType.MONTH){
            if(!monthHourButton.text?.toString()!!.matches(Regex("\\d{1,2}:\\d\\d"))){
                val toast = Toast.makeText(activity.applicationContext, "You must input a time", Toast.LENGTH_SHORT)
                toast.show()
                return false
            }
        } else {
            if(!weekHourButton.text?.toString()!!.matches(Regex("\\d{1,2}:\\d\\d"))){
                val toast = Toast.makeText(activity.applicationContext, "You must input a time", Toast.LENGTH_SHORT)
                toast.show()
                return false
            }
        }
        return true
    }

    private fun sumCash(payers: ArrayList<UserDetailCash>): Double {
        return payers.map { it.cash }.sum()
    }

}