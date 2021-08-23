package com.txznet.fm.factory.interfase;

/**
 * 工厂接口，产生对象
 * 
 * @author ASUS User
 *
 */
public interface IFactory<T> {

	/**
	 * 根据不同的类型产生不同的命令对象
	 * 
	 * @param type
	 * @return
	 */
	public T createCommand(int type);
}
