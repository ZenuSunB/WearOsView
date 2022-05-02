package com.example.wearosview

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wearosview.bluetoochconnect.BluetoothMesg
import com.example.wearosview.bluetoochconnect.BluetoothMesgReceiver
import com.example.wearosview.bluetoochconnect.BluetoothMesgSender
import com.example.wearosview.databinding.ActivityMainBinding
import com.example.wearosview.socketconnect.Device
import com.example.wearosview.socketconnect.WearMesg.WearMesgSender
import com.example.wearosview.socketconnect.WearMesg.WearMessage
import java.util.*

class BluetoothMainActivity  : Activity() {
    @Volatile
    var heart_beat_ratio=100f;
    private lateinit var sensorManager: SensorManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var textView: TextView
    private lateinit var heartBeatSensor: Sensor
    @Volatile
    private var isAlive:Boolean=true
    //定时器设置
    private var NewWearMesgGenerator: Timer?=null
    //定时器事件设置
    private var timerTask: TimerTask?=object : TimerTask(){
        override fun run() {
            try {
                if (!isAlive)
                    cancel()
                sendBluetoothMesg(heart_beat_ratio.toString())

            }
            catch (e:Throwable)
            {
                e.printStackTrace()
            }
        }
    }
    private var heartBeatSensorlistener: SensorEventListener =
        object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    textView.setText(it.values[0].toString())
                    heart_beat_ratio=it.values[0]
                }
            } }

    fun sendBluetoothMesg(str:String) {
        val bluetoothMesg = BluetoothMesg(str.toByteArray())
        BluetoothMesg.setBluetoothDevice(BluetoothMesg.getBluetoothDevice())
        BluetoothMesgSender.addBluetoothMesg(bluetoothMesg)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Ask for permision
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BODY_SENSORS), 1);
        }
        else {
//         Permission has already been granted
        }

        textView=findViewById(R.id.heartBeatRatio)
        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartBeatSensor=sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        heartBeatSensor?.let {
            sensorManager.registerListener(heartBeatSensorlistener,it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        BluetoothMesgReceiver.start(object: BluetoothMesgReceiver.BluetoothMesgReceiverListener{
            override fun onMesgReceiver(meg: String?,device: BluetoothDevice) {
                when(meg)
                {
                    "startSendHeatBeatRatio"->
                    {
                        NewWearMesgGenerator = Timer()
                        NewWearMesgGenerator?.schedule(timerTask,500,100)
                    }
                    "finish"->
                    {
                        BluetoothMesgReceiver.close()
                        finish()
                    }
                }
            }
        })


    }
    override fun onResume()
    {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()


    }

    override fun onPause() {
        super.onPause()

    }
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(heartBeatSensorlistener)
        timerTask?.cancel()
        timerTask=null
        NewWearMesgGenerator?.cancel()
        NewWearMesgGenerator=null
        isAlive=false

    }
}