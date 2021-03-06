package com.example.wearosview

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ClipData.newIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wearosview.bluetoochconnect.BluetoothMesg
import com.example.wearosview.bluetoochconnect.BluetoothMesgReceiver
import com.example.wearosview.bluetoochconnect.ConnectBroadcastReceiver
import com.example.wearosview.databinding.ActivityConnectBinding
import com.example.wearosview.layoutImpliment.BackArrowView


class BluetoothConnectActivity : Activity() {
    private lateinit var binding: ActivityConnectBinding
    private lateinit var btnReturn: BackArrowView
    private lateinit var connnectBtn: Button
    private lateinit var pairBtn: Button
    private lateinit var mark: ImageView
    var isListeningOpen:Boolean=false;
    var isLConnectingOpen:Boolean=false;
    var hostDevices: MutableMap<String, BluetoothDevice> = mutableMapOf()

    private var mAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var connectBroadcastReceiver: ConnectBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
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
        mark=findViewById(R.id.mark)
        btnReturn=findViewById(R.id.back_arrow)
        connnectBtn=findViewById(R.id.connect2Btn)
        pairBtn=findViewById(R.id.connectBtn)
        btnReturn?.setOnClickListener {
            finish()
        }
        BluetoothMesg.setBluetoothAdapter(mAdapter)
        registerConnectBroadcast()
        pairBtn?.setOnClickListener {
            if(isListeningOpen)
            {
                mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
                isListeningOpen=false
                pairBtn.setText("????????????")
                BluetoothMesgReceiver.close()
                Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show()
            }
            else{
                if (mAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                }
                BluetoothMesgReceiver.open()
                isListeningOpen=true
                pairBtn.setText("????????????")
                Toast.makeText(baseContext, "?????????????????????", Toast.LENGTH_SHORT).show()

            }
        }
        connnectBtn?.setOnClickListener {
            if(isLConnectingOpen)
            {
                mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
                isLConnectingOpen=false
                connnectBtn.setText("????????????")
                BluetoothMesgReceiver.close()
                Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show()
            }
            else{
                BluetoothMesgReceiver.open()
                isLConnectingOpen=true
                connnectBtn.setText("????????????")
                Toast.makeText(baseContext, "?????????????????????", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onResume()
    {
        super.onResume()
        BluetoothMesgReceiver.start(object:BluetoothMesgReceiver.BluetoothMesgReceiverListener{
            override fun onMesgReceiver(meg: String?,device: BluetoothDevice) {
                when(meg)
                {
                    "connect"->
                    {
                        mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg5))
                        BluetoothMesg.setBluetoothDevice(device)
                        runOnUiThread {
                            mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
                            val intent = Intent(baseContext, BluetoothMainActivity::class.java)
                            connnectBtn.setText("????????????")
                            isLConnectingOpen = false;
                            BluetoothMesgReceiver.close()
                            startActivity(intent)
                        }
                    }
                }
            }
        })
//        mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
    }



    /**
     * ??????????????????
     */
    @Synchronized
    fun registerConnectBroadcast() {
        val application: Application = this.application
        //???????????????????????????????????????
        if (connectBroadcastReceiver == null && application != null) {
            connectBroadcastReceiver = ConnectBroadcastReceiver(object :
                ConnectBroadcastReceiver.ConnectBroadcastReceiverListener {
                override fun on_connect(device: BluetoothDevice?) {
                    device?.let {
                        try {
                            hostDevices.put(device.name, device)
                            mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg3))
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun on_disconnect(device: BluetoothDevice?) {
                    device?.let {
                        try {
                            hostDevices.remove(it.name)
                            mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg4))
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun on_bond(device: BluetoothDevice?) {
                    device?.let {
                        try {
                            mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg3))
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun on_pair(device: BluetoothDevice?) {
                    device?.let {
                        try {
                            mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg3))

                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
            val filter = IntentFilter()
            //?????????
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            //????????????
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            //??????????????????
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            //?????????????????????????????????????????????????????????
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            application.registerReceiver(connectBroadcastReceiver, filter)
        }
    }


    override fun onStop() {
        super.onStop()
        mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
        isListeningOpen=false
    }

    override fun onPause() {
        super.onPause()


    }

    override fun onDestroy() {
        super.onDestroy()
        if(connectBroadcastReceiver!=null) {
            unregisterReceiver(connectBroadcastReceiver);
            connectBroadcastReceiver = null
        }
    }

}