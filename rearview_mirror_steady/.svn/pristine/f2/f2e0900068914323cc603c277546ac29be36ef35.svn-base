package com.txznet.audio.server.dns;

import com.txznet.audio.server.response.DNSUtils;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


public class DnsState {

    Sender sender;
    PipedInputStream in = null;
    String mDomain = "";

    public DnsState(String domain) {
        mDomain = domain;
        sender = new Sender();
        in = new PipedInputStream();
        PipedOutputStream po = sender.getPipedOutputStream();
        try {
            in.connect(po);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        new Thread(sender).start();
    }

    /**
     * 阻塞操作
     */
    public void read(byte[] bys) {
        try {
            in.read(bys);
            in.close();
            in = null;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cancel() {
        try {
            if (in != null) {
                in.close();
                in = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class Sender implements Runnable {
        PipedOutputStream out = null;

        public PipedOutputStream getPipedOutputStream() {
            out = new PipedOutputStream();
            return out;
        }

        @Override
        public void run() {

            try {
                String ip = DNSUtils.getIP(mDomain, 0);
                out.write(ip.getBytes());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try {
                out.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }


    }

}
