package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Waliasuh Care", appName)
  }

  @Test
  fun `verify TopOn credentials and click persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("h6af3e64badfd8", com.example.util.TopOnAdManager.getAppId())
    assertEquals("a760869481eae8041424abeb19ad8634c", com.example.util.TopOnAdManager.getAppKey())
    assertEquals("n6af3e7b1e288d", com.example.util.TopOnAdManager.getInterstitialPlacementId())
    assertEquals("n6af3e88b080a6", com.example.util.TopOnAdManager.getRewardPlacementId())

    val prefs = context.getSharedPreferences("topon_ads_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    assertEquals(0, com.example.util.TopOnAdManager.getSavedClickCount(context))

    // Simulate 5 clicks
    for (i in 1..5) {
      com.example.util.TopOnAdManager.onUserActionClick(context)
    }
    assertEquals(5, com.example.util.TopOnAdManager.getSavedClickCount(context))

    // Simulate next 4 clicks (total 9)
    for (i in 1..4) {
      com.example.util.TopOnAdManager.onUserActionClick(context)
    }
    assertEquals(9, com.example.util.TopOnAdManager.getSavedClickCount(context))

    // 10th click triggers reset back to 0
    com.example.util.TopOnAdManager.onUserActionClick(context)
    assertEquals(0, com.example.util.TopOnAdManager.getSavedClickCount(context))
  }
}
