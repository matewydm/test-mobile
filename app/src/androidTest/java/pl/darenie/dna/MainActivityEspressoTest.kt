package pl.darenie.dna

import android.content.Context
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import pl.darenie.R
import pl.darenie.dna.ui.main.MainActivity


@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    public var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Mock
    var mMockContext: Context? = null

    @Before
    fun init() {
    }

    @Test
    fun checkDefaultLog() {
        onView(withId(R.id.loginField)).check(matches(isDisplayed()))
    }

    @Test
    fun checkCanLoginAndPassToMainActivity() {
        onView(withId(R.id.loginField)).perform(ViewActions.typeText("mateusz@test.mail.com"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.loginField)).check(matches(ViewMatchers.withText("mateusz@test.mail.com")))

        onView(withId(R.id.passwordField)).perform(ViewActions.typeText("TestPassword1234"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.passwordField)).check(matches(ViewMatchers.withText("TestPassword1234")))
    }

}
