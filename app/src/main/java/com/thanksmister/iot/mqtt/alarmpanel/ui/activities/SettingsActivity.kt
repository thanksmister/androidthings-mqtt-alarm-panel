/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License.
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed
 *   ~ under the License is distributed on an "AS IS" BASIS,
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   ~ See the License for the specific language governing permissions and
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.*
import kotlinx.android.synthetic.main.activity_settings.*
import timber.log.Timber
import android.support.v4.view.ViewCompat.setAlpha
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable



class SettingsActivity : BaseActivity(), ViewPager.OnPageChangeListener, SettingsFragment.SettingsFragmentListener {

    private var settingTitles: Array<String>? = null
    private var pagerAdapter: PagerAdapter? = null
    private var actionBar: ActionBar? = null
    private val PAGE_NUM = 10

    private val inactivityHandler: Handler = Handler()
    private val inactivityCallback = Runnable {
        Toast.makeText(this@SettingsActivity, getString(R.string.toast_screen_timeout), Toast.LENGTH_LONG).show()
        dialogUtils.clearDialogs()
        finish()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.show()
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setTitle(R.string.activity_settings_title)
            actionBar = supportActionBar
        }

        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter
        viewPager.addOnPageChangeListener(this)

        setPageViewController()
        stopDisconnectTimer()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        } else if (id == R.id.action_help) {
            support()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = 0
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.menu_settings)
            val itemLen = menu.size()
            for (i in 0 until itemLen) {
                val drawable = menu.getItem(i).icon
                if (drawable != null) {
                    drawable.mutate()
                    drawable.setColorFilter(resources.getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP)
                }
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        inactivityHandler.postDelayed(inactivityCallback, 300000)
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityHandler.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, 300000)
    }

    override fun navigatePageNumber(page: Int) {
        if(page in 1..(PAGE_NUM - 1)) {
            viewPager.currentItem = page
        }
    }

    /**
     * We don't show screen saver on this screen
     */
    override fun showScreenSaver(manuallySet: Boolean) {
        //na-da
    }

    private fun support() {
        val intent = SupportActivity.createStartIntent(this@SettingsActivity)
        startActivity(intent)
    }

    private fun setPageViewController() {
        settingTitles = resources.getStringArray(R.array.settings_titles)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (actionBar != null) {
            actionBar!!.title = settingTitles!![position]
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                DEVICE_SETTINGS -> DeviceSettingsFragment()
                ALARM_SETTINGS -> AlarmSettingsFragment()
                MQTT_SETTINGS -> MqttSettingsFragment()
                NOTIFICATIONS_SETTINGS -> NotificationsSettingsFragment()
                CAMERA_SETTINGS -> CameraSettingsFragment()
                SCREEN_SAVER_SETTINGS -> ScreenSettingsFragment()
                WEATHER_SETTINGS -> WeatherSettingsFragment()
                PLATFORM_SETTINGS -> PlatformSettingsFragment()
                ABOUT_SETTINGS -> AboutFragment()
                else -> SettingsFragment()
            }
        }
        override fun getCount(): Int {
            return PAGE_NUM
        }
    }

    companion object {
        const val DEVICE_SETTINGS:Int = 1
        const val ALARM_SETTINGS:Int = 2
        const val MQTT_SETTINGS:Int = 3
        const val NOTIFICATIONS_SETTINGS:Int = 4
        const val CAMERA_SETTINGS:Int = 5
        const val SCREEN_SAVER_SETTINGS:Int = 6
        const val WEATHER_SETTINGS:Int = 7
        const val PLATFORM_SETTINGS:Int = 8
        const val ABOUT_SETTINGS:Int = 9
        fun createStartIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}