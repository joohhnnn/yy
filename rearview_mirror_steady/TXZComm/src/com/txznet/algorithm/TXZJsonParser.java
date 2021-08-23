package com.txznet.algorithm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.os.HandlerThread;

import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.ThreadManager;

public class TXZJsonParser {
	private final static long INVALID_SESSION_ID = 0;
	/**
	 * 默认缓存大小
	 */
	public final static int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;

	private static HandlerThread mManagerThread;
	private static TXZHandler mManagerHandler;

	/**
	 * sax解析接口
	 */
	private static interface ISax {
		public int onNull();

		public int onBool(boolean b);

		public int onInt(int i);

		public int onLong(long l);

		public int onDouble(double d);

		public int onString(byte[] s);

		public int onObjectStart();

		public int onObjectKey(byte[] key);

		public int onObjectEnd(int count);

		public int onArrayStart();

		public int onArrayEnd(int count);

		public int onSuccess();

		public int onCancel();

		public int onError(int errOffset, int errCode, String errDesc);
	}

	static {
		System.loadLibrary("TXZUtil");
		// 后台进程
		mManagerThread = new HandlerThread("TXZJsonParserThread");
		mManagerThread.start();
		mManagerHandler = new TXZHandler(mManagerThread.getLooper());
	}

	private static native long create(Object sax, int buffSize, boolean useNativeThread);

	private static native boolean process(long sessionId, Object sax);
	
	private static native boolean write(long sessionId, byte[] data);

	private static native boolean cancel(long sessionId);

	private static native boolean complete(long sessionId);

	private static native boolean destroy(long sessionId);

	// //////////////////////////////////////////////////////////////////////////////////////////////

	public static class JsonException extends Exception {
		private static final long serialVersionUID = -5517949907293772278L;

		int errCode;

		JsonException(int errCode, String errDesc) {
			super(errDesc);
			this.errCode = errCode;
		}

		public int getErrCode() {
			return errCode;
		}
	}

	public static abstract class ParseResult<BeanType> {
		public abstract void onSuccess(BeanType result);

		public abstract void onCancel();

		public abstract void onError(int errOffset, Exception e);
	}

	public static class ParseTask {
		long sessionId = INVALID_SESSION_ID;
		Runnable cancelRunnable = new Runnable() {
			@Override
			public void run() {
				TXZJsonParser.cancel(ParseTask.this.sessionId);
			}
		};
		Runnable destroyRunnable = new Runnable() {
			@Override
			public void run() {
				TXZJsonParser.destroy(ParseTask.this.sessionId);
				ParseTask.this.sessionId = INVALID_SESSION_ID;
			}
		};
		Runnable completeRunnable = new Runnable() {
			@Override
			public void run() {
				TXZJsonParser.complete(ParseTask.this.sessionId);
			}
		};

		void destroy() {
			mManagerHandler.removeCallbacks(destroyRunnable);
			mManagerHandler.post(destroyRunnable);
		}

		ParseTask() {
		}

		public void complete() {
			mManagerHandler.removeCallbacks(completeRunnable);
			mManagerHandler.post(completeRunnable);
		}

		public void cancel() {
			mManagerHandler.removeCallbacks(cancelRunnable);
			mManagerHandler.post(cancelRunnable);
		}

		public void write(final byte[] data) {
			mManagerHandler.post(new Runnable() {
				@Override
				public void run() {
					TXZJsonParser.write(ParseTask.this.sessionId, data);
				}
			});
		}

		public void write(String data) {
			write(data.getBytes());
		}
	}

	private static Object getClassGenericType(Object clazz, int index) {
		ParameterizedType pt;
		if (clazz instanceof ParameterizedType) {
			pt = (ParameterizedType) clazz;
		} else {
			pt = ((ParameterizedType) ((Class<?>) clazz).getGenericSuperclass());
		}
		Type[] ts = pt.getActualTypeArguments();
		Type t = ts[index];
		return t;
	}

	private static class TaskSax<BeanType> implements ISax {
		ParseTask task;
		ParseResult<BeanType> callback;
		Exception lastException;
		boolean requiedAllField;

		final int NODE_TYPE_LST = 1;
		final int NODE_TYPE_OBJ = 2;

		private class Node {
			Node parent;
			Object clazz;
			Object current;
			int type; // 0默认，NODE_TYPE_LST为列表，NODE_TYPE_OBJ为对象
			Field field; // 对象的字段
		};

		Object ret;
		Node cur;

		TaskSax(ParseTask task, ParseResult<BeanType> callback,
				boolean requiedAllField) {
			this.task = task;
			this.callback = callback;
			this.requiedAllField = requiedAllField;
		}

