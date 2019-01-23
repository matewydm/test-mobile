package pl.darenie.dna

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.closeSoftKeyboard
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.hasErrorText
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.darenie.R
import pl.darenie.dna.ui.login.LoginActivity
import pl.darenie.dna.ui.login.RegisterActivity


@RunWith(AndroidJUnit4::class)
class RegisterActivityEspressoTest {

    @Rule
    @JvmField
    var mActivityRule = ActivityTestRule(RegisterActivity::class.java)

    @Test
    fun ensureCanBackToLoginActivity() {
        Intents.init()
        onView(withId(R.id.backToLogin)).check(matches(isDisplayed()))
        onView(withId(R.id.backToLogin)).perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun ensureCannotRegisterWhenFieldsAreEmpty() {
        // Assert email field validation
        onView(withId(R.id.registerBtn)).perform(click())
        onView(withId(R.id.emailField)).check(matches(hasErrorText("Wrong email format")))
        onView(withId(R.id.phoneNumberField)).check(matches(hasNoErrorText()))

        onView(withId(R.id.emailField)).perform(typeText("mateusz@example.email.com")).perform(closeSoftKeyboard())
        onView(withId(R.id.emailField)).check(matches((hasNoErrorText())))

        // Assert phone number field validation
        onView(withId(R.id.registerBtn)).perform(click())
        onView(withId(R.id.phoneNumberField)).check(matches(hasErrorText("Wrong format number: +00 000 000 000")))

        onView(withId(R.id.phoneNumberField)).perform(typeText("+48 555 555 555")).perform(closeSoftKeyboard())
        onView(withId(R.id.phoneNumberField)).check(matches((hasNoErrorText())))

        // Assert password field validation
        onView(withId(R.id.registerBtn)).perform(click())
        onView(withId(R.id.passwordField)).check(matches(hasErrorText("Password too short - min 5 signs")))

        onView(withId(R.id.passwordField)).perform(typeText("testPassword")).perform(closeSoftKeyboard())
        onView(withId(R.id.passwordField)).check(matches((hasNoErrorText())))

        // Assert first name field validation
        onView(withId(R.id.registerBtn)).perform(click())
        onView(withId(R.id.firstnameField)).check(matches(hasErrorText("Empty fisrtname")))

        onView(withId(R.id.firstnameField)).perform(typeText("Mateusz")).perform(closeSoftKeyboard())
        onView(withId(R.id.firstnameField)).check(matches((hasNoErrorText())))

        // Assert last name field validation
        onView(withId(R.id.registerBtn)).perform(click())
        onView(withId(R.id.lastnameField)).check(matches(hasErrorText("Empty lastname")))

        onView(withId(R.id.lastnameField)).perform(typeText("MateuszLastName")).perform(closeSoftKeyboard())
        onView(withId(R.id.lastnameField)).check(matches((hasNoErrorText())))
    }

    private fun hasNoErrorText(): Matcher<View> {
        return object : BoundedMatcher<View, EditText>(EditText::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("has no error text: ")
            }

            override fun matchesSafely(view: EditText): Boolean {
                return view.error == null
            }
        }
    }
}
