package pl.darenie.dna

import android.support.design.widget.NavigationView
import com.google.firebase.FirebaseApp
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLooper
import pl.darenie.BuildConfig
import pl.darenie.R
import pl.darenie.dna.configuration.AppConstants
import pl.darenie.dna.ui.login.LoginActivity
import pl.darenie.dna.ui.main.MainActivity
import java.util.concurrent.TimeUnit


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class MainActivityRobolectricTest {

    companion object {
        private lateinit var server: MockWebServer

        @BeforeClass
        @JvmStatic
        fun setup() {
            server = MockWebServer()
            server.start(8198)
            AppConstants.API_URL = server.url("/").toString()
        }

        @AfterClass
        fun tearDown() {
            server.shutdown()
        }
    }

    private lateinit var activity: MainActivity

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(ShadowApplication.getInstance().applicationContext)

        activity = Robolectric.setupActivity(MainActivity::class.java)
    }

    @Test
    fun validateMainActivityIsVisibleOnlyForAMomentIfUserIsNotAuthenticated() {
        val navigationView = activity.findViewById(R.id.navigation) as NavigationView
        assertNotNull("TextView could not be found", navigationView)

        server.enqueue(MockResponse())
        ShadowLooper.idleMainLooper(3000, TimeUnit.MILLISECONDS)

        val startedIntent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(LoginActivity::class.java, shadowIntent.intentClass)
    }
}
