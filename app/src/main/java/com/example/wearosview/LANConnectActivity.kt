package com.example.wearosview

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wearosview.databinding.ActivityConnectBinding
import com.example.wearosview.layoutImpliment.BackArrowView
import com.example.wearosview.socketconnect.Command.CommandReceiver
import com.example.wearosview.socketconnect.Device
import com.example.wearosview.socketconnect.Communication.DeviceSearchResponser

class LANConnectActivity : Activity() {
    private lateinit var binding: ActivityConnectBinding
    private lateinit var btnReturn: BackArrowView
    private lateinit var connnectBtn: Button
    private lateinit var mark: ImageView
    var isListeningOpen:Boolean=false;

    var hostDevice: Device?=null
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
        connnectBtn?.setOnClickListener {
            if(isListeningOpen)
            {
                //停止响应搜索
                mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
                stopListen()
                isListeningOpen=false
                connnectBtn.setText("打开监听")
                //停止接受通信命令
                CommandReceiver.close()
                Toast.makeText(this, "已经关闭监听", Toast.LENGTH_SHORT).show()
            }
            else{
                //开始响应搜索
                startListen()
                isListeningOpen=true
                connnectBtn.setText("关闭应答")
                CommandReceiver.open()
                Toast.makeText(baseContext, "已经打开监听程序！", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onResume()
    {
        super.onResume()

        mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
        //开始接受通信命令
        CommandReceiver.start(object : CommandReceiver.CommandListener{
            override fun onReceive(command: String?) {
                commandResolver(command)
            }
        })
    }

    private fun commandResolver(demand:String?)
    {
        when(demand) {
            "SendWearMesg" -> {
                mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg4))
                val intent = Intent(baseContext, MainActivity::class.java)
                var hostIp = hostDevice!!.ip
                intent.putExtra("hostIp", hostIp)
                CommandReceiver.close()
                stopListen()
                isListeningOpen = false
                connnectBtn.setText("开始监听")
                startActivity(intent)
            }
            null -> {
            }
        }
    }
    override fun onStop() {
        super.onStop()
        //停止响应搜索
        mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg2))
        stopListen()
        isListeningOpen=false
        //停止接受通信命令
        CommandReceiver.close()

    }

    override fun onPause() {
        super.onPause()


    }

    override fun onDestroy() {
        super.onDestroy()
    }
    /**
     * 开始同步监听局域网中的host发出的搜索包
     */
    public fun startListen() {
        DeviceSearchResponser.open(object : DeviceSearchResponser.OnSearchListener{
            override fun onGetHost(device: Device?) {
                device?.let {
                    mark.setImageDrawable(resources.getDrawable(R.drawable.image_bg3))
                    hostDevice = it
                }
            }
        })

    }

    public fun stopListen() {
        DeviceSearchResponser.close()
    }
}