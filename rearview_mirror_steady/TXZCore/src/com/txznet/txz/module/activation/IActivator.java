package com.txznet.txz.module.activation;

public interface IActivator {
	public boolean checkPermission(String sEncryptedDevSn);
	public boolean isSupportLocalActivation();
}
