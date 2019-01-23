package pl.darenie.dna.ui.login

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import kotlinx.android.synthetic.main.activity_register.*
import pl.darenie.R
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.json.UserRegistrationRequest
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.base.BaseActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RegisterActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {

    private val TAG = "RegisterActivity"

    @BindView(R.id.emailField)
    lateinit var emailField: EditText
    @BindView(R.id.phoneNumberField)
    lateinit var phoneNumberField: EditText
    @BindView(R.id.passwordField)
    lateinit var passwordField: EditText
    @BindView(R.id.firstnameField)
    lateinit var firstnameField: EditText
    @BindView(R.id.lastnameField)
    lateinit var lastnameField: EditText

    @Inject
    lateinit var retrofit: Retrofit

    lateinit var birthDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).restComponent!!.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.registerBtn)
    fun registerAction(view: View) {
        if (validateForm()) {
            val request = prepareRegistrationRequest()
            val call: Call<Void> = retrofit.create(RestProvider::class.java).register(request)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        intentActivity(this@RegisterActivity, LoginActivity::class.java)
                        val toast = Toast.makeText(applicationContext, "Congratulations! You have registered to application", Toast.LENGTH_LONG)
                        toast.show()
                    } else {
                        Log.d(TAG, response.code().toString())
                        val toast = Toast.makeText(applicationContext, "Upss...", Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d(TAG, t.message)
                    val toast = Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT)
                    toast.show()
                }
            })
        }
    }

    private fun validateForm(): Boolean {
        if(emailField.text?.toString().isNullOrEmpty()  || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailField.text?.toString()).matches()){
            emailField.error = "Wrong email format"
            emailField.requestFocus()
            return false
        }
        if(phoneNumberField.text?.toString().isNullOrEmpty()  || !android.util.Patterns.PHONE.matcher(phoneNumberField.text?.toString()).matches()){
            phoneNumberField.error = "Wrong format number: +00 000 000 000"
            phoneNumberField.requestFocus()
            return false
        }
        if(passwordField.text?.toString()?.length!! < 5){
            passwordField.error = "Password too short - min 5 signs"
            passwordField.requestFocus()
            return false
        }
        if(firstnameField.text?.toString()?.length!! < 4){
            firstnameField.error = "Empty fisrtname"
            firstnameField.requestFocus()
            return false
        }
        if(lastnameField.text?.toString()?.length!! < 4){
            lastnameField.error = "Empty lastname"
            lastnameField.requestFocus()
            return false
        }
        if(birthDate.isEmpty()){
            val toast = Toast.makeText(applicationContext, "You must select birthDate", Toast.LENGTH_SHORT)
            toast.show()
            return false
        }
        return true;
    }

    @OnClick(R.id.backToLogin)
    fun backToLoginAction(view: View) {
        intentActivity(this@RegisterActivity, LoginActivity::class.java)
    }

    @OnClick(R.id.birthDatePickerButton)
    fun birthDatePickerButtonAction(view: View) {
        val birthDateDialog = DatePickerDialog(this@RegisterActivity, this, 1995, 1, 1)
        birthDateDialog.show()
    }

    private fun prepareRegistrationRequest(): UserRegistrationRequest {
        return UserRegistrationRequest(
                emailField.text.toString(),
                passwordField.text.toString(),
                firstnameField.text.toString(),
                lastnameField.text.toString(),
                birthDate,
                phoneNumberField.text.toString())
    }

    @SuppressLint("SimpleDateFormat")
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        birthDatePickerButton.text = day.toString().plus(" / ").plus(month + 1).plus(" / ").plus(year)
        var calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        var date = calendar.time
        var formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        birthDate = formatter.format(date)
    }

    override fun loginAction() {
        Log.w(TAG, "loggedIn")
    }

    override fun signOutAction() {
        Log.w(TAG, "signedOut")
    }
}