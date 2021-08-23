package com.txznet.audio.server;

import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.SessionManager;
import com.txznet.audio.server.response.MediaHttpClient;
import com.txznet.audio.server.response.MediaResponseFactory;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.Constant;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class LocalMediaServer extends NanoHTTPD {
	private static final String MIME_AUDIO = "audio/mpeg";

	private static LocalMediaServer sInstance;
	static {
		try {
			sInstance = new LocalMediaServer();
		} catch (Exception e) {
		}
	}

	public static LocalMediaServer getInstance() {
		return sInstance;
	}

	private LocalMediaServer() throws IOException {
		super(25555, new File("."));
	}

	public Response serve(Socket socket, String uri, String method,
			Properties header, Properties parms, Properties files) {
		try {
			LogUtil.logi(Constant.SPEND_TAG + " receive request ");
			long from = -1;
			long to = -1;
			int sessionId = 0;
			StringBuffer sb = new StringBuffer();
			try {
				if (null != header) {
					// Range: bytes=11100-12184830
					Iterator<Object> iterator = header.keySet().iterator();
					while (iterator.hasNext()) {
						String key = (String) iterator.next();
						if (key.equalsIgnoreCase("range")) {
							String[] rangVal = header.get(key).toString()
									.substring(6).split("-");
							from = Long.parseLong(rangVal[0]);
							if (rangVal.length > 1) {
								to = Long.parseLong(rangVal[1]);
							}
						}
						sb.append("" + key + "=" + header.get(key) + "\r\n");
					}
				}

				sessionId = Integer.parseInt(uri.substring(1));
			} catch (Exception e) {
				LogUtil.loge("media session request error: " + e.getMessage());
				return new Response(NanoHTTPD.HTTP_BADREQUEST, MIME_AUDIO,
						e.getMessage());
			}

			SessionInfo sess = SessionManager.getInstance().getSessionInfo(
					sessionId);

			if (sess == null) {
				LogUtil.loge("media session not found: " + sessionId);
				return new Response(NanoHTTPD.HTTP_BADREQUEST, MIME_AUDIO,
						"media session not found: " + sessionId);
			}

			Thread.currentThread().setName("MediaSession#" + sess.getLogId());

			if (Constant.ISTESTDATA) {
				LogUtil.logd("media session[" + sess.getLogId()
						+ "] create method: " + method + ",from/to=" + from
						+ "/" + to + "\r\n" + sb);
			}
			if (null != header) {
				for (Map.Entry<Object, Object> entry : header.entrySet()) {
					Object key = entry.getKey();
					Object value = entry.getValue();
					LogUtil.logd(MediaHttpClient.TAG_HTTP+"player http request header: " + key + "=" + value);
				}
			}
//			if (Constant.ISTESTDATA) {
//				LogUtil.logd("media session[" + sess.getLogId()
//						+ "] create method: " + method);
//			}
			return MediaResponseFactory.createResponse(socket, sess, from, to);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.serve(socket, uri, method, header, parms, files);
	}

	public int getPort() {
		return myTcpPort;
	}
}
