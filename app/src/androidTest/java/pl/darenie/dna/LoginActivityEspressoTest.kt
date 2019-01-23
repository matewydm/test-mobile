package pl.darenie.dna

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isNotChecked
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.darenie.R
import pl.darenie.dna.ui.login.LoginActivity
import pl.darenie.dna.ui.login.RegisterActivity


@RunWith(AndroidJUnit4::class)
class LoginActivityEspressoTest {

    @Rule
    @JvmField
    var mActivityRule = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun ensureRegisterButtonIsStartingRegisterActivity() {
        Intents.init()
        onView(withId(R.id.registerBtn)).perform(click())
        intended(hasComponent(RegisterActivity::class.java.name))
    }

    @Test
    fun ensureLoginViewIsDisplayed() {
        onView(withId(R.id.icon)).check(matches(isDisplayed()))
        onView(withId(R.id.loginField)).check(matches(isDisplayed()))
        onView(withId(R.id.passwordField)).check(matches(isDisplayed()))
        onView(withId(R.id.loginBtn)).check(matches(isDisplayed()))
        onView(withId(R.id.orLbl)).check(matches(isDisplayed()))
    }

    @Test
    fun ensureTextChangesWork() {
        onView(withId(R.id.loginField)).perform(typeText("mateusz@test.mail.com"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.loginField)).check(matches(withText("mateusz@test.mail.com")))

        onView(withId(R.id.passwordField)).perform(typeText("TestPassword1234"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.passwordField)).check(matches(withText("TestPassword1234")))
    }

    @Test
    fun ensureRememberMeIsDisabledByDefault() {
        onView(withId(R.id.rememberCheckbox)).check(matches(isNotChecked()))
    }

}
