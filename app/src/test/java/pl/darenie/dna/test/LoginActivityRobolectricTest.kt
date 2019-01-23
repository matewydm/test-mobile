package pl.darenie.dna.test

//package pl.darenie.dna
//
//import android.support.design.widget.NavigationView
//import android.support.test.espresso.Espresso
//import android.support.test.espresso.action.ViewActions
//import android.support.test.espresso.assertion.ViewAssertions
//import android.support.test.espresso.matcher.ViewMatchers
//import android.widget.EditText
//import com.google.firebase.FirebaseApp
//import com.google.firebase.auth.FirebaseAuth
//import junit.framework.Assert.assertNotNull
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mockito.`when`
//import org.mockito.Mockito.mock
//import org.robolectric.Robolectric
//import org.robolectric.RobolectricTestRunner
//import org.robolectric.annotation.Config
//import org.robolectric.shadows.ShadowApplication
//import pl.darenie.BuildConfig
//import pl.darenie.R
//import pl.darenie.dna.ui.login.LoginActivity
//
//
//@RunWith(RobolectricTestRunner::class)
//@Config(constants = BuildConfig::class)
//class LoginActivityRobolectricTest {
//
//    private lateinit var activity: LoginActivity
//
//    @Before
//    fun setUp() {
//        FirebaseApp.initializeApp(ShadowApplication.getInstance().applicationContext)
//        activity = Robolectric.setupActivity(LoginActivity::class.java)
//    }
//
//    @Test
//    fun clickingLogin_shouldStartMainActivity() {
//        val navigationView = activity.findViewById(R.id.navigation) as NavigationView
//        assertNotNull("TextView could not be found", navigationView)
//
//        (activity.findViewById(R.id.loginField) as EditText).setText("mateusz@test.mail.com")
//        (activity.findViewById(R.id.passwordField) as EditText).setText("TestPassword1234")
//
//        Espresso.onView(ViewMatchers.withId(R.id.loginField)).perform(ViewActions.typeText("mateusz@test.mail.com"), ViewActions.closeSoftKeyboard())
//        Espresso.onView(ViewMatchers.withId(R.id.loginField)).check(ViewAssertions.matches(ViewMatchers.withText("mateusz@test.mail.com")))
//
//        Espresso.onView(ViewMatchers.withId(R.id.passwordField)).perform(ViewActions.typeText("TestPassword1234"), ViewActions.closeSoftKeyboard())
//        Espresso.onView(ViewMatchers.withId(R.id.passwordField)).check(ViewAssertions.matches(ViewMatchers.withText("TestPassword1234")))
//
//    }
//
//    class TestLoginActivity : LoginActivity() {
//        override fun initAndReturnFirebaseAuth(): FirebaseAuth {
//            val firebaseMock = mock(FirebaseAuth::class.java)
//            `when`(firebaseMock.signInWithEmailAndPassword(loginField.text.toString(), passwordField.text.toString())).thenReturn(
//
//            )
//        }
//    }
//}
