package com.txznet.txz.component.asr.txzasr;

import com.google.protobuf.nano.MessageNano;
import com.txz.asr.udp.TxzAsrUdpProtosV0.TxzAsrUdpInfo;
import com.txz.ui.equipment.UiEquipment.UdpAsrServerConfig;

public abstract class UdpClient {
	private native static long nativeCreate(byte[] globalOption, byte[] option,
			Object listener);

	private native static void nativeUpdate(long session, byte[] option);

	private native static void nativePushData(long session, byte[] data,
			int offset, int size);

	private native static void nativeCancel(long session);

	private native static void nativeComplete(long session);

	private native static void nativeDestroy(long session);

	// ////////////////////////////////////////////////////////////////////////////////

	private static interface EventListener {
		public void onError(int err);

		public void onCancel();

		public void onResultData(boolean last, byte[] data);
	}

	// ////////////////////////////////////////////////////////////////////////////////

	private long mSessionId;
	private EventListener mListener = new EventListener() {
		@Override
		public void onResultData(boolean last, byte[] data) {
			UdpClient.this.onResultData(last, data);

			if (last) {
				release();
			}
		}

		@Override
		public void onCancel() {
			UdpClient.this.onCancel();
			release();
		}

		@Override
		public void onError(int err) {
			UdpClient.this.onError(err);
			release();
		}
	};

	private synchronized/* write */void release() {
		nativeDestroy(mSessionId);
		mSessionId = 0;
	}

	// ////////////////////////////////////////////////////////////////////////////////

	public UdpClient(UdpAsrServerConfig cfg, TxzAsrUdpInfo info) {
		mSessionId = nativeCreate(MessageNano.toByteArray(cfg),
				MessageNano.toByteArray(info), mListener);
	}

	public synchronized/* read */void update(TxzAsrUdpInfo info) {
		if (mSessionId == 0)
			return;
		nativeUpdate(mSessionId, MessageNano.toByteArray(info));
	}

	public synchronized/* read */void pushData(byte[] data, int offset,
			int size) {
		if (mSessionId == 0)
			return;
		nativePushData(mSessionId, data, offset, size);
	}

	public synchronized/* read */void pushData(byte[] data) {
		if (mSessionId == 0)
			return;
		nativePushData(mSessionId, data, 0, data.length);
	}

	public synchronized/* read */void cancel() {
		if (mSessionId == 0)
			return;
		nativeCancel(mSessionId);
	}

	public synchronized/* read */void complete() {
		if (mSessionId == 0)
			return;
		nativeComplete(mSessionId);
	}

	// ////////////////////////////////////////////////////////////////////////////////

	public abstract void onError(int err);

	public void onCancel() {
		// default do nothing
	}

	public abstract void onResultData(boolean last, byte[] data);
}
