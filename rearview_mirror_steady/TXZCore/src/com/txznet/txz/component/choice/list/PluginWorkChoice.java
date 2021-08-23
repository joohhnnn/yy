package com.txznet.txz.component.choice.list;

import com.txznet.comm.ui.IKeepClass;
import com.txznet.txz.component.choice.option.CompentOption;

public abstract class PluginWorkChoice<E> extends CommWorkChoice<E> implements IKeepClass {

	public PluginWorkChoice(CompentOption<E> option) {
		super(option);
	}

	public abstract String getReportId();
}