package com.example.wearosview.bluetoochconnect;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ConnectBroadcastReceiver extends BroadcastReceiver {
    public interface ConnectBroadcastReceiverListener{
        void on_connect(BluetoothDevice device);
        void on_disconnect(BluetoothDevice device);
        void on_bond(BluetoothDevice device);
        void on_pair(BluetoothDevice device);
    }
    ConnectBroadcastReceiverListener listener;
    public ConnectBroadcastReceiver(ConnectBroadcastReceiverListener listener)
    {
        this.listener=listener;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BluetoothConnect", "intent=" + intent.toString());
        String action = intent.getAction();
        if (action != null) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            try {
                switch (action) {
                    case BluetoothDevice.ACTION_ACL_CONNECTED: {
                        Log.d("BluetoothConnect", "已连接");
                        if(listener!=null)
                            listener.on_connect(device);
                        break;
                    }
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                        Log.d("BluetoothConnect", "断开连接");
                        if(listener!=null)
                            listener.on_disconnect(device);
                        break;
                    }
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        Log.d("BluetoothConnect", "配对状态改变");
                        if (device != null) {
                            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                                Log.d("BluetoothConnect", "配对失败");
                            } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                                if(listener!=null)
                                    listener.on_bond(device);
                                Log.d("BluetoothConnect", "配对中");
                            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {

                                Log.d("BluetoothConnect", "配对成功");

                            }
                        }
                        break;
                    case BluetoothDevice.ACTION_PAIRING_REQUEST: {
                        //配对的验证码，如果是-1侧不需要验证码
                        int key = intent.getExtras().getInt(BluetoothDevice.EXTRA_PAIRING_KEY, -1);
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            try {
                                //1.确认配对
                                boolean success = false;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    if (key != -1) {
                                        if(listener!=null)
                                            listener.on_pair(device);
//                                        success = device.setPin(String.valueOf(key).getBytes());
                                        success = device.setPairingConfirmation(true);
                                    } else {
                                        if(listener!=null)
                                            listener.on_pair(device);
                                        //需要系统权限，如果没有系统权限，就点击弹窗上的按钮配对吧
                                        success = device.setPairingConfirmation(true);
                                    }
                                }
                                Log.d("BluetoothConnect", "key=" + key + "  bond=" + success);
                                //如果没有将广播终止，则会出现一个一闪而过的配对框。
                                abortBroadcast();
                            } catch (Exception e) {
                                Log.e("BluetoothConnect", "反射异常：" + e);
                            }
                        }
                        break;
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
