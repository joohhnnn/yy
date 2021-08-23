package com.txznet.feedback.db;

import com.txznet.feedback.AppLogic;
import com.txznet.sqlite.AbstractBaseDao;

public class MsgDao extends AbstractBaseDao{

	public MsgDao() {
		super(AppLogic.getSqliteParams());
	}
}