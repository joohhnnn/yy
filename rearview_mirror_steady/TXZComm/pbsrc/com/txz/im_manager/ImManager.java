// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.im_manager;

@SuppressWarnings("hiding")
public interface ImManager {
  
  // enum SubCommand
  public static final int SUBCMD_0 = 0;
  public static final int SUBCMD_ROOM_IN = 1;
  public static final int SUBCMD_ROOM_OUT = 2;
  public static final int SUBCMD_ROOM_MEMBER_LIST = 3;
  
  // enum ErrorCode
  public static final int EC_DEFAULT = 0;
  public static final int EC_ROOM_NOT_EXIST = 8001;
  
  public static final class Req_RoomIn extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Req_RoomIn[] _emptyArray;
    public static Req_RoomIn[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Req_RoomIn[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_from_type = 1;
    public java.lang.Integer uint32FromType;
    
    // optional uint64 uint64_rid = 2;
    public java.lang.Long uint64Rid;
    
    // optional .com.txz.ui.im.RoomMember msg_user_info = 3;
    public com.txz.ui.im.UiIm.RoomMember msgUserInfo;
    
    public Req_RoomIn() {
      clear();
    }
    
    public Req_RoomIn clear() {
      uint32FromType = null;
      uint64Rid = null;
      msgUserInfo = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32FromType != null) {
        output.writeUInt32(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        output.writeUInt64(2, this.uint64Rid);
      }
      if (this.msgUserInfo != null) {
        output.writeMessage(3, this.msgUserInfo);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32FromType != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(2, this.uint64Rid);
      }
      if (this.msgUserInfo != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(3, this.msgUserInfo);
      }
      return size;
    }
    
    @Override
    public Req_RoomIn mergeFrom(
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
            this.uint32FromType = input.readUInt32();
            break;
          }
          case 16: {
            this.uint64Rid = input.readUInt64();
            break;
          }
          case 26: {
            if (this.msgUserInfo == null) {
              this.msgUserInfo = new com.txz.ui.im.UiIm.RoomMember();
            }
            input.readMessage(this.msgUserInfo);
            break;
          }
        }
      }
    }
    
    public static Req_RoomIn parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Req_RoomIn(), data);
    }
    
    public static Req_RoomIn parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Req_RoomIn().mergeFrom(input);
    }
  }
  
  public static final class Resp_RoomIn extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Resp_RoomIn[] _emptyArray;
    public static Resp_RoomIn[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Resp_RoomIn[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_from_type = 1;
    public java.lang.Integer uint32FromType;
    
    // optional uint64 uint64_rid = 2;
    public java.lang.Long uint64Rid;
    
    public Resp_RoomIn() {
      clear();
    }
    
    public Resp_RoomIn clear() {
      uint32FromType = null;
      uint64Rid = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32FromType != null) {
        output.writeUInt32(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        output.writeUInt64(2, this.uint64Rid);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32FromType != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(2, this.uint64Rid);
      }
      return size;
    }
    
    @Override
    public Resp_RoomIn mergeFrom(
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
            this.uint32FromType = input.readUInt32();
            break;
          }
          case 16: {
            this.uint64Rid = input.readUInt64();
            break;
          }
        }
      }
    }
    
    public static Resp_RoomIn parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Resp_RoomIn(), data);
    }
    
    public static Resp_RoomIn parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Resp_RoomIn().mergeFrom(input);
    }
  }
  
  public static final class Req_RoomOut extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Req_RoomOut[] _emptyArray;
    public static Req_RoomOut[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Req_RoomOut[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_from_type = 1;
    public java.lang.Integer uint32FromType;
    
    // optional uint64 uint64_rid = 2;
    public java.lang.Long uint64Rid;
    
    public Req_RoomOut() {
      clear();
    }
    
    public Req_RoomOut clear() {
      uint32FromType = null;
      uint64Rid = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32FromType != null) {
        output.writeUInt32(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        output.writeUInt64(2, this.uint64Rid);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32FromType != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(2, this.uint64Rid);
      }
      return size;
    }
    
    @Override
    public Req_RoomOut mergeFrom(
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
            this.uint32FromType = input.readUInt32();
            break;
          }
          case 16: {
            this.uint64Rid = input.readUInt64();
            break;
          }
        }
      }
    }
    
    public static Req_RoomOut parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Req_RoomOut(), data);
    }
    
    public static Req_RoomOut parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Req_RoomOut().mergeFrom(input);
    }
  }
  
  public static final class Resp_RoomOut extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Resp_RoomOut[] _emptyArray;
    public static Resp_RoomOut[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Resp_RoomOut[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_from_type = 1;
    public java.lang.Integer uint32FromType;
    
    // optional uint64 uint64_rid = 2;
    public java.lang.Long uint64Rid;
    
    public Resp_RoomOut() {
      clear();
    }
    
    public Resp_RoomOut clear() {
      uint32FromType = null;
      uint64Rid = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32FromType != null) {
        output.writeUInt32(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        output.writeUInt64(2, this.uint64Rid);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32FromType != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32FromType);
      }
      if (this.uint64Rid != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(2, this.uint64Rid);
      }
      return size;
    }
    
    @Override
    public Resp_RoomOut mergeFrom(
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
            this.uint32FromType = input.readUInt32();
            break;
          }
          case 16: {
            this.uint64Rid = input.readUInt64();
            break;
          }
        }
      }
    }
    
    public static Resp_RoomOut parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Resp_RoomOut(), data);
    }
    
    public static Resp_RoomOut parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Resp_RoomOut().mergeFrom(input);
    }
  }
  
  public static final class Req_RoomMemberList extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Req_RoomMemberList[] _emptyArray;
    public static Req_RoomMemberList[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Req_RoomMemberList[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint64 uint64_rid = 1;
    public java.lang.Long uint64Rid;
    
    // optional uint32 uint32_type = 2;
    public java.lang.Integer uint32Type;
    
    // optional .com.txz.ui.im.RoomMember msg_user_info = 3;
    public com.txz.ui.im.UiIm.RoomMember msgUserInfo;
    
    public Req_RoomMemberList() {
      clear();
    }
    
    public Req_RoomMemberList clear() {
      uint64Rid = null;
      uint32Type = null;
      msgUserInfo = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint64Rid != null) {
        output.writeUInt64(1, this.uint64Rid);
      }
      if (this.uint32Type != null) {
        output.writeUInt32(2, this.uint32Type);
      }
      if (this.msgUserInfo != null) {
        output.writeMessage(3, this.msgUserInfo);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint64Rid != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(1, this.uint64Rid);
      }
      if (this.uint32Type != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32Type);
      }
      if (this.msgUserInfo != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(3, this.msgUserInfo);
      }
      return size;
    }
    
    @Override
    public Req_RoomMemberList mergeFrom(
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
            this.uint64Rid = input.readUInt64();
            break;
          }
          case 16: {
            this.uint32Type = input.readUInt32();
            break;
          }
          case 26: {
            if (this.msgUserInfo == null) {
              this.msgUserInfo = new com.txz.ui.im.UiIm.RoomMember();
            }
            input.readMessage(this.msgUserInfo);
            break;
          }
        }
      }
    }
    
    public static Req_RoomMemberList parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Req_RoomMemberList(), data);
    }
    
    public static Req_RoomMemberList parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Req_RoomMemberList().mergeFrom(input);
    }
  }
  
  public static final class Resp_RoomMemberList extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Resp_RoomMemberList[] _emptyArray;
    public static Resp_RoomMemberList[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Resp_RoomMemberList[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint64 uint64_rid = 1;
    public java.lang.Long uint64Rid;
    
    // optional uint32 uint32_type = 2;
    public java.lang.Integer uint32Type;
    
    // repeated .com.txz.ui.im.RoomMember rpt_msg_member_list = 3;
    public com.txz.ui.im.UiIm.RoomMember[] rptMsgMemberList;
    
    public Resp_RoomMemberList() {
      clear();
    }
    
    public Resp_RoomMemberList clear() {
      uint64Rid = null;
      uint32Type = null;
      rptMsgMemberList = com.txz.ui.im.UiIm.RoomMember.emptyArray();
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint64Rid != null) {
        output.writeUInt64(1, this.uint64Rid);
      }
      if (this.uint32Type != null) {
        output.writeUInt32(2, this.uint32Type);
      }
      if (this.rptMsgMemberList != null && this.rptMsgMemberList.length > 0) {
        for (int i = 0; i < this.rptMsgMemberList.length; i++) {
          com.txz.ui.im.UiIm.RoomMember element = this.rptMsgMemberList[i];
          if (element != null) {
            output.writeMessage(3, element);
          }
        }
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint64Rid != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(1, this.uint64Rid);
      }
      if (this.uint32Type != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32Type);
      }
      if (this.rptMsgMemberList != null && this.rptMsgMemberList.length > 0) {
        for (int i = 0; i < this.rptMsgMemberList.length; i++) {
          com.txz.ui.im.UiIm.RoomMember element = this.rptMsgMemberList[i];
          if (element != null) {
            size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeMessageSize(3, element);
          }
        }
      }
      return size;
    }
    
    @Override
    public Resp_RoomMemberList mergeFrom(
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
            this.uint64Rid = input.readUInt64();
            break;
          }
          case 16: {
            this.uint32Type = input.readUInt32();
            break;
          }
          case 26: {
            int arrayLength = com.google.protobuf.nano.WireFormatNano
                .getRepeatedFieldArrayLength(input, 26);
            int i = this.rptMsgMemberList == null ? 0 : this.rptMsgMemberList.length;
            com.txz.ui.im.UiIm.RoomMember[] newArray =
                new com.txz.ui.im.UiIm.RoomMember[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.rptMsgMemberList, 0, newArray, 0, i);
            }
            for (; i < newArray.length - 1; i++) {
              newArray[i] = new com.txz.ui.im.UiIm.RoomMember();
              input.readMessage(newArray[i]);
              input.readTag();
            }
            // Last one without readTag.
            newArray[i] = new com.txz.ui.im.UiIm.RoomMember();
            input.readMessage(newArray[i]);
            this.rptMsgMemberList = newArray;
            break;
          }
        }
      }
    }
    
    public static Resp_RoomMemberList parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Resp_RoomMemberList(), data);
    }
    
    public static Resp_RoomMemberList parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Resp_RoomMemberList().mergeFrom(input);
    }
  }
}
