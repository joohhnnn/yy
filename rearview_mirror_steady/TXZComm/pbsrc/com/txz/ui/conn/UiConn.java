// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.ui.conn;

@SuppressWarnings("hiding")
public interface UiConn {
  
  // enum SubEvent
  public static final int SUBEVENT_DEFAULT = 0;
  public static final int SUBEVENT_IP_NEW = 1;
  
  public static final class ConnConfig extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile ConnConfig[] _emptyArray;
    public static ConnConfig[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new ConnConfig[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_last_login_server_ip = 1;
    public java.lang.Integer uint32LastLoginServerIp;
    
    // optional uint32 uint32_last_login_server_port = 2;
    public java.lang.Integer uint32LastLoginServerPort;
    
    public ConnConfig() {
      clear();
    }
    
    public ConnConfig clear() {
      uint32LastLoginServerIp = null;
      uint32LastLoginServerPort = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32LastLoginServerIp != null) {
        output.writeUInt32(1, this.uint32LastLoginServerIp);
      }
      if (this.uint32LastLoginServerPort != null) {
        output.writeUInt32(2, this.uint32LastLoginServerPort);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32LastLoginServerIp != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32LastLoginServerIp);
      }
      if (this.uint32LastLoginServerPort != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32LastLoginServerPort);
      }
      return size;
    }
    
    @Override
    public ConnConfig mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!com.google.protobuf.nano.WireFormatNano.parseUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 8: {
            this.uint32LastLoginServerIp = input.readUInt32();
            break;
          }
          case 16: {
            this.uint32LastLoginServerPort = input.readUInt32();
            break;
          }
        }
      }
    }
    
    public static ConnConfig parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new ConnConfig(), data);
    }
    
    public static ConnConfig parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new ConnConfig().mergeFrom(input);
    }
  }
  
  public static final class ConnHost extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile ConnHost[] _emptyArray;
    public static ConnHost[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new ConnHost[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional string str_server_ip = 1;
    public java.lang.String strServerIp;
    
    // optional uint32 uint32_server_port = 2;
    public java.lang.Integer uint32ServerPort;
    
    public ConnHost() {
      clear();
    }
    
    public ConnHost clear() {
      strServerIp = null;
      uint32ServerPort = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.strServerIp != null) {
        output.writeString(1, this.strServerIp);
      }
      if (this.uint32ServerPort != null) {
        output.writeUInt32(2, this.uint32ServerPort);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.strServerIp != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(1, this.strServerIp);
      }
      if (this.uint32ServerPort != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32ServerPort);
      }
      return size;
    }
    
    @Override
    public ConnHost mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!com.google.protobuf.nano.WireFormatNano.parseUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 10: {
            this.strServerIp = input.readString();
            break;
          }
          case 16: {
            this.uint32ServerPort = input.readUInt32();
            break;
          }
        }
      }
    }
    
    public static ConnHost parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new ConnHost(), data);
    }
    
    public static ConnHost parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new ConnHost().mergeFrom(input);
    }
  }
}
