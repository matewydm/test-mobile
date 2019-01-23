package pl.darenie.dna

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import com.facebook.FacebookSdk
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import kotlinx.android.synthetic.main.fragment_bill.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import org.robolectric.util.FragmentTestUtil.startFragment
import pl.darenie.BuildConfig
import pl.darenie.R
import pl.darenie.dna.configuration.AppConstants
import pl.darenie.dna.model.enums.Priority
import pl.darenie.dna.model.json.Bill
import pl.darenie.dna.model.json.UserDetailCash
import pl.darenie.dna.ui.bill.BillFragment
import java.util.ArrayList
import java.util.concurrent.TimeUnit


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class YourFragmentTest {

    companion object {
        private lateinit var server: MockWebServer

        @BeforeClass
        @JvmStatic
        fun setup() {
            server = MockWebServer()
            server.start(8199)
            AppConstants.API_URL = server.url("/").toString()

        }

        @AfterClass
        fun tearDown() {
            server.shutdown()
        }
    }

    @Before
    fun setUp() {
        FacebookSdk.sdkInitialize(ShadowApplication.getInstance().applicationContext)
    }

    @Test
    fun shouldNotBeNull() {
        val fragment = BillFragment.newInstance(Bill("DummyBill", 50.0, Priority.HIGH,
                                                     listOf(
                                                             UserDetailCash("token1", "Mateusz", "Test", 10.0)
                                                     ),
                                                     listOf(
                                                             UserDetailCash("token2", "Marcin", "Test", 20.0)
                                                     )
        ), true)
        startFragment(fragment)
        assertNotNull(fragment)
    }

    @Test
    fun shouldSelectedPayerBeAddedToPayersList() {
        val fragment = BillFragment.newInstance(Bill("DummyBill", 50.0, Priority.HIGH,
                                                     listOf(
                                                             UserDetailCash("token1", "Mateusz", "Test", 10.0)
                                                     ),
                                                     listOf(
                                                             UserDetailCash("token2", "Marcin", "Test", 20.0)
                                                     )
        ), true)
        startFragment(fragment)

        fragment.view.findViewById<ImageView>(R.id.payersAddIcon).performClick()

        assertTrue(ShadowAlertDialog.getShownDialogs().size == 1)
        assertEquals(fragment.payers.size, 1)

        assertTrue(fragment.view.findViewById<ListView>(R.id.chargersListView).visibility == View.VISIBLE)
    }

    @Test
    fun shouldSelectedChargerBeAddedToChargersList() {
        val fragment = BillFragment.newInstance(Bill("DummyBill", 50.0, Priority.HIGH,
                                                     listOf(
                                                             UserDetailCash("token1", "Mateusz", "Test", 10.0)
                                                     ),
                                                     listOf(
                                                             UserDetailCash("token2", "Marcin", "Test", 20.0)
                                                     )
        ), true)
        startFragment(fragment)

        fragment.view.findViewById<ImageView>(R.id.chargersAddIcon).performClick()

        assertTrue(ShadowAlertDialog.getShownDialogs().size == 1)
        assertEquals(fragment.chargers.size, 1)
    }

    @Test
    fun testBillAddingValidation() {
        server.enqueue(MockResponse()
                               .setResponseCode(200)
                               .setBody("[{ \"firebaseToken\":\"abc\"," +
                                                "\"email\": \"test@test.com\"," +
                                                "\"firstname\":\"test\"," +
                                                "\"lastname\":\"test\"," +
                                                "\"birthDate\":\"10-12-2018\"}]"))
        server.enqueue(MockResponse().setResponseCode(200))

        val fragment = BillFragment.newInstance(Bill("", 50.0, Priority.HIGH,
                                                     listOf(
                                                             UserDetailCash("token1", "Mateusz", "Test", 10.0)
                                                     ),
                                                     listOf(
                                                             UserDetailCash("token2", "Marcin", "Test", 20.0)
                                                     )
        ), true)
        startFragment(fragment)

        fragment.addBillButton.performClick()
        assertEquals(fragment.view.findViewById<EditText>(R.id.billName).error, "Bill must have a name")

        assertEquals(ShadowToast.getTextOfLatestToast(), null)

        fragment.billName.requestFocus()
        fragment.billName.setText("New Bill")
        fragment.addBillButton.performClick()
        assertEquals(fragment.billName.error, "")

        fragment.paymentField.requestFocus()
        fragment.paymentField.setText("")
        fragment.addBillButton.performClick()
        assertEquals(fragment.paymentField.error, "Payment cannot be empty")

        fragment.paymentField.requestFocus()
        fragment.paymentField.setText("50")
        fragment.addBillButton.performClick()
        assertEquals(fragment.paymentField.error, "")

        ShadowLooper.idleMainLooper(500, TimeUnit.MILLISECONDS)
        assertEquals(ShadowToast.getTextOfLatestToast(), "Sum of payers founds must be equal to payment")

        fragment.payers = ArrayList(listOf(UserDetailCash("token2", "Marcin", "Test", 50.0)))
        fragment.addBillButton.performClick()
        ShadowLooper.idleMainLooper(500, TimeUnit.MILLISECONDS)
        assertEquals(ShadowToast.getTextOfLatestToast(), "Sum of chargers debts must be equal to payment")

        fragment.chargers = ArrayList(listOf(UserDetailCash("token1", "Mateusz", "Test", 50.0)))
        fragment.addBillButton.performClick()

        Thread.sleep(2000)

        ShadowLooper.idleMainLooper(2500, TimeUnit.MILLISECONDS)

        assertEquals(ShadowToast.getTextOfLatestToast(), "Bill added successfully")
    }

    @Test
    fun testRetrofitUnsuccessfullResponse() {
        val fragment = BillFragment.newInstance(Bill("DummyBill", 50.0, Priority.HIGH,
                                                     listOf(
                                                             UserDetailCash("token1", "Mateusz", "Test", 50.0)
                                                     ),
                                                     listOf(
                                                             UserDetailCash("token2", "Marcin", "Test", 50.0)
                                                     )
        ), true)
        startFragment(fragment)

        fragment.addBillButton.performClick()

        server.enqueue(MockResponse()
                               .setResponseCode(200)
                               .setBody("[{ \"firebaseToken\":\"abc\"," +
                                                "\"email\": \"test@test.com\"," +
                                                "\"firstname\":\"test\"," +
                                                "\"lastname\":\"test\"," +
                                                "\"birthDate\":\"10-12-2018\"}]"))

        server.enqueue(MockResponse().setResponseCode(400))

        Thread.sleep(2000)

        ShadowLooper.idleMainLooper(2500, TimeUnit.MILLISECONDS)

        assertEquals(ShadowToast.getTextOfLatestToast(), "Upss... Try again later")
    }
}