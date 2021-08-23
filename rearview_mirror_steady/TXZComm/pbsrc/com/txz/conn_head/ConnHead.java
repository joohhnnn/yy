// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.conn_head;

@SuppressWarnings("hiding")
public interface ConnHead {
  
  // enum Command
  public static final int CMD_DEFAULT = 0;
  public static final int CMD_USER_MANAGER = 1;
  public static final int CMD_RELATIONSHIP_MANAGER = 2;
  public static final int CMD_DATA_MANAGER = 3;
  public static final int CMD_MESSAGE_MANAGER = 4;
  public static final int CMD_LOG_MANAGER = 5;
  public static final int CMD_MUSIC_MANAGER = 6;
  public static final int CMD_EQUIPMENT_MANAGER = 7;
  public static final int CMD_IM_MANAGER = 8;
  public static final int CMD_SERVER_MANAGER = 9;
  public static final int CMD_PUSH_MANAGER = 10;
  public static final int CMD_AUDIO_MANAGER = 11;
  public static final int CMD_REPORT_MANAGER = 12;
  public static final int CMD_VERSION_MANAGER = 65536;
  public static final int CMD_UI_BEGIN = 2147418112;
  
  // enum ErrorCode
  public static final int EC_OK = 0;
  public static final int EC_SERVICE_NOTFOUND = 1;
  public static final int EC_SERVICE_TIMEOUT = 2;
  public static final int EC_SERVICE_OVERLOAD = 3;
  public static final int EC_REQUEST_FULL = 4;
  public static final int EC_NO_LOGIN = 5;
  public static final int EC_SVR_BUSY = 6;
  public static final int EC_PROTO_PARSE_ERR = 7;
  public static final int EC_PARAM_ERROR = 8;
  public static final int EC_REQUEST_TIMEOUT = 9;
  public static final int EC_SEND_ERROR = 10;
  public static final int EC_NEED_NETWORK = 11;
  public static final int EC_NEED_SDK_INIT = 12;
  public static final int EC_WX_OUT_LIMIT = 13;
  public static final int EC_SERVER_REQUEST_FREQ_LIMIT = 14;
  public static final int EC_VERSION_TOO_LOW = 15;
  
  // enum PacketType
  public static final int PACKTYPE_REQUEST = 0;
  public static final int PACKTYPE_RESPONSE = 1;
  public static final int PACKTYPE_RESPONSE_BY_CLIENT = 2;
  
