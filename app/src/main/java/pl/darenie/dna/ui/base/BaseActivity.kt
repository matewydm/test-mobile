package pl.darenie.dna.ui.base;

import android.annotation.SuppressLint
import android.app.Activity;
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import pl.darenie.dna.configuration.ApplicationParameter
import pl.darenie.dna.ui.login.LoginActivity


abstract class BaseActivity : AppCompatActivity() {

    private val TAG : String = "BaseActivity"
    protected lateinit var auth : FirebaseAuth
    protected lateinit var authListener : FirebaseAuth.AuthStateListener
    protected lateinit var sharedPreferences : SharedPreferences

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences(ApplicationParameter.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val preferencesEditor = sharedPreferences.edit()
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user : FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {
                auth.currentUser!!.getIdToken(true).addOnSuccessListener { result ->
                    val authToken = result.token!!
                    preferencesEditor.putString(ApplicationParameter.TOKEN_HEADER, authToken)
                    preferencesEditor.commit()
                    loginAction()
                }
            }
        }
        val messagingToken = FirebaseInstanceId.getInstance().token
        preferencesEditor.putString(ApplicationParameter.MESSAGING_TOKEN, messagingToken)
        preferencesEditor.commit()
        auth.addAuthStateListener(authListener)
    }

    abstract fun loginAction()

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }

    fun intentActivity(packageContext: Context, clazz: Class<*>) {
        val intent = Intent(packageContext, clazz)
        startActivity(intent)
        finish()
    }

    @SuppressLint("ApplySharedPref")
    fun signIn() {
        val preferencesEditor = sharedPreferences.edit()
        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val authToken = result.token!!
            preferencesEditor.putString(ApplicationParameter.TOKEN_HEADER, authToken)
            preferencesEditor.commit()
        }
    }

    open fun signOut() {
        sharedPreferences = getSharedPreferences(ApplicationParameter.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val preferencesEditor = sharedPreferences.edit()
        if (sharedPreferences.contains(ApplicationParameter.TOKEN_HEADER)) {
            preferencesEditor.remove(ApplicationParameter.TOKEN_HEADER)
            preferencesEditor.apply()
        }
        if (FirebaseAuth.getInstance().currentUser != null) {
            if (FirebaseAuth.getInstance().currentUser?.providers!!.contains("facebook.com")) {
                LoginManager.getInstance().logOut()
            }
            FirebaseAuth.getInstance().signOut()
        }
        signOutAction()
    }

    abstract fun signOutAction()

    fun replaceFragment(layoutRes: Int, fragment: Fragment, addToBackStack: String?) {
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(layoutRes, fragment)
        fragmentTransaction.addToBackStack(addToBackStack)
        fragmentTransaction.commit()
    }

    fun addFragment(layoutRes: Int, fragment: Fragment) {
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(layoutRes, fragment)
        fragmentTransaction.commit()
    }


}
