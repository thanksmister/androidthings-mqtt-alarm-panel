/*
 * Copyright (c) 2018. ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.modules

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

/**
 * Modified by Michael Ritchie 2018
 */
class MotionSensor(private val motionListener: MotionListener, motionSensorPinNumber: String) : LifecycleObserver {

    private val motionSensorGpioPin: Gpio = PeripheralManagerService().openGpio(motionSensorPinNumber)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        //Receive data from the sensor - DIRECTION_IN
        motionSensorGpioPin.setDirection(Gpio.DIRECTION_IN)
        //High voltage means movement has been detected
        motionSensorGpioPin.setActiveType(Gpio.ACTIVE_HIGH)
        //We don't want to receive both low and high triggers so EDGE_RISING
        motionSensorGpioPin.setEdgeTriggerType(Gpio.EDGE_BOTH)
        motionSensorGpioPin.registerGpioCallback(gpiCallback)
    }

    private val gpiCallback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio?): Boolean {
            if (gpio != null) {
                if (gpio.value) {
                    motionListener.onMotionDetected()
                } else {
                    motionListener.onMotionStopped()
                }
            }
            return true
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        motionSensorGpioPin.close()
        motionSensorGpioPin.unregisterGpioCallback(gpiCallback)
    }

    interface MotionListener {
        fun onMotionDetected()
        fun onMotionStopped()
    }

    companion object {
        val MOTION_SENSOR_GPIO_PIN = "BCM27"
    }
}