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

import android.os.Build

import com.google.android.things.pio.PeripheralManagerService

object BoardDefaults {
    private val DEVICE_EDISON_ARDUINO = "edison_arduino"
    private val DEVICE_EDISON = "edison"
    private val DEVICE_RPI3 = "rpi3"
    private val DEVICE_NXP = "imx6ul"
    private var sBoardVariant = ""

    /**
     * Return the preferred PWM port for each board.
     */
    val pwmPort: String
        get() {
            when (boardVariant) {
                DEVICE_EDISON_ARDUINO -> return "IO6"
                DEVICE_EDISON -> return "GP12"
                DEVICE_RPI3 -> return "PWM0"
                DEVICE_NXP -> return "PWM7"
                else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
            }
        }

    private // For the edison check the pin prefix
            // to always return Edison Breakout pin name when applicable.
    val boardVariant: String
        get() {
            if (sBoardVariant.isNotEmpty()) {
                return sBoardVariant
            }

            sBoardVariant = Build.DEVICE

            if (sBoardVariant == DEVICE_EDISON) {
                val pioService = PeripheralManagerService()
                val gpioList = pioService.gpioList

                if (gpioList.size != 0) {
                    val pin = gpioList[0]

                    if (pin.startsWith("IO")) {
                        sBoardVariant = DEVICE_EDISON_ARDUINO
                    }
                }
            }

            return sBoardVariant
        }
}