  public static final class RedirectInfo extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile RedirectInfo[] _emptyArray;
    public static RedirectInfo[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new RedirectInfo[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // repeated uint32 rpt_uint32_direct_wrong_ips = 1;
    public int[] rptUint32DirectWrongIps;
    
    // optional bytes str_app_id = 2;
    public byte[] strAppId;
    
    public RedirectInfo() {
      clear();
    }
    
    public RedirectInfo clear() {
      rptUint32DirectWrongIps = com.google.protobuf.nano.WireFormatNano.EMPTY_INT_ARRAY;
      strAppId = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.rptUint32DirectWrongIps != null && this.rptUint32DirectWrongIps.length > 0) {
        for (int i = 0; i < this.rptUint32DirectWrongIps.length; i++) {
          output.writeUInt32(1, this.rptUint32DirectWrongIps[i]);
        }
      }
      if (this.strAppId != null) {
        output.writeBytes(2, this.strAppId);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.rptUint32DirectWrongIps != null && this.rptUint32DirectWrongIps.length > 0) {
        int dataSize = 0;
        for (int i = 0; i < this.rptUint32DirectWrongIps.length; i++) {
          int element = this.rptUint32DirectWrongIps[i];
          dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeUInt32SizeNoTag(element);
        }
        size += dataSize;
        size += 1 * this.rptUint32DirectWrongIps.length;
      }
      if (this.strAppId != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(2, this.strAppId);
      }
      return size;
    }
    
    @Override
    public RedirectInfo mergeFrom(
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
            int arrayLength = com.google.protobuf.nano.WireFormatNano
                .getRepeatedFieldArrayLength(input, 8);
            int i = this.rptUint32DirectWrongIps == null ? 0 : this.rptUint32DirectWrongIps.length;
            int[] newArray = new int[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.rptUint32DirectWrongIps, 0, newArray, 0, i);
            }
            for (; i < newArray.length - 1; i++) {
              newArray[i] = input.readUInt32();
              input.readTag();
            }
            // Last one without readTag.
            newArray[i] = input.readUInt32();
            this.rptUint32DirectWrongIps = newArray;
            break;
          }
          case 10: {
            int length = input.readRawVarint32();
            int limit = input.pushLimit(length);
            // First pass to compute array length.
            int arrayLength = 0;
            int startPos = input.getPosition();
            while (input.getBytesUntilLimit() > 0) {
              input.readUInt32();
              arrayLength++;
            }
            input.rewindToPosition(startPos);
            int i = this.rptUint32DirectWrongIps == null ? 0 : this.rptUint32DirectWrongIps.length;
            int[] newArray = new int[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.rptUint32DirectWrongIps, 0, newArray, 0, i);
            }
            for (; i < newArray.length; i++) {
              newArray[i] = input.readUInt32();
            }
            this.rptUint32DirectWrongIps = newArray;
            input.popLimit(limit);
            break;
          }
          case 18: {
            this.strAppId = input.readBytes();
            break;
          }
        }
      }
    }
    
    public static RedirectInfo parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new RedirectInfo(), data);
    }
    
    public static RedirectInfo parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new RedirectInfo().mergeFrom(input);
    }
  }
  
  public static final class Head extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Head[] _emptyArray;
    public static Head[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Head[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_cmd = 1;
    public java.lang.Integer uint32Cmd;
    
    // optional uint32 uint32_subcmd = 2;
    public java.lang.Integer uint32Subcmd;
    
    // optional bytes str_version = 3;
    public byte[] strVersion;
    
    // optional bytes str_username = 4;
    public byte[] strUsername;
    
    // optional uint64 uint64_uid = 5;
    public java.lang.Long uint64Uid;
    
    // optional uint32 uint32_seq = 6;
    public java.lang.Integer uint32Seq;
    
    // optional uint32 uint32_resultcode = 7;
    public java.lang.Integer uint32Resultcode;
    
    // optional uint32 uint32_type = 8;
    public java.lang.Integer uint32Type;
    
    // optional uint32 uint32_version = 9;
    public java.lang.Integer uint32Version;
    
    // optional .com.txz.conn_head.RedirectInfo msg_redirect_info = 10;
    public com.txz.conn_head.ConnHead.RedirectInfo msgRedirectInfo;
    
    public Head() {
      clear();
    }
    
    public Head clear() {
      uint32Cmd = null;
      uint32Subcmd = null;
      strVersion = null;
      strUsername = null;
      uint64Uid = null;
      uint32Seq = null;
      uint32Resultcode = null;
      uint32Type = null;
      uint32Version = null;
      msgRedirectInfo = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32Cmd != null) {
        output.writeUInt32(1, this.uint32Cmd);
      }
      if (this.uint32Subcmd != null) {
        output.writeUInt32(2, this.uint32Subcmd);
      }
      if (this.strVersion != null) {
        output.writeBytes(3, this.strVersion);
      }
      if (this.strUsername != null) {
        output.writeBytes(4, this.strUsername);
      }
      if (this.uint64Uid != null) {
        output.writeUInt64(5, this.uint64Uid);
      }
      if (this.uint32Seq != null) {
        output.writeUInt32(6, this.uint32Seq);
      }
      if (this.uint32Resultcode != null) {
        output.writeUInt32(7, this.uint32Resultcode);
      }
      if (this.uint32Type != null) {
        output.writeUInt32(8, this.uint32Type);
      }
      if (this.uint32Version != null) {
        output.writeUInt32(9, this.uint32Version);
      }
      if (this.msgRedirectInfo != null) {
        output.writeMessage(10, this.msgRedirectInfo);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32Cmd != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32Cmd);
      }
      if (this.uint32Subcmd != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32Subcmd);
      }
      if (this.strVersion != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(3, this.strVersion);
      }
      if (this.strUsername != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(4, this.strUsername);
      }
      if (this.uint64Uid != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(5, this.uint64Uid);
      }
      if (this.uint32Seq != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(6, this.uint32Seq);
      }
      if (this.uint32Resultcode != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(7, this.uint32Resultcode);
      }
      if (this.uint32Type != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(8, this.uint32Type);
      }
      if (this.uint32Version != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(9, this.uint32Version);
      }
      if (this.msgRedirectInfo != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(10, this.msgRedirectInfo);
      }
      return size;
    }
    
    @Override
    public Head mergeFrom(
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
            this.uint32Cmd = input.readUInt32();
            break;
          }
          case 16: {
            this.uint32Subcmd = input.readUInt32();
            break;
          }
          case 26: {
            this.strVersion = input.readBytes();
            break;
          }
          case 34: {
            this.strUsername = input.readBytes();
            break;
          }
          case 40: {
            this.uint64Uid = input.readUInt64();
            break;
          }
          case 48: {
            this.uint32Seq = input.readUInt32();
            break;
          }
          case 56: {
            this.uint32Resultcode = input.readUInt32();
            break;
          }
          case 64: {
            this.uint32Type = input.readUInt32();
            break;
          }
          case 72: {
            this.uint32Version = input.readUInt32();
            break;
          }
          case 82: {
            if (this.msgRedirectInfo == null) {
              this.msgRedirectInfo = new com.txz.conn_head.ConnHead.RedirectInfo();
            }
            input.readMessage(this.msgRedirectInfo);
            break;
          }
        }
      }
    }
    
    public static Head parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Head(), data);
    }
    
    public static Head parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Head().mergeFrom(input);
    }
  }
  
  public static final class Body extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Body[] _emptyArray;
    public static Body[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Body[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional .com.txz.app_head.Head msg_app_head = 1;
    public com.txz.app_head.AppHead.Head msgAppHead;
    
    // optional bytes str_app_data = 2;
    public byte[] strAppData;
    
    public Body() {
      clear();
    }
    
    public Body clear() {
      msgAppHead = null;
      strAppData = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.msgAppHead != null) {
        output.writeMessage(1, this.msgAppHead);
      }
      if (this.strAppData != null) {
        output.writeBytes(2, this.strAppData);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.msgAppHead != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(1, this.msgAppHead);
      }
      if (this.strAppData != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(2, this.strAppData);
      }
      return size;
    }
    
    @Override
    public Body mergeFrom(
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
            if (this.msgAppHead == null) {
              this.msgAppHead = new com.txz.app_head.AppHead.Head();
            }
            input.readMessage(this.msgAppHead);
            break;
          }
          case 18: {
            this.strAppData = input.readBytes();
            break;
          }
        }
      }
    }
    
    public static Body parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Body(), data);
    }
    
    public static Body parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Body().mergeFrom(input);
    }
  }
  
  public static final class RequestRecord extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile RequestRecord[] _emptyArray;
    public static RequestRecord[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new RequestRecord[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional .com.txz.conn_head.Head msg_conn_head = 1;
    public com.txz.conn_head.ConnHead.Head msgConnHead;
    
    // optional .com.txz.app_head.Head msg_app_head = 2;
    public com.txz.app_head.AppHead.Head msgAppHead;
    
    // optional bytes str_app_data = 3;
    public byte[] strAppData;
    
    public RequestRecord() {
      clear();
    }
    
    public RequestRecord clear() {
      msgConnHead = null;
      msgAppHead = null;
      strAppData = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.msgConnHead != null) {
        output.writeMessage(1, this.msgConnHead);
      }
      if (this.msgAppHead != null) {
        output.writeMessage(2, this.msgAppHead);
      }
      if (this.strAppData != null) {
        output.writeBytes(3, this.strAppData);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.msgConnHead != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(1, this.msgConnHead);
      }
      if (this.msgAppHead != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(2, this.msgAppHead);
      }
      if (this.strAppData != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(3, this.strAppData);
      }
      return size;
    }
    
    @Override
    public RequestRecord mergeFrom(
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
            if (this.msgConnHead == null) {
              this.msgConnHead = new com.txz.conn_head.ConnHead.Head();
            }
            input.readMessage(this.msgConnHead);
            break;
          }
          case 18: {
            if (this.msgAppHead == null) {
              this.msgAppHead = new com.txz.app_head.AppHead.Head();
            }
            input.readMessage(this.msgAppHead);
            break;
          }
          case 26: {
            this.strAppData = input.readBytes();
            break;
          }
        }
      }
    }
    
    public static RequestRecord parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new RequestRecord(), data);
    }
    
    public static RequestRecord parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new RequestRecord().mergeFrom(input);
    }
  }
}