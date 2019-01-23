package pl.darenie.dna.ui.user

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import kotlinx.android.synthetic.main.fragment_profile.*
import pl.darenie.R
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.json.User
import pl.darenie.dna.ui.base.BaseFragment
import retrofit2.Retrofit
import javax.inject.Inject
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import pl.darenie.dna.model.json.Bill
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.bill.BillListFragment
import pl.darenie.dna.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


class UserFragment: BaseFragment() {

    private val TAG: String = "UserFragment"

    @Inject
    lateinit var retrofit: Retrofit
    lateinit var user: User
    private lateinit var mCallbackManager : CallbackManager


    companion object {
        fun newInstance(user: User): UserFragment {
            val fragment = UserFragment()
            val bundle = Bundle()
            bundle.putSerializable("USER", user)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode,resultCode,data)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity.application as App).restComponent!!.inject(this)
        user = arguments.getSerializable("USER") as User
        val view = inflater?.inflate(R.layout.fragment_profile, container, false)
        view?.let {
            ButterKnife.bind(this, it)
        }
        return view
    }

    @OnClick(R.id.dotsMenu)
    fun showMenu(view: View) {
        val popup = PopupMenu(activity.applicationContext, dotsMenu)
        popup.menuInflater.inflate(R.menu.user_menu, popup.menu)
        popup.setOnMenuItemClickListener { navigateFromMenu(it) }
        popup.show()
    }

    private fun navigateFromMenu(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.ownedBillsItem -> {
                showOwnedBills()
            }

            R.id.billsItem -> {
                showBillHistory()
            }

            R.id.signOutItem -> {
                (activity as MainActivity).signOut()
            }
        }
        return true
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameLabel.text = user.displayName()
        emailLabel.text = user.email
        phoneLabel.text = user.phoneNumber
        debtsLabel.text = user.balance?.debt.toString()
        duesLabel.text = user.balance?.due.toString()
        balanceLabel.text = user.balance?.balance.toString()

        periodAccountingCheckbox.visibility = View.GONE
        saveFriendButton.visibility = View.GONE

        if (!FirebaseAuth.getInstance().currentUser?.providers!!.contains(FacebookAuthProvider.PROVIDER_ID)) {
            linkWithFbLayout.visibility = View.VISIBLE
        }

        dotsMenu.visibility = View.VISIBLE

    }
    private fun showOwnedBills() {
        val call : Call<List<Bill>> = retrofit.create(RestProvider::class.java).getOwnedBills()
        call.enqueue(object : Callback<List<Bill>> {
            override fun onResponse(call : Call<List<Bill>>, response: Response<List<Bill>>) =
                    if (response.isSuccessful) {
                        val bills = ArrayList<Bill>()
                        bills.addAll(response.body()!!)
                        replaceFragment(R.id.container,BillListFragment.newInstance(bills, true),null)
                    } else {
                        Log.d(TAG, response.code().toString())
                        val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                        toast.show()
                    }
            override fun onFailure(call : Call<List<Bill>>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun showBillHistory() {
        val call : Call<List<Bill>> = retrofit.create(RestProvider::class.java).getBillHistory()
        call.enqueue(object : Callback<List<Bill>> {
            override fun onResponse(call : Call<List<Bill>>, response: Response<List<Bill>>) =
                    if (response.isSuccessful) {
                        val bills = ArrayList<Bill>()
                        bills.addAll(response.body()!!)
                        replaceFragment(R.id.container,BillListFragment.newInstance(bills, false),null)
                    } else {
                        Log.d(TAG, response.code().toString())
                        val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                        toast.show()
                    }
            override fun onFailure(call : Call<List<Bill>>, t: Throwable) {
                    val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                    toast.show()
            }
        })
    }

    @OnClick(R.id.fbLoginButton)
    fun fbLoginAction() {

        mCallbackManager = CallbackManager.Factory.create()
        fbLoginButton.setReadPermissions("email", "public_profile", "user_birthday", "user_friends", "user_about_me")
        fbLoginButton.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Log.d(TAG, "facebook:onSuccess:" + result)
                linkWithFacebookAccount(result!!.accessToken)
            }
            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                Toast.makeText(activity, "Login cancelled.",
                        Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException?) {
                Log.d(TAG, "facebook:onError", error)
                Toast.makeText(activity, "Invalid credentials.",
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun linkWithFacebookAccount(token: AccessToken) {
        Log.d(TAG, "linkWithFacebookAccount:" + token)

        val credential = FacebookAuthProvider.getCredential(token.token)
        FirebaseAuth.getInstance().currentUser?.linkWithCredential(credential)!!
            .addOnCompleteListener(activity, { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "linkWithFacebookAccount:success")
                    passFacebookId()
                    Toast.makeText(activity, "Accounts linked successfully, now you can log in two different ways",
                            Toast.LENGTH_LONG).show()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage(resources.getString(R.string.error_linking_account) + "\n" + resources.getString(R.string.tryLater_label))
                            .setCancelable(false)
                            .setPositiveButton("OK", { _: DialogInterface, _: Int ->
                                fun onClick(dialog : DialogInterface, id : Int) {
                                    dialog.dismiss()
                                }
                            })
                    val alert = builder.create()
                    alert.show()
                    LoginManager.getInstance().logOut()
                }
            })
    }

    private fun passFacebookId() {
        user.facebookId = Profile.getCurrentProfile().id
        val call : Call<Void> = retrofit.create(RestProvider::class.java).updateUser(user)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                } else {
                    Log.d(TAG, "Cannot pass facebookId to applicationServer")
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                Log.d(TAG, "Connection problem")
            }
        })
    }
}