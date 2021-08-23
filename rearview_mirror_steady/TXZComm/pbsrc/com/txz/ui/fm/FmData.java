// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.ui.fm;

@SuppressWarnings("hiding")
public interface FmData {
  
  // enum SubEventId
  public static final int SUBEVENT_FM_DEFAULT = 0;
  public static final int SUBEVENT_FM_SETTING = 1;
  public static final int SUBEVENT_FM_TOFREQ = 2;
  public static final int SUBEVENT_FM_NAMES_PATH = 3;
  
  public static final class FMSettingData extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile FMSettingData[] _emptyArray;
    public static FMSettingData[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new FMSettingData[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_min_value = 1;
    public java.lang.Integer uint32MinValue;
    
    // optional uint32 uint32_max_value = 2;
    public java.lang.Integer uint32MaxValue;
    
    public FMSettingData() {
      clear();
    }
    
    public FMSettingData clear() {
      uint32MinValue = null;
      uint32MaxValue = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.uint32MinValue != null) {
        output.writeUInt32(1, this.uint32MinValue);
      }
      if (this.uint32MaxValue != null) {
        output.writeUInt32(2, this.uint32MaxValue);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.uint32MinValue != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(1, this.uint32MinValue);
      }
      if (this.uint32MaxValue != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32MaxValue);
      }
      return size;
    }
    
    @Override
    public FMSettingData mergeFrom(
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
            this.uint32MinValue = input.readUInt32();
            break;
          }
          case 16: {
            this.uint32MaxValue = input.readUInt32();
            break;
          }
        }
      }
    }
    
    public static FMSettingData parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new FMSettingData(), data);
    }
    
    public static FMSettingData parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new FMSettingData().mergeFrom(input);
    }
  }
  
  public static final class FMResultData extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile FMResultData[] _emptyArray;
    public static FMResultData[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new FMResultData[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional string str_freq = 1;
    public java.lang.String strFreq;
    
    // optional uint32 uint32_unit = 2;
    public java.lang.Integer uint32Unit;
    
    public FMResultData() {
      clear();
    }
    
    public FMResultData clear() {
      strFreq = null;
      uint32Unit = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.strFreq != null) {
        output.writeString(1, this.strFreq);
      }
      if (this.uint32Unit != null) {
        output.writeUInt32(2, this.uint32Unit);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.strFreq != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(1, this.strFreq);
      }
      if (this.uint32Unit != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(2, this.uint32Unit);
      }
      return size;
    }
    
    @Override
    public FMResultData mergeFrom(
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
            this.strFreq = input.readString();
            break;
          }
          case 16: {
            this.uint32Unit = input.readUInt32();
            break;
          }
        }
      }
    }
    
    public static FMResultData parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new FMResultData(), data);
    }
    
    public static FMResultData parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new FMResultData().mergeFrom(input);
    }
  }
}