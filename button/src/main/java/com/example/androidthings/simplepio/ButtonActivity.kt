/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.simplepio

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import java.io.IOException

/**
 * Sample usage of the Gpio API that logs when a button is pressed.

 */
class ButtonActivity : Activity() {

    private lateinit var mButtonGpio: Gpio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting ButtonActivity")

        val service = PeripheralManagerService()

        try {
            val pinName = BoardDefaults.gpioForButton

            mButtonGpio = service.openGpio(pinName)
            mButtonGpio.setDirection(Gpio.DIRECTION_IN)
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING)
            mButtonGpio.registerGpioCallback(object : GpioCallback() {
                override fun onGpioEdge(gpio: Gpio?): Boolean {
                    Log.i(TAG, "GPIO changed, button pressed")
                    // Return true to continue listening to events
                    return true
                }
            })
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        // Close the Gpio pin
        Log.i(TAG, "Closing Button GPIO pin")

        try {
            mButtonGpio.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }
    }

    companion object {
        private val TAG = ButtonActivity::class.java.simpleName
    }
}
