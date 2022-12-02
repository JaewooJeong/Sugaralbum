/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.kiwiple.mediaframework.ffmpeg;
public interface IFFmpegService extends android.os.IInterface
{
  /** Default implementation for IFFmpegService. */
  public static class Default implements com.kiwiple.mediaframework.ffmpeg.IFFmpegService
  {
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.kiwiple.mediaframework.ffmpeg.IFFmpegService
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.kiwiple.mediaframework.ffmpeg.IFFmpegService interface,
     * generating a proxy if needed.
     */
    public static com.kiwiple.mediaframework.ffmpeg.IFFmpegService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.kiwiple.mediaframework.ffmpeg.IFFmpegService))) {
        return ((com.kiwiple.mediaframework.ffmpeg.IFFmpegService)iin);
      }
      return new com.kiwiple.mediaframework.ffmpeg.IFFmpegService.Stub.Proxy(obj);
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
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements com.kiwiple.mediaframework.ffmpeg.IFFmpegService
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
      public static com.kiwiple.mediaframework.ffmpeg.IFFmpegService sDefaultImpl;
    }
    public static boolean setDefaultImpl(com.kiwiple.mediaframework.ffmpeg.IFFmpegService impl) {
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
    public static com.kiwiple.mediaframework.ffmpeg.IFFmpegService getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public static final java.lang.String DESCRIPTOR = "com.kiwiple.mediaframework.ffmpeg.IFFmpegService";
}
