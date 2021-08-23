// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.ui.carcontrol;

@SuppressWarnings("hiding")
public interface CarControlData {
  
  // enum SubEventId
  public static final int SUBEVENT_DEFAULT = 0;
  public static final int SUBEVENT_TEMP_SETTING = 1;
  public static final int SUBEVENT_WSPEED_SETTING = 2;
  public static final int SUBEVENT_VOLUME_SETTING = 3;
  
  public static final class ACSettingData extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile ACSettingData[] _emptyArray;
    public static ACSettingData[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new ACSettingData[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_min_value = 1;
    public java.lang.Integer uint32MinValue;
    
    // optional uint32 uint32_max_value = 2;
    public java.lang.Integer uint32MaxValue;
    
    public ACSettingData() {
      clear();
    }
    
    public ACSettingData clear() {
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
    public ACSettingData mergeFrom(
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
    
    public static ACSettingData parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new ACSettingData(), data);
    }
    
    public static ACSettingData parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new ACSettingData().mergeFrom(input);
    }
  }
  
  public static final class VolumeSettingData extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile VolumeSettingData[] _emptyArray;
    public static VolumeSettingData[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new VolumeSettingData[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_min_value = 1;
    public java.lang.Integer uint32MinValue;
    
    // optional uint32 uint32_max_value = 2;
    public java.lang.Integer uint32MaxValue;
    
    public VolumeSettingData() {
      clear();
    }
    
    public VolumeSettingData clear() {
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
    public VolumeSettingData mergeFrom(
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
    
    public static VolumeSettingData parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new VolumeSettingData(), data);
    }
    
    public static VolumeSettingData parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new VolumeSettingData().mergeFrom(input);
    }
  }
}
