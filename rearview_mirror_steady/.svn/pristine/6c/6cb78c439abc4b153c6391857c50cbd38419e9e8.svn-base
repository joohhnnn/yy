package com.txznet.comm.remote.udprpc;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.udprpc.UdpConfiger.UdpAddress;
import com.txznet.comm.remote.udprpc.UdpDataFactory.UdpData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;

/**
 * Created by Terry on 2017/10/11.
 */
public class UdpClient {
    private static final String TAG = "UdpClient ";

    private DatagramSocket mSocketClient;
    private int mPort = UdpConfiger.getInstance().getClientPort(GlobalContext.get().getPackageName());
    private int mInitCount = 0;

    public int init() {
        if (mSocketClient == null) {
            while (true) {
                try {
                    mSocketClient = new DatagramSocket();
                    mSocketClient.setSoTimeout(UdpConfiger.TIME_OUT_CLIENT_RECV);
                    mSocketClient.setSendBufferSize(1024 * 1024);
                    LogUtil.logd(TAG + " sendBuffer:" + mSocketClient.getSendBufferSize());
                    return mSocketClient.getPort();
                } catch (SocketException e) {
                    Log.e(TAG,"create DatagramSocket exception , need network permission");
                    mInitCount++;
                    if (mInitCount > 5) {
                        return -2;
                    }
                }
            }
        }
        return mPort;
    }


	public UdpData sendInvoke(UdpData udpData, UdpAddress targetAddr) {
		if (udpData.invokeType == UdpData.INVOKE_SYNC) {
			return sendInvokeSync(UdpDataFactory.getTransferData(udpData), targetAddr);
		} else {
			return sendInvoke(UdpDataFactory.getTransferData(udpData), targetAddr);
		}
	}
    

    public UdpData sendInvokeSync(byte[] transferData,UdpAddress targetAddr) {
        InetAddress local = null;
        try {
            local = InetAddress.getByName(targetAddr.host);
            DatagramPacket dpSend = new DatagramPacket(transferData, transferData.length
                    , local, targetAddr.port);
            mSocketClient.send(dpSend);
            byte[] buffer = new byte[UdpConfiger.getInstance().getTransferLength()];
            DatagramPacket dpRecv = new DatagramPacket(buffer, buffer.length);
            mSocketClient.receive(dpRecv);
            UdpData udpData = UdpDataFactory.getUdpData(dpRecv.getData());
            return udpData;
        } catch (UnknownHostException e) {
            Log.e(TAG,"sendInvokeSync  UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG,"sendInvokeSync  IOException");
        }
        return null;
    }

    public UdpData sendInvoke(final byte[] transferData,final UdpAddress targetAddr) {
        try {
            InetAddress local = InetAddress.getByName(targetAddr.host);
            DatagramPacket dpSend = new DatagramPacket(transferData, transferData.length
                    , local, targetAddr.port);
            mSocketClient.send(dpSend);
        } catch (UnknownHostException e) {
            Log.e(TAG," sendInvoke UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG," sendInvoke IOException");
        }
        return null;
    }
}
