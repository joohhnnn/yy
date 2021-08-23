package com.txznet.record.bean;

public class ConstellationMatchingMsg extends ChatMessage{
	public int level;
	public String name;
	public String matchName;
	public String desc;

	public ConstellationMatchingMsg() {
		super(TYPE_FROM_SYS_CONSTELLATION_MATCHING);
	}

}
