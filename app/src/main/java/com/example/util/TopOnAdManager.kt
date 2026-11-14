package com.example.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.ATSDK
import com.anythink.core.api.AdError
import com.anythink.interstitial.api.ATInterstitial
import com.anythink.interstitial.api.ATInterstitialListener
import com.anythink.rewardvideo.api.ATRewardVideoAd
import com.anythink.rewardvideo.api.ATRewardVideoListener
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.BuildConfig

object TopOnAdManager {
    private const val TAG = "TopOnAdManager"
    private const val PREFS_NAME = "topon_ads_prefs"
    private const val KEY_CLICK_COUNTER = "key_click_counter"
    private const val CLICKS_THRESHOLD = 10

    // Kredensial TopOn & Placement ID
    const val DEFAULT_APP_ID = "h6af3e64badfd8"
    const val DEFAULT_APP_KEY = "a760869481eae8041424abeb19ad8634c"
    const val DEFAULT_INTERSTITIAL_PLACEMENT_ID = "n6af3e7b1e288d"
    const val DEFAULT_REWARD_PLACEMENT_ID = "n6af3e88b080a6"

    private var isInitialized = false
    private var interstitialAd: ATInterstitial? = null
    private var rewardVideoAd: ATRewardVideoAd? = null

    // Callback dinamis untuk Reward Video saat ini (dijamin hanya dipanggil maksimal 1 kali)
    private var currentRewardCallback: (() -> Unit)? = null
    private var currentActionLabel: String = "membuka laporan"
    private var rewardEarnedForCurrentSession = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAppId(): String {
        return try {
            val field = BuildConfig::class.java.getField("TOPON_APP_ID")
            val v = (field.get(null) as? String)?.trim() ?: ""
            if (v.isNotBlank()) v else DEFAULT_APP_ID
        } catch (_: Exception) {
            DEFAULT_APP_ID
        }
    }

    fun getAppKey(): String {
        return try {
            val field = BuildConfig::class.java.getField("TOPON_APP_KEY")
            val v = (field.get(null) as? String)?.trim() ?: ""
            if (v.isNotBlank()) v else DEFAULT_APP_KEY
        } catch (_: Exception) {
            DEFAULT_APP_KEY
        }
    }

    fun getInterstitialPlacementId(): String {
        return try {
            val field = BuildConfig::class.java.getField("TOPON_PLACEMENT_INTERSTITIAL")
            val v = (field.get(null) as? String)?.trim() ?: ""
            if (v.isNotBlank()) v else DEFAULT_INTERSTITIAL_PLACEMENT_ID
        } catch (_: Exception) {
            DEFAULT_INTERSTITIAL_PLACEMENT_ID
        }
    }

    fun getRewardPlacementId(): String {
        return try {
            val field = BuildConfig::class.java.getField("TOPON_PLACEMENT_REWARD")
            val v = (field.get(null) as? String)?.trim() ?: ""
            if (v.isNotBlank()) v else DEFAULT_REWARD_PLACEMENT_ID
        } catch (_: Exception) {
            DEFAULT_REWARD_PLACEMENT_ID
        }
    }

    /**
     * Inisialisasi TopOn SDK dan preload iklan Interstitial & Reward
     */
    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext
        try {
            val appId = getAppId()
            val appKey = getAppKey()

            Log.d(TAG, "Initializing TopOn SDK with AppID: $appId")
            ATSDK.setNetworkLogDebug(BuildConfig.DEBUG)
            ATSDK.init(appContext, appId, appKey)
            isInitialized = true

            setupInterstitial(appContext)
            setupRewardVideo(appContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TopOn SDK", e)
        }
    }

    private fun setupInterstitial(context: Context) {
        val placementId = getInterstitialPlacementId()
        interstitialAd = ATInterstitial(context, placementId)
        interstitialAd?.setAdListener(object : ATInterstitialListener {
            override fun onInterstitialAdLoaded() {
                Log.d(TAG, "TopOn Interstitial loaded successfully")
            }

            override fun onInterstitialAdLoadFail(adError: AdError?) {
                Log.w(TAG, "TopOn Interstitial load failed: ${adError?.fullErrorInfo}")
            }

            override fun onInterstitialAdShow(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Interstitial shown")
            }

            override fun onInterstitialAdClicked(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Interstitial clicked")
            }

            override fun onInterstitialAdClose(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Interstitial closed - reloading next ad")
                interstitialAd?.load()
            }

            override fun onInterstitialAdVideoStart(entity: ATAdInfo?) {}
            override fun onInterstitialAdVideoEnd(entity: ATAdInfo?) {}
            override fun onInterstitialAdVideoError(adError: AdError?) {
                Log.w(TAG, "TopOn Interstitial video error: ${adError?.fullErrorInfo}")
            }
        })
        interstitialAd?.load()
    }