		@Override
		public int onNull() {
			try {
				if (cur == null) {
					ret = null;
					return 0;
				}
				switch (cur.type) {
				case NODE_TYPE_LST:
					((List) cur.current).add(null);
					return 0;
				case NODE_TYPE_OBJ:
					if (cur.field != null)
						cur.field.set(cur.current, null);
					return 0;
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onBool(boolean v) {
			try {
				if (cur == null) {
					ret = (Boolean) v;
					return 0;
				}
				switch (cur.type) {
				case NODE_TYPE_LST:
					((List) cur.current).add(v);
					return 0;
				case NODE_TYPE_OBJ:
					if (cur.field != null)
						cur.field.set(cur.current, v);
					return 0;
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onInt(int v) {
			try {
				if (cur == null) {
					ret = (Integer) v;
					return 0;
				}
				switch (cur.type) {
				case NODE_TYPE_LST:
					((List) cur.current).add(v);
					return 0;
				case NODE_TYPE_OBJ:
					if (cur.field != null)
						cur.field.set(cur.current, v);
					return 0;
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onLong(long v) {
			try {
				if (cur == null) {
					ret = (Long) v;
					return 0;
				}
				switch (cur.type) {
				case NODE_TYPE_LST:
					((List) cur.current).add(v);
					return 0;
				case NODE_TYPE_OBJ:
					if (cur.field != null)
						cur.field.set(cur.current, v);
					return 0;
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onDouble(double v) {
			try {
				if (cur == null) {
					ret = (Double) v;
					return 0;
				}
				switch (cur.type) {
				case NODE_TYPE_LST:
					((List) cur.current).add(v);
					return 0;
				case NODE_TYPE_OBJ:
					if (cur.field != null)
						cur.field.set(cur.current, v);
					return 0;
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onString(byte[] v) {
			try {
				if (cur == null) {
					ret = new String(v);
					return 0;
				}
				switch (cur.type) {
				case NODE_TYPE_LST:
					((List) cur.current).add(new String(v));
					return 0;
				case NODE_TYPE_OBJ:
					if (cur.field != null)
						cur.field.set(cur.current, new String(v));
					return 0;
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onObjectStart() {
			try {
				if (cur == null) {
					cur = new Node();
					cur.parent = null;
					cur.clazz = getClassGenericType(callback.getClass(), 0);
					cur.current = ret = ((Class<?>) cur.clazz).newInstance();
					cur.type = NODE_TYPE_OBJ;
					return 0;
				}
				switch (cur.type) {
				case NODE_TYPE_LST: {
					Node n = new Node();
					n.parent = cur;
					n.clazz = getClassGenericType(cur.clazz, 0);
					n.current = ((Class<?>) n.clazz).newInstance();
					n.type = NODE_TYPE_OBJ;
					((List) cur.current).add(n.current);
					cur = n;
					return 0;
				}
				case NODE_TYPE_OBJ: {
					Node n = new Node();
					n.parent = cur;
					n.clazz = cur.field.getType();
					n.current = ((Class<?>) n.clazz).newInstance();
					n.type = NODE_TYPE_OBJ;
					if (cur.field != null)
						cur.field.set(cur.current, n.current);
					cur = n;
					return 0;
				}
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onObjectKey(byte[] key) {
			try {
				cur.field = ((Class<?>) cur.clazz).getDeclaredField(new String(
						key));
				cur.field.setAccessible(true);
			} catch (NoSuchFieldException e) {
				if (requiedAllField) {
					lastException = e;
					e.printStackTrace();
					return -1;
				}
				cur.field = null;
				return 1; // 跳过这个key
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onObjectEnd(int count) {
			try {
				cur = cur.parent;
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onArrayStart() {
			try {
				if (cur == null) {
					cur = new Node();
					cur.parent = null;
					cur.clazz = getClassGenericType(callback.getClass(), 0);
					cur.current = ret = new ArrayList();
					cur.type = NODE_TYPE_LST;
					return 0;
				}
				Node n = new Node();
				n.parent = cur;
				n.current = new ArrayList();
				n.type = NODE_TYPE_LST;
				switch (cur.type) {
				case NODE_TYPE_LST: {
					n.clazz = getClassGenericType(cur.clazz, 0);
					((List) cur.current).add(n.current);
					cur = n;
					return 0;
				}
				case NODE_TYPE_OBJ: {
					n.clazz = cur.field.getType();
					if (n.clazz == List.class) {
						n.clazz = (ParameterizedType) cur.field
								.getGenericType();
					}
					if (cur.field != null)
						cur.field.set(cur.current, n.current);
					cur = n;
					return 0;
				}
				}
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onArrayEnd(int count) {
			try {
				cur = cur.parent;
			} catch (Exception e) {
				lastException = e;
				e.printStackTrace();
				return -1;
			}
			return 0;
		}

		@Override
		public int onSuccess() {
			mManagerHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onSuccess((BeanType) ret);
				}
			});
			task.destroy();
			return 0;
		}

		@Override
		public int onCancel() {
			mManagerHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onCancel();
				}
			});
			task.destroy();
			return 0;
		}

		@Override
		public int onError(final int errOffset, final int errCode,
				final String errDesc) {
			mManagerHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onError(errOffset,
							lastException != null ? lastException
									: new JsonException(errCode, errDesc));
				}
			});
			task.destroy();
			return 0;
		}
	}

	public static <BeanType> ParseTask createTask(
			final ParseResult<BeanType> callback, final int bufferSize,
			final boolean requiedAllField, final boolean useNativeThread) {
		final ParseTask task = new ParseTask();
		mManagerHandler.post(new Runnable() {
			@Override
			public void run() {
				final TaskSax<BeanType> sax = new TaskSax<BeanType>(task, callback,
						requiedAllField);
				task.sessionId = TXZJsonParser.create(sax, bufferSize, useNativeThread);
				if (!useNativeThread) {
					ThreadManager.getPool().execute(new Runnable() {
						@Override
						public void run() {
							process(task.sessionId, sax);
						}
					});
				}
			}
		});
		return task;
	}

	public static <BeanType> ParseTask createTask(
			ParseResult<BeanType> callback, boolean requiedAllField) {
		return createTask(callback, DEFAULT_BUFFER_SIZE, requiedAllField, false);
	}

	public static <BeanType> ParseTask createTask(
			ParseResult<BeanType> callback, int bufferSize) {
		return createTask(callback, bufferSize, false, false);
	}

	public static <BeanType> ParseTask createTask(ParseResult<BeanType> callback) {
		return createTask(callback, DEFAULT_BUFFER_SIZE, false, false);
	}
}
