package com.txznet.proxy;

import com.txznet.comm.remote.util.LogUtil;

import java.util.Iterator;
import java.util.LinkedList;

public class SessionManager {

    private static final class Holder {
        private static final SessionManager MANAGER = new SessionManager();
    }

    private SessionManager() {
    }

    public static SessionManager get() {
        return Holder.MANAGER;
    }

    private final LinkedList<ProxySession> mProxySessions = new LinkedList<>();

    public ProxySession getSession(int sessionId) {
        synchronized (mProxySessions) {
            for (ProxySession session : mProxySessions) {
                if (session.hashCode() == sessionId) {
                    return session;
                }
            }
        }
        return null;
    }

    public void addSession(ProxySession sess) {
        synchronized (mProxySessions) {
            Iterator<ProxySession> iterator = mProxySessions.iterator();
            while (iterator.hasNext()) {
                ProxySession session = iterator.next();
                if (session == null) {
                    iterator.remove();
                }
            }
            // 控制容器上限
            if (mProxySessions.size() > 3) {
                ProxySession session = mProxySessions.getFirst();
                session.cancelAllResponse();
                mProxySessions.remove(session);
                LogUtil.logd("proxy:oom:remove same:" + session.tag);
            }
            // 清除重复tag的session
            ProxySession sameProxySession = null;
            for (ProxySession session : mProxySessions) {
                if (session.tag != null && session.tag.equals(sess.tag)) {
                    sameProxySession = session;
                    break;
                }
            }
            if (sameProxySession != null) {
                sameProxySession.cancelAllResponse();
                mProxySessions.remove(sameProxySession);
                LogUtil.logd("proxy:oom:remove same:" + sameProxySession.tag);
            }
            mProxySessions.add(sess);
            LogUtil.logd("proxy:oom:add:" + sess.tag);
            LogUtil.logd("proxy:oom:size:" + mProxySessions.size());
        }
    }

    public void removeSession(String tag) {
        synchronized (mProxySessions) {
            Iterator<ProxySession> iterator = mProxySessions.iterator();
            while (iterator.hasNext()) {
                ProxySession session = iterator.next();
                if (session != null && session.tag.equals(tag)) {
                    session.cancelAllResponse();
                    iterator.remove();
                    LogUtil.logd("proxy:oom:remove:" + session.tag);
                }
            }
            LogUtil.logd("proxy:oom:size:" + mProxySessions.size());
        }
    }

    public ProxySession findSessionByTag(int sid, long id) {
        synchronized (mProxySessions) {
            for (ProxySession session : mProxySessions) {
                if (session != null && session.tag.equals(getTag(sid, id))) {
                    return session;
                }
            }
        }
        return null;
    }

    public String getTag(int sid, long id) {
        return sid + "_" + id;
    }
}
