package pl.darenie.dna.ui.social

import android.annotation.SuppressLint
import android.app.TimePickerDialog
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
import com.facebook.FacebookSdk
import kotlinx.android.synthetic.main.fragment_profile.*
import pl.darenie.R
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.enums.CyclicType
import pl.darenie.dna.model.enums.DayOfWeek
import pl.darenie.dna.model.json.CyclicEntity
import pl.darenie.dna.model.json.Friend
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject



class FriendProfileFragment : BaseFragment() {

    private val TAG: String = "FriendProfileFragment"

    @Inject
    lateinit var retrofit: Retrofit
    lateinit var friend: Friend

    private val c: Calendar = Calendar.getInstance()
    private val mHour = c.get(Calendar.HOUR_OF_DAY)
    private val mMinute = c.get(Calendar.MINUTE)

    companion object {
        fun newInstance(friend: Friend): FriendProfileFragment {
            val fragment = FriendProfileFragment()
            val bundle = Bundle()
            bundle.putSerializable("USER", friend)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity.application as App).restComponent!!.inject(this)
        friend = arguments.getSerializable("USER") as Friend
        val view = inflater?.inflate(R.layout.fragment_profile, container, false)
        view?.let {
            ButterKnife.bind(this, it)
        }
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameLabel.text = friend.displayName()
        emailLabel.text = friend.email
        phoneLabel.text = friend.phoneNumber
        debtsLabel.text = friend.balance.debt.toString()
        duesLabel.text = friend.balance.due.toString()
        balanceLabel.text = friend.balance.balance.toString()
        initCyclicView()
        var accounting = friend.cyclicAccounting
        if (accounting != null){
            periodAccountingCheckbox.isChecked = true
            cyclicLayout.visibility = View.VISIBLE
            val typePosition = (cycleTypeSpinner.adapter as ArrayAdapter<CyclicType>).getPosition(accounting.type)
            cycleTypeSpinner.setSelection(typePosition)
            if (accounting.type == CyclicType.WEEK) {
                weekLayout.visibility = View.VISIBLE
                val weekDayPosition = (cycleTypeSpinner.adapter as ArrayAdapter<DayOfWeek>).getPosition(DayOfWeek.getValueByDay(accounting.day))
                weekDaySpinner.setSelection(weekDayPosition)
                weekHourButton.text =  accounting.day.toString()
                dayWeekLabel.text = weekDaySpinner.selectedItem.toString()
            } else {
                weekLayout.visibility = View.GONE
                monthDaySeeker.progress = accounting.day
                dayMonthLabel.text = accounting.day.toString()
                monthHourButton.text = accounting.getTime()
            }
            hourWeekLabel.text = accounting.getTime()
        }


    }

    @SuppressLint("SetTextI18n")
    private fun initCyclicView () {
        cycleTypeSpinner.adapter = ArrayAdapter<CyclicType>(FacebookSdk.getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, CyclicType.values())
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

        weekDaySpinner.adapter = ArrayAdapter<DayOfWeek>(FacebookSdk.getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, DayOfWeek.values())
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
    }

    private fun roundMinutes(minute: Int): String {
        val quartet = (Math.round(minute.toDouble() / 15) * 15) % 60
        return when {
            minute > 45 -> "45"
            quartet == 0L -> "00"
            else -> quartet.toString()
        }
    }

    private fun validateCyclicBill() : Boolean {
        if (periodAccountingCheckbox.isChecked) {
            if (cycleTypeSpinner.selectedItem == CyclicType.MONTH) {
                if (!monthHourButton.text?.toString()!!.matches(Regex("\\d{1,2}:\\d\\d"))) {
                    val toast = Toast.makeText(activity.applicationContext, "You must input a time", Toast.LENGTH_SHORT)
                    toast.show()
                    return false
                }
            } else {
                if (!weekHourButton.text?.toString()!!.matches(Regex("\\d{1,2}:\\d\\d"))) {
                    val toast = Toast.makeText(activity.applicationContext, "You must input a time", Toast.LENGTH_SHORT)
                    toast.show()
                    return false
                }
            }
        }
        return true
    }

    @OnClick(R.id.periodAccountingCheckbox)
    fun addPeriodAccountingCheckboxAction(view: View) {
        if (periodAccountingCheckbox.isChecked) {
            cyclicLayout.visibility = View.VISIBLE
        } else {
            cyclicLayout.visibility = View.GONE
        }
    }

    @OnClick(R.id.saveFriendButton)
    fun addSaveFriendButtonAction(view: View) {
        if (validateCyclicBill()) {
            prepareFriendRequest()
            val call: Call<Void> = retrofit.create(RestProvider::class.java).updateFriend(friend)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val toast = Toast.makeText(activity.applicationContext, "Friend update successful", Toast.LENGTH_SHORT)
                        toast.show()
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
    private fun prepareFriendRequest() {
        if (periodAccountingCheckbox.isChecked) {
            if (cycleTypeSpinner.selectedItem == CyclicType.MONTH) {
                friend.cyclicAccounting = CyclicEntity(CyclicType.MONTH, monthDaySeeker.progress + 1, monthHourButton.text.split(":")[0].toInt(), monthHourButton.text.split(":")[1].toInt())
            } else {
                friend.cyclicAccounting = CyclicEntity(CyclicType.WEEK, (weekDaySpinner.selectedItem as DayOfWeek).day, weekHourButton.text.split(":")[0].toInt(), weekHourButton.text.split(":")[1].toInt())
            }
        } else {
            friend.cyclicAccounting = null
        }
    }

}