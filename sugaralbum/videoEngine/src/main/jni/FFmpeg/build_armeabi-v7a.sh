#!/usr/bin/env bash
make clean
rm -rf /prebuilt-lib/$CPU
rm config.fate
rm config.h
rm config.log
rm config.mak

export HOST_TAG=linux-x86_64
export ARCH=armv7a
export CPU=armeabi-v7a
export MIN=21
export ANDROID_NDK_PLATFORM=android-21

export PREFIX=$(pwd)/prebuilt-lib/$CPU
export NDK=/home/stephen/android-ndk-r10e

export MIN_PLATFORM=$NDK/platforms/android-$MIN
export SYSROOT=$NDK/platforms/android-5/arch-arm/
export TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.8/prebuilt/$HOST_TAG
export CC=$TOOLCHAIN/bin/arm-linux-androideabi-gcc
export NM=$TOOLCHAIN/bin/arm-linux-androideabi-nm
export STRIP=$TOOLCHAIN/bin/arm-linux-androideabi-strip
export CROSS_PREFIX=arm-linux-androideabi-

OPTIMIZE="-O3 -fpic -DANDROID -DHAVE_SYS_UIO_H=1 -Dipv6mr_interface=ipv6mr_ifindex -fasm -Wno-psabi -fno-short-enums -fno-strict-aliasing -finline-limit=300 -mfloat-abi=softfp -mfpu=vfpv3-d16 -marm -march=armv7-a"
ADDI_LDFLAGS="-Wl,-rpath-link=$NDK/platforms/android-5/arch-arm/usr/lib -L$NDK/platforms/android-5/arch-arm/usr/lib -llog"

sed  -i "s/SLIBNAME_WITH_MAJOR='\$(SLIBNAME).\$(LIBMAJOR)'/SLIBNAME_WITH_MAJOR='\$(SLIBPREF)\$(FULLNAME)-\$(LIBMAJOR)\$(SLIBSUF)'/" configure
sed  -i "s/LIB_INSTALL_EXTRA_CMD='\$\$(RANLIB) \"\$(LIBDIR)\\/\$(LIBNAME)\"'/LIB_INSTALL_EXTRA_CMD='\$\$(RANLIB) \"\$(LIBDIR)\\/\$(LIBNAME)\"'/" configure
sed  -i "s/SLIB_INSTALL_NAME='\$(SLIBNAME_WITH_VERSION)'/SLIB_INSTALL_NAME='\$(SLIBNAME_WITH_MAJOR)'/" configure
sed  -i "s/SLIB_INSTALL_LINKS='\$(SLIBNAME_WITH_MAJOR) \$(SLIBNAME)'/SLIB_INSTALL_LINKS='\$(SLIBNAME)'/" configure

./configure \
--arch=arm \
--target-os=linux \
--enable-cross-compile \
--cross-prefix=$CROSS_PREFIX \
--prefix=$PREFIX \
--cc=$CC \
--nm=$NM \
--strip=$STRIP \
--sysroot=$SYSROOT \
--enable-cross-compile \
--extra-cflags="$OPTIMIZE" \
--extra-ldflags="$ADDI_LDFLAGS" \
--enable-shared \
--disable-static \
--enable-runtime-cpudetect \
--disable-muxers \
--enable-muxer=mov \
--enable-muxer=mp4 \
--enable-muxer=aac \
--enable-muxer=mp3 \
--enable-muxer=ogg \
--enable-muxer=flac \
--enable-muxer=wav \
--enable-muxer=pcm_s16le \
--disable-demuxers \
--enable-demuxer=mov \
--enable-demuxer=aac \
--enable-demuxer=mp3 \
--enable-demuxer=ogg \
--enable-demuxer=flac \
--enable-demuxer=wav \
--enable-demuxer=pcm_s16le \
--disable-encoders \
--enable-encoder=mpeg4 \
--enable-encoder=aac \
--enable-encoder=pcm_s16le \
--disable-decoders \
--enable-decoder=h264 \
--enable-decoder=h264_vda \
--enable-decoder=mpeg4 \
--enable-decoder=aac \
--enable-decoder=aac_latm \
--enable-decoder=mp3 \
--enable-decoder=mp3float \
--enable-decoder=mp3adu \
--enable-decoder=mp3adufloat \
--enable-decoder=mp3on4 \
--enable-decoder=mp3on4float \
--enable-decoder=vorbis \
--enable-decoder=flac \
--enable-decoder=pcm_s16le \
--enable-decoder=pcm_s16le_planar \
--disable-parsers \
--enable-parser=mpeg4video \
--enable-parser=aac \
--enable-parser=aac_latm \
--enable-parser=mpegaudio \
--enable-parser=vorbis \
--enable-parser=flac \
--disable-avresample \
--disable-avdevice \
--disable-doc \
--disable-programs \
--disable-symver \
--enable-nonfree \
--enable-asm


sed  -i "s/#define HAVE_TRUNC 0/#define HAVE_TRUNC 1/" config.h
sed  -i "s/#define HAVE_TRUNCF 0/#define HAVE_TRUNCF 1/" config.h
sed  -i "s/#define HAVE_RINT 0/#define HAVE_RINT 1/" config.h
sed  -i "s/#define HAVE_LRINT 0/#define HAVE_LRINT 1/" config.h
sed  -i "s/#define HAVE_LRINTF 0/#define HAVE_LRINTF 1/" config.h
sed  -i "s/#define HAVE_ROUND 0/#define HAVE_ROUND 1/" config.h
sed  -i "s/#define HAVE_ROUNDF 0/#define HAVE_ROUNDF 1/" config.h
sed  -i "s/#define HAVE_CBRT 0/#define HAVE_CBRT 1/" config.h
sed  -i "s/#define HAVE_CBRTF 0/#define HAVE_CBRTF 1/" config.h
sed  -i "s/#define HAVE_COPYSIGN 0/#define HAVE_COPYSIGN 1/" config.h
sed  -i "s/#define HAVE_ERF 0/#define HAVE_ERF 1/" config.h
sed  -i "s/#define HAVE_HYPOT 0/#define HAVE_HYPOT 1/" config.h
sed  -i "s/#define HAVE_ISNAN 0/#define HAVE_ISNAN 1/" config.h
sed  -i "s/#define HAVE_ISFINITE 0/#define HAVE_ISFINITE 1/" config.h
sed  -i "s/#define HAVE_INET_ATON 0/#define HAVE_INET_ATON 1/" config.h
sed  -i "s/#define getenv(x) NULL/\\/\\/ #define getenv(x) NULL/" config.h

make clean
make -j8
make install
rm -rf prebuilt-lib/$CPU/lib/libavcodec.so
rm -rf prebuilt-lib/$CPU/lib/libavfilter.so
rm -rf prebuilt-lib/$CPU/lib/libavformat.so
rm -rf prebuilt-lib/$CPU/lib/libavutil.so
rm -rf prebuilt-lib/$CPU/lib/libswresample.so
rm -rf prebuilt-lib/$CPU/lib/libswscale.so