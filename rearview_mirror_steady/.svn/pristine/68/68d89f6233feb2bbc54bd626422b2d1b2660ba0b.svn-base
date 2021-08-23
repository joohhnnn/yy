package com.txznet.fm.factory;

import com.txznet.fm.bean.VersionInfo;
import com.txznet.fm.command.PlayCommand;
import com.txznet.fm.command.interfase.ICommand;
import com.txznet.fm.factory.abstraction.AFactory;
import com.txznet.fm.receiver.AudioReceiver;

public class PlayCommandFactory extends AFactory {
	public static ICommand createCommand(int type) {
		if (VersionInfo.getVersion() == VersionInfo.ONE) {
			return new PlayCommand(ReceiverFactory.createCommand(type));
		}
		return new PlayCommand(new AudioReceiver());
	}

}
