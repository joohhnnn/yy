package com.txznet.fm.factory;

import com.txznet.fm.factory.abstraction.AFactory;
import com.txznet.fm.receiver.AudioReceiver;
import com.txznet.fm.receiver.DirectReceiver;
import com.txznet.fm.receiver.MusicReceiver;
import com.txznet.fm.receiver.interfase.IReceiver;

/**
 * 根据不同的类型产生不同的接收者，电台，音乐，直播等
 * 
 * @author ASUS User
 *
 */
public class ReceiverFactory extends AFactory {

	public static IReceiver createCommand(int type) {

		switch (type) {
		case 1:
			return new MusicReceiver();
		case 2:
			return new AudioReceiver();
		case 3:
			return new DirectReceiver();
		default:
			break;
		}

		return null;
	}

}
