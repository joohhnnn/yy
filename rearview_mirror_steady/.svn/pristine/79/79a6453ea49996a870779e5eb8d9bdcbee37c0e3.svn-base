/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\svn\\android\\projects\\rearview_mirror_steady\\TXZExtAudioRecord\\src\\com\\txznet\\txz\\extaudiorecord\\IAudioCallback.aidl
 */
package com.txznet.txz.extaudiorecord;
public interface IAudioCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.txznet.txz.extaudiorecord.IAudioCallback
{
private static final java.lang.String DESCRIPTOR = "com.txznet.txz.extaudiorecord.IAudioCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.txznet.txz.extaudiorecord.IAudioCallback interface,
 * generating a proxy if needed.
 */
public static com.txznet.txz.extaudiorecord.IAudioCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.txznet.txz.extaudiorecord.IAudioCallback))) {
return ((com.txznet.txz.extaudiorecord.IAudioCallback)iin);
}
return new com.txznet.txz.extaudiorecord.IAudioCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onCallBack:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
this.onCallBack(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.txznet.txz.extaudiorecord.IAudioCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onCallBack(byte[] data) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(data);
mRemote.transact(Stub.TRANSACTION_onCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onCallBack(byte[] data) throws android.os.RemoteException;
}
