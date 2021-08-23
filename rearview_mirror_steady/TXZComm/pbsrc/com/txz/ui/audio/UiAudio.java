// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.ui.audio;

@SuppressWarnings("hiding")
public interface UiAudio {
  
  // enum SubEvent
  public static final int SUBEVENT_DEFAULT = 0;
  public static final int SUBEVENT_PREPROCESSING_READY = 1;
  public static final int SUBEVENT_REQ_DATA_INTERFACE = 2;
  public static final int SUBEVENT_RESP_DATA_INTERFACE = 3;
  public static final int SUBEVENT_DATA_PUSH_INTERFACE = 4;
  
  public static final class Req_DataInterface extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Req_DataInterface[] _emptyArray;
    public static Req_DataInterface[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Req_DataInterface[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional string str_cmd = 1;
    public java.lang.String strCmd;
    
    // optional bytes str_data = 2;
    public byte[] strData;
    
    // optional uint32 uint32_seq = 3;
    public java.lang.Integer uint32Seq;
    
    public Req_DataInterface() {
      clear();
    }
    
    public Req_DataInterface clear() {
      strCmd = null;
      strData = null;
      uint32Seq = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.strCmd != null) {
        output.writeString(1, this.strCmd);
      }
      if (this.strData != null) {
        output.writeBytes(2, this.strData);
      }
      if (this.uint32Seq != null) {
        output.writeUInt32(3, this.uint32Seq);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.strCmd != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(1, this.strCmd);
      }
      if (this.strData != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(2, this.strData);
      }
      if (this.uint32Seq != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(3, this.uint32Seq);
      }
      return size;
    }
    
    @Override
    public Req_DataInterface mergeFrom(
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
            this.strCmd = input.readString();
            break;
          }
          case 18: {
            this.strData = input.readBytes();
            break;
          }
          case 24: {
            this.uint32Seq = input.readUInt32();
            break;
          }
        }
      }
    }
    
    public static Req_DataInterface parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Req_DataInterface(), data);
    }
    
    public static Req_DataInterface parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Req_DataInterface().mergeFrom(input);
    }
  }
  
  public static final class Resp_DataInterface extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile Resp_DataInterface[] _emptyArray;
    public static Resp_DataInterface[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Resp_DataInterface[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional string str_cmd = 1;
    public java.lang.String strCmd;
    
    // optional bytes str_data = 2;
    public byte[] strData;
    
    // optional uint32 uint32_err_code = 3;
    public java.lang.Integer uint32ErrCode;
    
    // optional uint32 uint32_seq = 4;
    public java.lang.Integer uint32Seq;
    
    public Resp_DataInterface() {
      clear();
    }
    
    public Resp_DataInterface clear() {
      strCmd = null;
      strData = null;
      uint32ErrCode = null;
      uint32Seq = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.strCmd != null) {
        output.writeString(1, this.strCmd);
      }
      if (this.strData != null) {
        output.writeBytes(2, this.strData);
      }
      if (this.uint32ErrCode != null) {
        output.writeUInt32(3, this.uint32ErrCode);
      }
      if (this.uint32Seq != null) {
        output.writeUInt32(4, this.uint32Seq);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.strCmd != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(1, this.strCmd);
      }
      if (this.strData != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBytesSize(2, this.strData);
      }
      if (this.uint32ErrCode != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(3, this.uint32ErrCode);
      }
      if (this.uint32Seq != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(4, this.uint32Seq);
      }
      return size;
    }
    
    @Override
    public Resp_DataInterface mergeFrom(
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
            this.strCmd = input.readString();
            break;
          }
          case 18: {
            this.strData = input.readBytes();
            break;
          }
          case 24: {
            this.uint32ErrCode = input.readUInt32();
            break;
          }
          case 32: {
            this.uint32Seq = input.readUInt32();
            break;
          }
        }
      }
    }
    
    public static Resp_DataInterface parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Resp_DataInterface(), data);
    }
    
    public static Resp_DataInterface parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Resp_DataInterface().mergeFrom(input);
    }
  }
  
  public static final class AudioFavourite extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile AudioFavourite[] _emptyArray;
    public static AudioFavourite[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new AudioFavourite[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_type = 1;
    public java.lang.Integer uint32Type;
    
    // optional uint32 uint32_app_id = 2;
    public java.lang.Integer uint32AppId;
    
    // optional uint64 uint64_audio_id = 3;
    public java.lang.Long uint64AudioId;
    
    // optional string str_path = 4;
    public java.lang.String strPath;
    
    public AudioFavourite() {
      clear();
    }
    
    public AudioFavourite clear() {
      uint32Type = null;
      uint32AppId = null;
      uint64AudioId = null;
      strPath = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32Type != null) {
        output.writeUInt32(1, this.uint32Type);
      }
      if (this.uint32AppId != null) {
        output.writeUInt32(2, this.uint32AppId);
      }
      if (this.uint64AudioId != null) {
        output.writeUInt64(3, this.uint64AudioId);
      }
      if (this.strPath != null) {
        output.writeString(4, this.strPath);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32Type != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32Type);
      }
      if (this.uint32AppId != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32AppId);
      }
      if (this.uint64AudioId != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt64Size(3, this.uint64AudioId);
      }
      if (this.strPath != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(4, this.strPath);
      }
      return size;
    }
    
    @Override
    public AudioFavourite mergeFrom(
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
            this.uint32Type = input.readUInt32();
            break;
          }
          case 16: {
            this.uint32AppId = input.readUInt32();
            break;
          }
          case 24: {
            this.uint64AudioId = input.readUInt64();
            break;
          }
          case 34: {
            this.strPath = input.readString();
            break;
          }
        }
      }
    }
    
    public static AudioFavourite parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new AudioFavourite(), data);
    }
    
    public static AudioFavourite parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new AudioFavourite().mergeFrom(input);
    }
  }
  
  public static final class File_AudioList extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile File_AudioList[] _emptyArray;
    public static File_AudioList[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new File_AudioList[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // repeated .com.txz.ui.audio.AudioFavourite rpt_favourite_list = 1;
    public com.txz.ui.audio.UiAudio.AudioFavourite[] rptFavouriteList;
    
    public File_AudioList() {
      clear();
    }
    
    public File_AudioList clear() {
      rptFavouriteList = com.txz.ui.audio.UiAudio.AudioFavourite.emptyArray();
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.rptFavouriteList != null && this.rptFavouriteList.length > 0) {
        for (int i = 0; i < this.rptFavouriteList.length; i++) {
          com.txz.ui.audio.UiAudio.AudioFavourite element = this.rptFavouriteList[i];
          if (element != null) {
            output.writeMessage(1, element);
          }
        }
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.rptFavouriteList != null && this.rptFavouriteList.length > 0) {
        for (int i = 0; i < this.rptFavouriteList.length; i++) {
          com.txz.ui.audio.UiAudio.AudioFavourite element = this.rptFavouriteList[i];
          if (element != null) {
            size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeMessageSize(1, element);
          }
        }
      }
      return size;
    }
    
    @Override
    public File_AudioList mergeFrom(
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
            int arrayLength = com.google.protobuf.nano.WireFormatNano
                .getRepeatedFieldArrayLength(input, 10);
            int i = this.rptFavouriteList == null ? 0 : this.rptFavouriteList.length;
            com.txz.ui.audio.UiAudio.AudioFavourite[] newArray =
                new com.txz.ui.audio.UiAudio.AudioFavourite[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.rptFavouriteList, 0, newArray, 0, i);
            }
            for (; i < newArray.length - 1; i++) {
              newArray[i] = new com.txz.ui.audio.UiAudio.AudioFavourite();
              input.readMessage(newArray[i]);
              input.readTag();
            }
            // Last one without readTag.
            newArray[i] = new com.txz.ui.audio.UiAudio.AudioFavourite();
            input.readMessage(newArray[i]);
            this.rptFavouriteList = newArray;
            break;
          }
        }
      }
    }
    
    public static File_AudioList parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new File_AudioList(), data);
    }
    
    public static File_AudioList parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new File_AudioList().mergeFrom(input);
    }
  }
}
