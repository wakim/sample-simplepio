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
import com.google.android.things.pio.PeripheralManagerService
import com.google.android.things.pio.Pwm
import android.os.Bundle
import android.os.Handler
import android.util.Log

import java.io.IOException

/**
 * Sample usage of the PWM API that changes the PWM pulse width at a fixed interval defined in
 * [.INTERVAL_BETWEEN_STEPS_MS].

 */
class PWMActivity : Activity() {

    private val mHandler = Handler()
    private lateinit var mPwm: Pwm
    private var mIsPulseIncreasing = true
    private var mActivePulseDuration: Double = 0.toDouble()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting PWMActivity")

        val service = PeripheralManagerService()

        try {
            val pinName = BoardDefaults.pwmPort
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS

            mPwm = service.openPwm(pinName)

            // Always set frequency and initial duty cycle before enabling PWM
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS)
            mPwm.setPwmDutyCycle(mActivePulseDuration)
            mPwm.setEnabled(true)

            // Post a Runnable that continuously change PWM pulse width, effectively changing the
            // servo position
            Log.d(TAG, "Start changing PWM pulse")
            mHandler.post(mChangePWMRunnable)
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove pending Runnable from the handler.
        mHandler.removeCallbacks(mChangePWMRunnable)
        // Close the PWM port.
        Log.i(TAG, "Closing port")

        try {
            mPwm.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }
    }

    private val mChangePWMRunnable = object : Runnable {
        override fun run() {
            // Change the duration of the active PWM pulse, but keep it between the minimum and
            // maximum limits.
            // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
            // will bounce from MIN to MAX.
            if (mIsPulseIncreasing) {
                mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS
            } else {
                mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS
            }

            // Bounce mActivePulseDuration back from the limits
            if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS
                mIsPulseIncreasing = !mIsPulseIncreasing
            } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS
                mIsPulseIncreasing = !mIsPulseIncreasing
            }

            Log.d(TAG, "Changing PWM active pulse duration to $mActivePulseDuration ms")

            try {
                // Duty cycle is the percentage of active (on) pulse over the total duration of the
                // PWM pulse
                mPwm.setPwmDutyCycle(100 * mActivePulseDuration / PULSE_PERIOD_MS)

                // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS.toLong())
            } catch (e: IOException) {
                Log.e(TAG, "Error on PeripheralIO API", e)
            }

        }
    }

    companion object {
        private val TAG = PWMActivity::class.java.simpleName

        // Parameters of the servo PWM
        private val MIN_ACTIVE_PULSE_DURATION_MS = 1.0
        private val MAX_ACTIVE_PULSE_DURATION_MS = 2.0
        private val PULSE_PERIOD_MS = 20.0  // Frequency of 50Hz (1000/20)

        // Parameters for the servo movement over time
        private val PULSE_CHANGE_PER_STEP_MS = 0.2
        private val INTERVAL_BETWEEN_STEPS_MS = 1000
    }
}
