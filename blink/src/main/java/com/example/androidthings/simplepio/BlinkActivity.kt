package com.example.androidthings.simplepio

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import kotlinx.android.synthetic.main.activity_blink.*
import java.io.IOException

class BlinkActivity : Activity() {
    companion object {
        private val TAG = BlinkActivity::class.java.simpleName
        private val INTERVAL_BETWEEN_BLINKS_MS = 1000
    }

    private val mHandler = Handler()
    private lateinit var mLedGpio: Gpio

    private lateinit var mBlinkRunnable: () -> Unit

    init {
        mBlinkRunnable = {
            try {
                // Toggle the GPIO state
                mLedGpio.value = !mLedGpio.value
                Log.d(TAG, "State set to " + mLedGpio.value)

                button.isChecked = mLedGpio.value

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS.toLong())
            } catch (e: IOException) {
                Log.e(TAG, "Error on PeripheralIO API", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blink)

        Log.i(TAG, "Starting BlinkActivity")

        val service = PeripheralManagerService()

        try {
            val pinName = BoardDefaults.gpioForLED

            mLedGpio = service.openGpio(pinName)
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            Log.i(TAG, "Start blinking LED GPIO pin")

            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            mHandler.post(mBlinkRunnable)
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable)
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin")

        try {
            mLedGpio.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }
    }
}