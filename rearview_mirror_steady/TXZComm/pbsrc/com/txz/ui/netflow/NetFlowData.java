// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.ui.netflow;

@SuppressWarnings("hiding")
public interface NetFlowData {
  
  // enum SubEventId
  public static final int SUBEVENT_DEFAULT = 0;
  public static final int SUBEVENT_FLOW_SETTING = 1;
  
  public static final class NetFlowSettingData extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile NetFlowSettingData[] _emptyArray;
    public static NetFlowSettingData[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new NetFlowSettingData[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional uint32 uint32_min_value = 1;
    public java.lang.Integer uint32MinValue;
    
    // optional uint32 uint32_max_value = 2;
    public java.lang.Integer uint32MaxValue;
    
    public NetFlowSettingData() {
      clear();
    }
    
    public NetFlowSettingData clear() {
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
    public NetFlowSettingData mergeFrom(
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
    
    public static NetFlowSettingData parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new NetFlowSettingData(), data);
    }
    
    public static NetFlowSettingData parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new NetFlowSettingData().mergeFrom(input);
    }
  }
}