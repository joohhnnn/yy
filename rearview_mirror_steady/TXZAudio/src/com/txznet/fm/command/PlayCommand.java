package com.txznet.fm.command;

import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.fm.command.interfase.ICommand;
import com.txznet.fm.receiver.interfase.IReceiver;

/**
 * 播放操作按钮
 * 
 * @author ASUS User
 *
 */
public class PlayCommand implements ICommand{

	//真正做实事的
	private IReceiver  receiver;
	
	public PlayCommand(IReceiver receiver) {
		super();
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		//做事
		receiver.play();
	}

	@Override
	public void onClick(View v) {
		execute();
	}
}
