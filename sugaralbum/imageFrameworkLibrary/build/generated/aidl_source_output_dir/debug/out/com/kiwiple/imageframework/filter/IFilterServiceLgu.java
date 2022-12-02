/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.kiwiple.imageframework.filter;
public interface IFilterServiceLgu extends android.os.IInterface
{
  /** Default implementation for IFilterServiceLgu. */
  public static class Default implements com.kiwiple.imageframework.filter.IFilterServiceLgu
  {
    @Override public java.lang.String processingImageFile(java.lang.String filename, int size, com.kiwiple.imageframework.filter.Filter filter, java.lang.String stickerImageFilePath) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.graphics.Bitmap processingImageBitmap(android.graphics.Bitmap image, com.kiwiple.imageframework.filter.Filter filter, java.lang.String stickerImageFilePath) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void stopProcessing(boolean canceled) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.kiwiple.imageframework.filter.IFilterServiceLgu
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.kiwiple.imageframework.filter.IFilterServiceLgu interface,
     * generating a proxy if needed.
     */
    public static com.kiwiple.imageframework.filter.IFilterServiceLgu asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.kiwiple.imageframework.filter.IFilterServiceLgu))) {
        return ((com.kiwiple.imageframework.filter.IFilterServiceLgu)iin);
      }
      return new com.kiwiple.imageframework.filter.IFilterServiceLgu.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_processingImageFile:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          com.kiwiple.imageframework.filter.Filter _arg2;
          if ((0!=data.readInt())) {
            _arg2 = com.kiwiple.imageframework.filter.Filter.CREATOR.createFromParcel(data);
          }
          else {
            _arg2 = null;
          }
          java.lang.String _arg3;
          _arg3 = data.readString();
          java.lang.String _result = this.processingImageFile(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          reply.writeString(_result);
          return true;
        }
        case TRANSACTION_processingImageBitmap:
        {
          data.enforceInterface(descriptor);
          android.graphics.Bitmap _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.graphics.Bitmap.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          com.kiwiple.imageframework.filter.Filter _arg1;
          if ((0!=data.readInt())) {
            _arg1 = com.kiwiple.imageframework.filter.Filter.CREATOR.createFromParcel(data);
          }
          else {
            _arg1 = null;
          }
          java.lang.String _arg2;
          _arg2 = data.readString();
          android.graphics.Bitmap _result = this.processingImageBitmap(_arg0, _arg1, _arg2);
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        case TRANSACTION_stopProcessing:
        {
          data.enforceInterface(descriptor);
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.stopProcessing(_arg0);
          reply.writeNoException();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements com.kiwiple.imageframework.filter.IFilterServiceLgu
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
      @Override public java.lang.String processingImageFile(java.lang.String filename, int size, com.kiwiple.imageframework.filter.Filter filter, java.lang.String stickerImageFilePath) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(filename);
          _data.writeInt(size);
          if ((filter!=null)) {
            _data.writeInt(1);
            filter.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeString(stickerImageFilePath);
          boolean _status = mRemote.transact(Stub.TRANSACTION_processingImageFile, _data, _reply, 0);
          if (!_status) {
            if (getDefaultImpl() != null) {
              return getDefaultImpl().processingImageFile(filename, size, filter, stickerImageFilePath);
            }
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public android.graphics.Bitmap processingImageBitmap(android.graphics.Bitmap image, com.kiwiple.imageframework.filter.Filter filter, java.lang.String stickerImageFilePath) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.graphics.Bitmap _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((image!=null)) {
            _data.writeInt(1);
            image.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((filter!=null)) {
            _data.writeInt(1);
            filter.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeString(stickerImageFilePath);
          boolean _status = mRemote.transact(Stub.TRANSACTION_processingImageBitmap, _data, _reply, 0);
          if (!_status) {
            if (getDefaultImpl() != null) {
              return getDefaultImpl().processingImageBitmap(image, filter, stickerImageFilePath);
            }
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.graphics.Bitmap.CREATOR.createFromParcel(_reply);
          }
          else {
            _result = null;
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void stopProcessing(boolean canceled) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((canceled)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopProcessing, _data, _reply, 0);
          if (!_status) {
            if (getDefaultImpl() != null) {
              getDefaultImpl().stopProcessing(canceled);
              return;
            }
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static com.kiwiple.imageframework.filter.IFilterServiceLgu sDefaultImpl;
    }
    static final int TRANSACTION_processingImageFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_processingImageBitmap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_stopProcessing = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    public static boolean setDefaultImpl(com.kiwiple.imageframework.filter.IFilterServiceLgu impl) {
      // Only one user of this interface can use this function
      // at a time. This is a heuristic to detect if two different
      // users in the same process use this function.
      if (Stub.Proxy.sDefaultImpl != null) {
        throw new IllegalStateException("setDefaultImpl() called twice");
      }
      if (impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static com.kiwiple.imageframework.filter.IFilterServiceLgu getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public static final java.lang.String DESCRIPTOR = "com.kiwiple.imageframework.filter.IFilterServiceLgu";
  public java.lang.String processingImageFile(java.lang.String filename, int size, com.kiwiple.imageframework.filter.Filter filter, java.lang.String stickerImageFilePath) throws android.os.RemoteException;
  public android.graphics.Bitmap processingImageBitmap(android.graphics.Bitmap image, com.kiwiple.imageframework.filter.Filter filter, java.lang.String stickerImageFilePath) throws android.os.RemoteException;
  public void stopProcessing(boolean canceled) throws android.os.RemoteException;
}
