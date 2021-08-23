// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.txz.version;

@SuppressWarnings("hiding")
public interface VersionManager {
  
  // enum SubCommand
  public static final int SUBCMD_DEFAULT = 0;
  public static final int SUBCMD_CHECK_NEW_VERSION = 1;
  public static final int SUBCMD_NEW_VERSION_PUBLISHED = 2;
  
  // enum ErrorCode
  public static final int EC_VERSION_LISENCE_DEFAULT = 0;
  public static final int EC_VERSION_LISENCE_FORBIDDEN = 65537;
  
  public static final class NewVersionInfo extends
      com.google.protobuf.nano.MessageNano {
    
    private static volatile NewVersionInfo[] _emptyArray;
    public static NewVersionInfo[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new NewVersionInfo[0];
          }
        }
      }
      return _emptyArray;
    }
    
    // optional .com.txz.ui.event.VersionInfo msg_new_version_info = 1;
    public com.txz.ui.event.UiEvent.VersionInfo msgNewVersionInfo;
    
    // optional string str_old_md5 = 2;
    public java.lang.String strOldMd5;
    
    // optional string str_new_md5 = 3;
    public java.lang.String strNewMd5;
    
    // optional string str_full_download_url = 4;
    public java.lang.String strFullDownloadUrl;
    
    // optional string str_inc_download_url = 5;
    public java.lang.String strIncDownloadUrl;
    
    public NewVersionInfo() {
      clear();
    }
    
    public NewVersionInfo clear() {
      msgNewVersionInfo = null;
      strOldMd5 = null;
      strNewMd5 = null;
      strFullDownloadUrl = null;
      strIncDownloadUrl = null;
      cachedSize = -1;
      return this;
    }
    
    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.msgNewVersionInfo != null) {
        output.writeMessage(1, this.msgNewVersionInfo);
      }
      if (this.strOldMd5 != null) {
        output.writeString(2, this.strOldMd5);
      }
      if (this.strNewMd5 != null) {
        output.writeString(3, this.strNewMd5);
      }
      if (this.strFullDownloadUrl != null) {
        output.writeString(4, this.strFullDownloadUrl);
      }
      if (this.strIncDownloadUrl != null) {
        output.writeString(5, this.strIncDownloadUrl);
      }
      super.writeTo(output);
    }
    
    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.msgNewVersionInfo != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(1, this.msgNewVersionInfo);
      }
      if (this.strOldMd5 != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(2, this.strOldMd5);
      }
      if (this.strNewMd5 != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(3, this.strNewMd5);
      }
      if (this.strFullDownloadUrl != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(4, this.strFullDownloadUrl);
      }
      if (this.strIncDownloadUrl != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(5, this.strIncDownloadUrl);
      }
      return size;
    }
    
    @Override
    public NewVersionInfo mergeFrom(
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
            if (this.msgNewVersionInfo == null) {
              this.msgNewVersionInfo = new com.txz.ui.event.UiEvent.VersionInfo();
            }
            input.readMessage(this.msgNewVersionInfo);
            break;
          }
          case 18: {
            this.strOldMd5 = input.readString();
            break;
          }
          case 26: {
            this.strNewMd5 = input.readString();
            break;
          }
          case 34: {
            this.strFullDownloadUrl = input.readString();
            break;
          }
          case 42: {
            this.strIncDownloadUrl = input.readString();
            break;
          }
        }
      }
    }
    
    public static NewVersionInfo parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new NewVersionInfo(), data);
    }
    
    public static NewVersionInfo parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new NewVersionInfo().mergeFrom(input);
    }
  }
}