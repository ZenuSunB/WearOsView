package com.example.wearosview.bluetoochconnect;

import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;

import com.example.wearosview.socketconnect.Command.SendCommandThreadFactory;
import com.example.wearosview.socketconnect.CommunicationKey;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BluetoothMesgSender {
    static UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(3, 4, 60,
                    TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new SendCommandThreadFactory(),
                    new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            throw new RejectedExecutionException();
                        }
                    });
    public static void addBluetoothMesg(final BluetoothMesg bluetoothMesg){
        addTask(new BluetoothMesgRunnable(bluetoothMesg));
    }

    private static void addTask(BluetoothMesgRunnable runnable){
        try{
            threadPool.execute(runnable);
        }catch (RejectedExecutionException e){
            e.printStackTrace();
        }
    }
    private static class BluetoothMesgRunnable implements Runnable{

        BluetoothMesg bluetoothMesg;
        public BluetoothMesgRunnable(BluetoothMesg bluetoothMesg){
            this.bluetoothMesg = bluetoothMesg;
        }
        @Override
        public void run() {
            BluetoothSocket  socket=null;
            try {
                socket  = BluetoothMesg.getBluetoothDevice().createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                OutputStream os = socket.getOutputStream();
                if(os!=null) {
                    //发送命令内容
                    os.write(bluetoothMesg.getContent());
                    os.write(CommunicationKey.EOF.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }catch (SecurityException e){
                e.printStackTrace();
            }catch (NullPointerException e) {
                e.printStackTrace();
            }
            finally {
                try {
//                    SystemClock.sleep(500);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
