package com.txznet.record.bean;

public class ConstellationFortuneMsg extends ChatMessage{
	public int level;
	public String name;
	public String fortuneType;
	public String desc;

	public ConstellationFortuneMsg() {
		super(TYPE_FROM_SYS_CONSTELLATION_FORTUNE);
	}

}
