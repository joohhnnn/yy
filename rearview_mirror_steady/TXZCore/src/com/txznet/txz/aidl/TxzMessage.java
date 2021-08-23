package com.txznet.txz.aidl;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;

public class TxzMessage implements Parcelable {

	/**
     * User-defined message code so that the recipient can identify 
     * what this message is about. Each {@link Handler} has its own name-space
     * for message codes, so you do not need to worry about yours conflicting
     * with other handlers.
     */
    public int what;

    /**
     * arg1 and arg2 are lower-cost alternatives to using
     * {@link #setData(Bundle) setData()} if you only need to store a
     * few integer values.
     */
    public int arg1; 

    /**
     * arg1 and arg2 are lower-cost alternatives to using
     * {@link #setData(Bundle) setData()} if you only need to store a
     * few integer values.
     */
    public int arg2;

    /**
     * An arbitrary object to send to the recipient.  When using
     * {@link Messenger} to send the message across processes this can only
     * be non-null if it contains a Parcelable of a framework class (not one
     * implemented by the application).   For other data transfer use
     * {@link #setData}.
     * 
     * <p>Note that Parcelable objects here are not supported prior to
     * the {@link android.os.Build.VERSION_CODES#FROYO} release.
     */
    public Object obj;

    /**
     * Optional Messenger where replies to this message can be sent.  The
     * semantics of exactly how this is used are up to the sender and
     * receiver.
     */
    public ITxzMessenger replyTo;

    /*package*/ Bundle data;
    
    /** 
     * Obtains a Bundle of arbitrary data associated with this
     * event, lazily creating it if necessary. Set this value by calling
     * {@link #setData(Bundle)}.  Note that when transferring data across
     * processes via {@link Messenger}, you will need to set your ClassLoader
     * on the Bundle via {@link Bundle#setClassLoader(ClassLoader)
     * Bundle.setClassLoader()} so that it can instantiate your objects when
     * you retrieve them.
     * @see #peekData()
     * @see #setData(Bundle)
     */
    public Bundle getData() {
        if (data == null) {
            data = new Bundle();
        }
        
        return data;
    }

    /** 
     * Like getData(), but does not lazily create the Bundle.  A null
     * is returned if the Bundle does not already exist.  See
     * {@link #getData} for further information on this.
     * @see #getData()
     * @see #setData(Bundle)
     */
    public Bundle peekData() {
        return data;
    }

    /**
     * Sets a Bundle of arbitrary data values. Use arg1 and arg2 members
     * as a lower cost way to send a few simple integer values, if you can.
     * @see #getData() 
     * @see #peekData()
     */
    public void setData(Bundle data) {
        this.data = data;
    }

    /** Constructor (but the preferred way to get a Message is to call {@link #obtain() Message.obtain()}).
    */
    public TxzMessage() {
    }

    @Override
    public String toString() {
    	StringBuilder b = new StringBuilder();
        b.append("{ ");

		b.append(" what=");
		b.append(what);

		if (arg1 != 0) {
			b.append(" arg1=");
			b.append(arg1);
		}

		if (arg2 != 0) {
			b.append(" arg2=");
			b.append(arg2);
		}

		if (obj != null) {
			b.append(" obj=");
			b.append(obj);
		}

        b.append(" }");
        return b.toString();
    }

    public static final Parcelable.Creator<TxzMessage> CREATOR
            = new Parcelable.Creator<TxzMessage>() {
        public TxzMessage createFromParcel(Parcel source) {
        	TxzMessage msg = new TxzMessage();
            msg.readFromParcel(source);
            return msg;
        }
        
        public TxzMessage[] newArray(int size) {
            return new TxzMessage[size];
        }
    };
        
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(what);
        dest.writeInt(arg1);
        dest.writeInt(arg2);
        if (obj != null) {
            try {
                Parcelable p = (Parcelable)obj;
                dest.writeInt(1);
                dest.writeParcelable(p, flags);
            } catch (ClassCastException e) {
                throw new RuntimeException(
                    "Can't marshal non-Parcelable objects across processes.");
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeBundle(data);
        writeMessengerOrNullToParcel(replyTo, dest);
    }

    private void readFromParcel(Parcel source) {
        what = source.readInt();
        arg1 = source.readInt();
        arg2 = source.readInt();
        if (source.readInt() != 0) {
            obj = source.readParcelable(getClass().getClassLoader());
        }
        data = source.readBundle();
        replyTo = readMessengerOrNullFromParcel(source);
    }
    
    /**
     * Convenience function for writing either a Messenger or null pointer to
     * a Parcel.  You must use this with {@link #readMessengerOrNullFromParcel}
     * for later reading it.
     * 
     * @param messenger The Messenger to write, or null.
     * @param out Where to write the Messenger.
     */
    private void writeMessengerOrNullToParcel(ITxzMessenger messenger, Parcel out) {
        out.writeStrongBinder(messenger != null ? messenger.asBinder() : null);
    }
    
    /**
     * Convenience function for reading either a Messenger or null pointer from
     * a Parcel.  You must have previously written the Messenger with
     * {@link #writeMessengerOrNullToParcel}.
     * 
     * @param in The Parcel containing the written Messenger.
     * 
     * @return Returns the Messenger read from the Parcel, or null if null had
     * been written.
     */
    private ITxzMessenger readMessengerOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        return b != null ? ITxzMessenger.Stub.asInterface(b) : null;
    }
}
