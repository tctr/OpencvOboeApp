package com.tctr.opencvoboeapp


import androidx.appcompat.app.AppCompatActivity

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle
import android.os.Build;

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Float.max


class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    var srcBitmap: Bitmap? = null
    var dstBitmap: Bitmap? = null

    private native long startEngine(int[] cpuIds);
    private native void stopEngine(long engineHandle);
    private static native void native_setDefaultStreamValues(int sampleRate, int framesPerBurst);
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setDefaultStreamValues(this);

        // Load the original image
        srcBitmap = BitmapFactory.decodeResource(this.resources, R.drawable.hypo2)

        // Create and display dstBitmap in image view, we will keep updating
        // dstBitmap and the changes will be displayed on screen
        dstBitmap = srcBitmap!!.copy(srcBitmap!!.config, true)
        imageView.setImageBitmap(dstBitmap)

        sldSigma.setOnSeekBarChangeListener(this)
	
	// Minimal Oboe : Let our AudioPlayer observe lifecycle events for the application so when it goes into the
        // background we can stop audio playback.
        ProcessLifecycleOwner.get().lifecycle.addObserver(AudioPlayer)
    }

    fun doBlur() {
        // The SeekBar range is 0-100 convert it to 0.1-10
        val sigma = max(0.1F, sldSigma.progress / 10F)

        // This is the actual call to the blur method inside native-lib.cpp
        this.blur(srcBitmap!!, dstBitmap!!, sigma)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        this.doBlur()
    }

    fun btnFlip_click(view: View) {
        // This is the actual call to the blur method inside native-lib.cpp
        // note we flip srcBitmap (which is not displayed) and then call doBlur which will
        // eventually update dstBitmap (and which is displayed)
        this.flip(srcBitmap!!, srcBitmap!!)
        this.doBlur()
    }

    fun btnAuStart_click(view: View) {
        setPlaybackEnabled(true) 
    }
    
    
    fun btnAuStop_click(view: View) {
        setPlaybackEnabled(false) 
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun blur(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    external fun flip(bitmapIn: Bitmap, bitmapOut: Bitmap)

    companion object {

        // ** IMPORTANT ** used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
