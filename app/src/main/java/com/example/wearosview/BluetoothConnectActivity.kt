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
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wearosview.bluetoochconnect.BluetoothMesg
import com.example.wearosview.bluetoochconnect.BluetoothMesgReceiver
import com.example.wearosview.bluetoochconnect.ConnectBroadcastReceiver
import com.example.wearosview.databinding.ActivityConnectBinding
import com.example.wearosview.layoutImpliment.BackArrowView
import com.example.wearosview.socketconnect.Command.CommandReceiver


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

        pairBtn?.setOnClickListener {
            if(isListeningOpen)
            {
                mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
                isListeningOpen=false
                pairBtn.setText("打开配对")
                BluetoothMesgReceiver.close()
                Toast.makeText(this, "已经关闭配对", Toast.LENGTH_SHORT).show()
            }
            else{
                if (mAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                }
                BluetoothMesgReceiver.open()
                isListeningOpen=true
                pairBtn.setText("关闭配对")
                Toast.makeText(baseContext, "已经打开配对！", Toast.LENGTH_SHORT).show()

            }
        }
        connnectBtn?.setOnClickListener {
            if(isLConnectingOpen)
            {
                mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
                isLConnectingOpen=false
                connnectBtn.setText("打开连接")
                BluetoothMesgReceiver.close()
                Toast.makeText(this, "已经关闭连接", Toast.LENGTH_SHORT).show()
            }
            else{
                BluetoothMesgReceiver.open()
                isLConnectingOpen=true
                connnectBtn.setText("关闭连接")
                Toast.makeText(baseContext, "已经打开连接！", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onResume()
    {
        super.onResume()
        registerConnectBroadcast()
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
                            //                      BluetoothMesgReceiver.close()
                            connnectBtn.setText("打开连接")
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
        isListeningOpen=false
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(connectBroadcastReceiver);
        connectBroadcastReceiver=null

    }

    override fun onDestroy() {
        super.onDestroy()


    }

}