    private fun setupRewardVideo(context: Context) {
        val appContext = context.applicationContext
        val placementId = getRewardPlacementId()
        rewardVideoAd = ATRewardVideoAd(appContext, placementId)
        rewardVideoAd?.setAdListener(object : ATRewardVideoListener {
            override fun onRewardedVideoAdLoaded() {
                Log.d(TAG, "TopOn Rewarded Video loaded successfully")
            }

            override fun onRewardedVideoAdFailed(adError: AdError?) {
                Log.w(TAG, "TopOn Rewarded Video load failed: ${adError?.fullErrorInfo}")
            }

            override fun onRewardedVideoAdPlayFailed(adError: AdError?, entity: ATAdInfo?) {
                Log.w(TAG, "TopOn Rewarded Video play failed: ${adError?.fullErrorInfo}")
                val callback = currentRewardCallback
                currentRewardCallback = null
                rewardEarnedForCurrentSession = false
                rewardVideoAd?.load()
                callback?.invoke()
            }

            override fun onRewardedVideoAdPlayStart(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Rewarded Video playback started")
                rewardEarnedForCurrentSession = false
            }

            override fun onRewardedVideoAdPlayEnd(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Rewarded Video playback ended")
            }

            override fun onRewardedVideoAdClosed(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Rewarded Video closed (rewardEarned=$rewardEarnedForCurrentSession)")
                val callback = currentRewardCallback
                val earned = rewardEarnedForCurrentSession
                val actionLabel = currentActionLabel
                currentRewardCallback = null
                rewardEarnedForCurrentSession = false

                // Reload untuk persiapan penggunaan berikutnya
                rewardVideoAd?.load()

                if (earned) {
                    callback?.invoke()
                } else if (callback != null) {
                    val isEn = AppLanguageManager.getLanguage(appContext) == AppLanguage.ENGLISH
                    Toast.makeText(
                        appContext,
                        if (isEn) {
                            "Watch the video ad until the end to $actionLabel."
                        } else {
                            "Tonton video iklan hingga selesai untuk $actionLabel."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onReward(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Rewarded Video reward granted!")
                rewardEarnedForCurrentSession = true
            }

            override fun onRewardedVideoAdPlayClicked(entity: ATAdInfo?) {
                Log.d(TAG, "TopOn Rewarded Video clicked")
            }
        })
        rewardVideoAd?.load()
    }

    fun isRewardAdReady(): Boolean {
        return rewardVideoAd?.isAdReady() == true
    }

    /**
     * Mengambil jumlah klik tersimpan saat ini
     */
    fun getSavedClickCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_CLICK_COUNTER, 0)
    }

    private fun Context.findActivity(): Activity? {
        var ctx: Context? = this
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Dipanggil setiap kali pengguna mengeklik navigasi/menu/kartu umum
     * (TIDAK dipanggil saat pengguna sedang mengisi form / menginput data).
     * Hitungan tersimpan secara persisten. Saat mencapai 10 kali, memicu iklan Interstitial
     * dan mereset hitungan kembali ke 0.
     */
    fun onUserActionClick(context: Context) {
        val activity = context.findActivity()
        val prefs = getPrefs(context)
        val currentCount = prefs.getInt(KEY_CLICK_COUNTER, 0)
        val newCount = currentCount + 1

        Log.d(TAG, "User action click registered: $newCount / $CLICKS_THRESHOLD")

        if (newCount >= CLICKS_THRESHOLD) {
            // Reset hitungan ke 0
            prefs.edit().putInt(KEY_CLICK_COUNTER, 0).apply()

            if (activity != null && interstitialAd?.isAdReady() == true) {
                Log.d(TAG, "Showing Interstitial Ad on 10th click")
                interstitialAd?.show(activity)
            } else {
                Log.d(TAG, "Interstitial not ready yet on 10th click, reloading...")
                interstitialAd?.load()
            }
        } else {
            // Simpan hitungan terbaru
            prefs.edit().putInt(KEY_CLICK_COUNTER, newCount).apply()
        }
    }

    /**
     * Menampilkan Iklan Reward untuk fitur Laporan Bulanan atau Unduh/Buka Laporan PDF.
     * Jika iklan siap, memutar video berhadiah dan memanggil [onProceed] tepat 1 kali setelah reward didapat.
     * Jika iklan belum siap/gagal dimuat, langsung menjalankan [onProceed] agar pengguna tidak terhambat.
     */
    fun showRewardAdForAction(
        context: Context,
        actionLabel: String = "mengunduh laporan",
        fallbackToast: String = "Memproses laporan...",
        onProceed: () -> Unit
    ) {
        val activity = context.findActivity()
        if (activity != null && rewardVideoAd?.isAdReady() == true) {
            Log.d(TAG, "Showing Rewarded Video for: $actionLabel")
            rewardEarnedForCurrentSession = false
            currentActionLabel = actionLabel
            currentRewardCallback = onProceed
            rewardVideoAd?.show(activity)
        } else {
            Log.d(TAG, "Rewarded video not ready for $actionLabel, reloading and running fallback")
            rewardVideoAd?.load()
            Toast.makeText(context, fallbackToast, Toast.LENGTH_SHORT).show()
            onProceed()
        }
    }

    /**
     * Menampilkan Iklan Reward pada menu Laporan Bulanan (di halaman profil).
     */
    fun showRewardAdForMonthlyReport(
        context: Context,
        onProceedToReport: () -> Unit
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        showRewardAdForAction(
            context = context,
            actionLabel = if (isEn) "open Monthly Report" else "membuka Laporan Bulanan",
            fallbackToast = if (isEn) "Opening Monthly Report..." else "Membuka Laporan Bulanan...",
            onProceed = onProceedToReport
        )
    }

    /**
     * Menampilkan Iklan Reward sebelum membuat/mengunduh Laporan PDF.
     */
    fun showRewardAdForPdfReport(
        context: Context,
        onProceedToPdf: () -> Unit
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        showRewardAdForAction(
            context = context,
            actionLabel = if (isEn) "download PDF Report" else "mengunduh Laporan PDF",
            fallbackToast = if (isEn) "Preparing PDF Report..." else "Menyiapkan Laporan PDF...",
            onProceed = onProceedToPdf
        )
    }
}
