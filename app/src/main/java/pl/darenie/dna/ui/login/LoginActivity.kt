package pl.darenie.dna.ui.login;

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import pl.darenie.R
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.json.User
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.base.BaseActivity
import pl.darenie.dna.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import javax.inject.Inject


open class LoginActivity : BaseActivity() {

    private var TAG: String = "LoginActivity"

    @BindView(R.id.loginField)
    lateinit var loginField: EditText
    @BindView(R.id.passwordField)
    lateinit var passwordField: EditText
    @BindView(R.id.loginBtn)
    lateinit var loginBtn: Button
    @BindView(R.id.registerBtn)
    lateinit var registerBtn: Button

    @Inject
    lateinit var retrofit: Retrofit
    private lateinit var mCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).restComponent!!.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)
        auth = initAndReturnFirebaseAuth()
//        signIn()
    }

    open fun initAndReturnFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @OnClick(R.id.loginBtn)
    fun loginAction(view: View) {
        auth.signInWithEmailAndPassword(loginField.text.toString(), passwordField.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        var user: FirebaseUser? = auth.currentUser
                        updateUI(user)
                    } else {
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                       Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

    @OnClick(R.id.registerBtn)
    fun registerAction(view: View) {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
    }

    @OnClick(R.id.fbLoginButton)
    fun fbLoginAction() {

        mCallbackManager = CallbackManager.Factory.create()
        fbLoginButton.setReadPermissions("email", "public_profile", "user_birthday", "pages_messaging_phone_number", "user_friends")
        fbLoginButton.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Log.d(TAG, "facebook:onSuccess:" + result)
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                Toast.makeText(this@LoginActivity, "Login cancelled.",
                               Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException?) {
                Log.d(TAG, "facebook:onError", error)
                Toast.makeText(this@LoginActivity, "Invalid credentials.",
                               Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:" + token)

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                        val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { fbUser, _ ->
                            try {
                                val user = User(
                                        auth.currentUser?.uid!!,
                                        fbUser.getString("email"),
                                        fbUser.getString("name").split(" ").first(),
                                        fbUser.getString("name").split(" ").last(),
                                        parseFbDate(fbUser.getString("birthday")),
                                        "")
                                user.facebookId = Profile.getCurrentProfile().id
                                updateUser(user)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        val parameters = Bundle()
                        parameters.putString("fields", "id,name,email,gender,birthday")
                        parameters.putString("edges", "friends,friendlist")
                        request.parameters = parameters
                        request.executeAsync()
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage(resources.getString(R.string.accountExists_label) + "\n" + resources.getString(R.string.accountFbLink_label))
                                .setCancelable(false)
                                .setPositiveButton("OK", { _: DialogInterface, _: Int ->
                                    fun onClick(dialog: DialogInterface, id: Int) {
                                        dialog.dismiss()
                                    }
                                })
                        val alert = builder.create()
                        alert.show()
                        LoginManager.getInstance().logOut()
                    }
                }
    }

    private fun updateUser(user: User) {
        val call: Call<Void> = retrofit.create(RestProvider::class.java).fbRegister(user)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    updateUI(auth.currentUser)
                } else {
                    signOut()
                    Toast.makeText(this@LoginActivity, "Cannot create user or log in,\n try again later",
                                   Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                signOut()
                Toast.makeText(this@LoginActivity, "Error with server connection",
                               Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseFbDate(fbDate: String?): String {
        val fbFormatter = SimpleDateFormat("MM/dd/yyyy")
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val birthday = fbFormatter.parse(fbDate)
        return formatter.format(birthday)
    }

    override fun loginAction() {
        Log.w(TAG, "loggedIn")
    }

    override fun signOutAction() {
        Log.w(TAG, "signedOut")
    }
}
