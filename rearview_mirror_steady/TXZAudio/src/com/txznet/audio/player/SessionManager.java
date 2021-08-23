package com.txznet.audio.player;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.util.SparseArray;

import com.txznet.audio.client.HttpMediaClient;
import com.txznet.audio.client.TXZMediaClient;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.server.LocalMediaServer;
import com.txznet.audio.server.response.MediaResponseBase;
import com.txznet.comm.remote.util.LogUtil;

public class SessionManager {
	private static SessionManager sInstance = new SessionManager();

	private SessionManager() {

	}

	public static SessionManager getInstance() {
		return sInstance;
	}

	private static String genLocalMediaUrl(SessionInfo sess) {
		// return
		// "http://audio.xmcdn.com/group7/M0A/3C/20/wKgDX1WcfSSh1ddlAFoUZBSo4Xo791.m4a";//喜马拉雅
		// "http://c203.duotin.com/M02/37/15/wKgB5Vc0D5qAJXdQAFtBw-PoEi8807.mp3";//多听
		// "http://192.168.0.104:8080/xmly.m4a";
		return "http://127.0.0.1:" + LocalMediaServer.getInstance().getPort()
				+ "/" + sess.hashCode() + "?s=" + sess.hashCode() + "&r="
				+ new Random().nextInt();
	}

	public TXZAudioPlayer createPlayer(PlayerAudio audio) {
		String localUrl = null;
		SessionInfo sess = new SessionInfo(audio);
		localUrl = genLocalMediaUrl(sess);
		if (audio.needCodecPlayer()) {
			TXZMediaClient mediaClient = new HttpMediaClient(localUrl);
			sess.player = new CodecAudioPlayer(sess, audio.getStreamType(),
					mediaClient, audio.getDuration());
		} else {
			sess.player = new SysAudioPlayer(sess, audio.getStreamType(),
					localUrl);
		}
		SessionManager.getInstance().addSessionInfo(sess.hashCode(), sess);

		LogUtil.logd("media session[" + sess.getLogId() + "] create session["
				+ audio.toString() + "]: player[" + sess.player.toString()
				+ "], url=" + localUrl);

		return sess.player;
	}

	public SessionInfo getSessionInfo(int playerSessionId) {
		synchronized (mAudioSessions) {
			return mAudioSessions.get(playerSessionId);
		}
	}

	public void addSessionInfo(int playSessionId, SessionInfo sess) {
		synchronized (mAudioSessions) {
			mAudioSessions.put(playSessionId, sess);
		}
	}
	public void removeSessionInfo(int playSessionId) {
		synchronized (mAudioSessions) {
			SessionManager.getInstance().mAudioSessions.remove(playSessionId);
		}
	}

	private SparseArray<SessionInfo> mAudioSessions = new SparseArray<SessionInfo>();

	public static class SessionInfo {
		public PlayerAudio audio;
		public TXZAudioPlayer player;
		protected Set<MediaResponseBase> responses = new HashSet<MediaResponseBase>();

		public SessionInfo(PlayerAudio audio) {
			this.audio = audio;
		}

		public void addResponse(MediaResponseBase res) {
			synchronized (responses) {
				responses.add(res);
			}
		}

		public void cancelAllResponse() {
			synchronized (responses) {
				for (MediaResponseBase response : responses) {
					response.cancel();
				}
				responses.clear();
			}
		}

		public String getLogId() {
			String name = audio.getAudioName();
			if (name.length() > 4) {
				name = name.substring(0, 4);
			}
			return "" + this.hashCode() + "#" + name;
		}
	}
}
