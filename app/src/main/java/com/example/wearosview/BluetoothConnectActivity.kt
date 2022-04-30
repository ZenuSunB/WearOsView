package com.example.wearosview

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wearosview.bluetoochconnect.ConnectBroadcastReceiver
import com.example.wearosview.databinding.ActivityConnectBinding
import com.example.wearosview.layoutImpliment.BackArrowView


class BluetoothConnectActivity : Activity() {
    private lateinit var binding: ActivityConnectBinding
    private lateinit var btnReturn: BackArrowView
    private lateinit var connnectBtn: Button
    private lateinit var mark: ImageView
    var isListeningOpen:Boolean=false;
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
        connnectBtn=findViewById(R.id.connectBtn)
        btnReturn?.setOnClickListener {
            finish()
        }

        registerConnectBroadcast()
        connnectBtn?.setOnClickListener {
            if(isListeningOpen)
            {
                mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
                isListeningOpen=false
                connnectBtn.setText("打开监听")
//                application.unregisterReceiver(connectBroadcastReceiver);
                Toast.makeText(this, "已经关闭监听", Toast.LENGTH_SHORT).show()
            }
            else{
                if (mAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                }
                isListeningOpen=true
                connnectBtn.setText("关闭应答")
                Toast.makeText(baseContext, "已经打开监听程序！", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onResume()
    {
        super.onResume()
//        mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
    }

    /**
     * 监听配对状态
     */
    @Synchronized
    fun registerConnectBroadcast() {
        val application: Application = this.application
        //注册蓝牙开关状态广播接收者
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
            //已连接
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            //连接断开
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            //配对状态改变
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            //配对请求，也就系统弹窗提示是否进行配对
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            application.registerReceiver(connectBroadcastReceiver, filter)
        }
    }


    override fun onStop() {
        super.onStop()
        mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
        unregisterReceiver(connectBroadcastReceiver);
        isListeningOpen=false
    }

    override fun onPause() {
        super.onPause()


    }

    override fun onDestroy() {
        super.onDestroy()
    }

}