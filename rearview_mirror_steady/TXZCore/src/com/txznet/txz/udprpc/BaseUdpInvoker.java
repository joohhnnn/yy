package com.txznet.txz.udprpc;

import com.txznet.comm.remote.udprpc.UdpDataFactory;

public abstract class BaseUdpInvoker {
	public abstract UdpDataFactory.UdpData onInvoke(UdpDataFactory.UdpData udpData);
}
