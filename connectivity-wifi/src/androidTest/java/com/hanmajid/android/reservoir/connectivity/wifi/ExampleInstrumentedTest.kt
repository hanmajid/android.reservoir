package com.hanmajid.android.reservoir.connectivity.wifi

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hanmajid.android.reservoir.connectivity.wifi.ui.state.WifiStateFragment
import com.hanmajid.android.reservoir.connectivity.wifi.ui.state.WifiStateFragmentDirections

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.hanmajid.android.reservoir.connectivity.wifi", appContext.packageName)
    }

    private val navController = mock(NavController::class.java)

    @Before
    fun init() {
        val scenario = launchFragmentInContainer(themeResId = R.style.AppTheme) {
            WifiStateFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecyleOwner ->
                    if (viewLifecyleOwner != null) {
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }
    }

    @Test
    fun testNavigationToWifiScanScreen() {
        onView(ViewMatchers.withId(R.id.button_wifi_scan)).perform(ViewActions.click())
        verify(navController).navigate(
            WifiStateFragmentDirections.actionWifiStateFragmentToWifiScanFragment()
        )
    }

    @Test
    fun testNavigationToWifiP2pScreen() {
        onView(ViewMatchers.withId(R.id.button_wifi_p2p)).perform(ViewActions.click())
        verify(navController).navigate(
            WifiStateFragmentDirections.actionWifiStateFragmentToWifiP2pFragment()
        )
    }

    @Test
    fun testNavigationToWifiSuggestionScreen() {
        onView(ViewMatchers.withId(R.id.button_wifi_suggestion)).perform(ViewActions.click())
        verify(navController).navigate(
            WifiStateFragmentDirections.actionWifiStateFragmentToWifiSuggestionFragment()
        )
    }
}