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

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.squareup.picasso.Picasso
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.InstagramApi
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.InstagramFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramItem
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramResponse
import com.thanksmister.iot.mqtt.alarmpanel.tasks.InstagramTask
import com.thanksmister.iot.mqtt.alarmpanel.tasks.NetworkTask
import kotlinx.android.synthetic.main.dialog_screen_saver.view.*
import retrofit2.Response
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class ScreenSaverView : RelativeLayout {

    private var task: InstagramTask? = null
    private var userName: String? = null
    private var fitToScreen: Boolean = false
    private var rotationHandler: Handler? = null
    private var timeHandler: Handler? = null
    private var picasso: Picasso? = null
    private var itemList: List<InstagramItem>? = null
    private var imageUrl: String? = null
    private var rotationInterval: Long = 0

    private var listener: ViewListener? = null
    private var saverContext: Context? = null

    private val delayRotationRunnable = object : Runnable {
        override fun run() {
            rotationHandler!!.removeCallbacks(this)
            startImageRotation()
        }
    }

    private val timeRunnable = object : Runnable {
        override fun run() {
            val currentTimeString = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault()).format(Date())
            screenSaverClock.text = currentTimeString
            if (timeHandler != null) {
                timeHandler!!.postDelayed(this, 1000)
            }
        }
    }

    interface ViewListener {
        fun onMotion()
    }

    constructor(context: Context) : super(context) {
        saverContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        saverContext = context
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (task != null) {
            task!!.cancel(true)
            task = null
        }

        if (picasso != null) {
            picasso!!.invalidate(imageUrl!!)
            picasso!!.cancelRequest(screenSaverImage)
            picasso = null
        }

        if (rotationHandler != null) {
            rotationHandler!!.removeCallbacks(delayRotationRunnable)
        }

        if (timeHandler != null) {
            timeHandler!!.removeCallbacks(timeRunnable)
        }
    }

    fun setListener(listener: ViewListener) {
        this.listener = listener
    }

    fun setScreenSaver(context: Context, useImageScreenSaver: Boolean, userName: String, fitToScreen: Boolean,
                       rotationIntervalMinutes: Int) {
        this.saverContext = context
        this.userName = userName
        this.fitToScreen = fitToScreen
        this.rotationInterval = (rotationIntervalMinutes * 1000).toLong() // convert to milliseconds

        if (useImageScreenSaver && !TextUtils.isEmpty(userName)) {
            screenSaverImage.setVisibility(View.VISIBLE)
            screenSaverClock.setVisibility(View.GONE)
            if (timeHandler != null) {
                timeHandler!!.removeCallbacks(timeRunnable)
            }
            startScreenSavor()
        } else { // use clock
            screenSaverImage.setVisibility(View.GONE)
            screenSaverClock.setVisibility(View.VISIBLE)
            timeHandler = Handler()
            timeHandler!!.postDelayed(timeRunnable, 10)
        }
    }

    private fun startScreenSavor() {
        if (itemList == null || itemList!!.isEmpty()) {
            fetchMediaData()
        } else {
            startImageRotation()
        }
    }

    private fun startImageRotation() {
        if (picasso == null) {
            picasso = Picasso.with(context)
        }
        if (itemList != null && !itemList!!.isEmpty()) {
            val min = 0
            val max = itemList!!.size - 1
            val random = Random().nextInt(max - min + 1) + min
            val instagramItem = itemList!![random]
            imageUrl = instagramItem.images.standardResolution.url
            if (fitToScreen) {
                picasso!!.load(imageUrl)
                        .placeholder(R.color.black)
                        .resize(screenSaverImage.getWidth(), screenSaverImage.getHeight())
                        .centerCrop()
                        .error(R.color.black)
                        .into(screenSaverImage)
            } else {
                picasso!!.load(imageUrl)
                        .placeholder(R.color.black)
                        .error(R.color.black)
                        .into(screenSaverImage)
            }
            if (rotationHandler == null) {
                rotationHandler = Handler()
            }
            rotationHandler!!.postDelayed(delayRotationRunnable, rotationInterval)
        }
    }

    private fun fetchMediaData() {
        if (task == null || task!!.isCancelled) {
            val api = InstagramApi()
            val fetcher = InstagramFetcher(api)
            task = InstagramTask(fetcher)
            task!!.setOnExceptionListener(object :   NetworkTask.OnExceptionListener {
                override fun onException(paramException: Exception) {
                    Timber.e("Instagram Exception: " + paramException.message)
                }
            })
            task!!.setOnCompleteListener(object : NetworkTask.OnCompleteListener<Response<InstagramResponse>> {
                override fun onComplete(paramResult: Response<InstagramResponse>) {
                    val instagramResponse = paramResult.body()
                    if (instagramResponse != null) {
                        itemList = instagramResponse.items
                        startImageRotation()
                    }
                }
            })
            task!!.execute(userName)
        }
    }
}