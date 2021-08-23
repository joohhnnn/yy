package com.txznet.feedback.volley;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class SSLSocketFactoryEx extends SSLSocketFactory {


    SSLContext sslContext = SSLContext.getInstance("TLS");


    public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException,

            KeyManagementException, KeyStoreException, UnrecoverableKeyException {

        super(truststore);

        // set up a TrustManager that trusts everything

        TrustManager tm = new X509TrustManager() {


            public X509Certificate[] getAcceptedIssuers() {

                //return new X509Certificate[]{};

                return null;

            }


            @Override

            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

                // TODO Auto-generated method stub


            }


            @Override

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                // TODO Auto-generated method stub


            }

        };


        sslContext.init(null, new TrustManager[]{tm}, new java.security.SecureRandom());

    }


    @Override

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)

            throws IOException, UnknownHostException {

        injectHostname(socket, host);

        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);

    }


    @Override

    public Socket createSocket() throws IOException {

        return sslContext.getSocketFactory().createSocket();

    }


    private void injectHostname(Socket socket, String host) {

        try {

            Field field = InetAddress.class.getDeclaredField("hostName");

            field.setAccessible(true);

            field.set(socket.getInetAddress(), host);

        } catch (Exception ignored) {

        }

    }

}