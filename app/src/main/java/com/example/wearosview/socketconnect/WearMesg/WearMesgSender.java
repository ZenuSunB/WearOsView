package com.example.wearosview.socketconnect.WearMesg;

import com.example.wearosview.socketconnect.CommunicationKey;
import com.example.wearosview.socketconnect.RemoteConst;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WearMesgSender {
    private static ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(2, 2, 1,
                    TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new SendWearMesgThreadFactory(),
                    new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            throw new RejectedExecutionException();
                        }
                    });


    public static void addCommand(final WearMessage mesg){
        addTask(new WearMesgRunnable(mesg));
    }

    private static void addTask(WearMesgRunnable runnable){
        try{
            threadPool.execute(runnable);
        }catch (RejectedExecutionException e){
            e.printStackTrace();
            if(runnable.mesg.getCallback()!=null){
                runnable.mesg.getCallback().onError("mesg is rejected");
            }
        }
    }
    private static class WearMesgRunnable implements Runnable{

        WearMessage mesg;
        public WearMesgRunnable(WearMessage mesg){
            this.mesg = mesg;
        }
        @Override
        public void run() {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(mesg.getDestIp(), RemoteConst.WEARMESG_RECEIVE_PORT));
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                //发送命令内容
                os.write(mesg.getContent());
                os.write(CommunicationKey.EOF.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
