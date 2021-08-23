package com.txznet.webchat.plugin.preset.logic.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpCookie;

/**
 * Created by <a href="http://www.jiechic.com" target="_blank">jiechic</a> on 15/5/27.
 */
public class SerializableHttpCookie implements Serializable {
    private static final String LOG_TAG = "SerializableHttpCookie";
    private static final long serialVersionUID = 6374381323722046732L;

    private transient final HttpCookie cookie;
    private transient HttpCookie clientCookie;

    public SerializableHttpCookie(HttpCookie cookie) {
        this.cookie = cookie;
    }

    public HttpCookie getCookie() {
        HttpCookie bestCookie = cookie;
        if (clientCookie != null) {
            bestCookie = clientCookie;
        }
        return bestCookie;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        HttpCookie cookie2Write = null == cookie ? clientCookie : cookie;
        out.writeObject(cookie2Write.getName());
        out.writeObject(cookie2Write.getValue());
        out.writeObject(cookie2Write.getComment());
        out.writeObject(cookie2Write.getCommentURL());
        out.writeObject(cookie2Write.getDomain());
        out.writeLong(cookie2Write.getMaxAge());
        out.writeObject(cookie2Write.getPath());
        out.writeObject(cookie2Write.getPortlist());
        out.writeInt(cookie2Write.getVersion());
        out.writeBoolean(cookie2Write.getSecure());
        out.writeBoolean(cookie2Write.getDiscard());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String name = (String) in.readObject();
        String value = (String) in.readObject();
        clientCookie = new HttpCookie(name, value);
        clientCookie.setComment((String) in.readObject());
        clientCookie.setCommentURL((String) in.readObject());
        clientCookie.setDomain((String) in.readObject());
        clientCookie.setMaxAge(in.readLong());
        clientCookie.setPath((String) in.readObject());
        clientCookie.setPortlist((String) in.readObject());
        clientCookie.setVersion(in.readInt());
        clientCookie.setSecure(in.readBoolean());
        clientCookie.setDiscard(in.readBoolean());
    }
}