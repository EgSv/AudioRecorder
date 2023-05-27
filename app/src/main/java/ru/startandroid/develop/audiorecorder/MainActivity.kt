package ru.startandroid.develop.audiorecorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.OnRecordPositionUpdateListener
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    private val TAG = "myLogs"
    private var myBufferSize = 8192
    private var audioRecord: AudioRecord? = null
    private var isReading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createAudioRecorder()
        Log.d(TAG, "init state = " + audioRecord!!.state)
    }

    private fun createAudioRecorder() {
        val sampleRate = 8000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minInternalBufferSize = AudioRecord.getMinBufferSize(sampleRate,
            channelConfig, audioFormat)
        val internalBufferSize = minInternalBufferSize * 4
        Log.d(TAG, "minInternalBufferSize = " + minInternalBufferSize
                + ", internalBufferSize = " + internalBufferSize
                + ", myBufferSize = " + myBufferSize)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
            sampleRate, channelConfig, audioFormat, internalBufferSize)
        audioRecord!!.positionNotificationPeriod = 1000
        audioRecord!!.notificationMarkerPosition = 10000
        audioRecord!!
            .setRecordPositionUpdateListener(object : OnRecordPositionUpdateListener {
                override fun onPeriodicNotification(recorder: AudioRecord) {
                    Log.d(TAG, "onPeriodicNotification")
                }

                override fun onMarkerReached(recorder: AudioRecord) {
                    Log.d(TAG, "onMarkerReached")
                    isReading = false
                }
            })
    }

    fun recordStart(v: View?) {
        Log.d(TAG, "record start")
        audioRecord!!.startRecording()
        val recordingState = audioRecord!!.recordingState
        Log.d(TAG, "recordingState = $recordingState")
    }

    fun recordStop(v: View?) {
        Log.d(TAG, "record stop")
        audioRecord!!.stop()
    }

    fun readStart(v: View?) {
        Log.d(TAG, "read start")
        isReading = true
        Thread(Runnable {
            if (audioRecord == null) return@Runnable
            val myBuffer = ByteArray(myBufferSize)
            var readCount = 0
            var totalCount = 0
            while (isReading) {
                readCount = audioRecord!!.read(myBuffer, 0, myBufferSize)
                totalCount += readCount
                Log.d(TAG, "readCount = " + readCount + ", totalCount = "
                        + totalCount)
            }
        }).start()
    }

    fun readStop(v: View?) {
        Log.d(TAG, "read stop")
        isReading = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isReading = false
        if (audioRecord != null) {
            audioRecord!!.release()
        }
    }
}