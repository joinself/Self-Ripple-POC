#!/bin/bash

if [[ "$compiler" != "gcc" ]]; then
	echo "'gcc' is the only supported compilers, while '${compiler}' was specified"
	exit 1
fi
if [[ "$targetArch" != "armeabi" ]] && [[ "$targetArch" != "armeabi-v7a" ]] && [[ "$targetArch" != "arm64-v8a" ]] && [[ "$targetArch" != "x86" ]] && [[ "$targetArch" != "x86_64" ]] && [[ "$targetArch" != "mips" ]] && [[ "$targetArch" != "mips64" ]]; then
	echo "'armeabi', 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64', 'mips', 'mips64' are the only supported target architectures, while '${targetArch}' was specified"
	exit 1
fi
echo "Going to build OpenSSL for ${targetOS}/${compiler}/${targetArch}"

# Verify environment
if [[ -z "$ANDROID_SDK" ]]; then
	echo "ANDROID_SDK is not set"
	exit 1
fi
if [[ ! -d "$ANDROID_SDK" ]]; then
	echo "ANDROID_SDK '${ANDROID_SDK}' is set incorrectly"
	exit 1
fi
export ANDROID_SDK_ROOT=$ANDROID_SDK
echo "Using ANDROID_SDK '${ANDROID_SDK}'"

if [[ -z "$ANDROID_NDK" ]]; then
	echo "ANDROID_NDK is not set"
	exit 1
fi
if [[ ! -d "$ANDROID_NDK" ]]; then
	echo "ANDROID_NDK '${ANDROID_NDK}' is set incorrectly"
	exit 1
fi
export ANDROID_NDK_ROOT=$ANDROID_NDK
echo "Using ANDROID_NDK '${ANDROID_NDK}'"

if [[ "$(uname -a)" =~ Linux ]]; then
	if [[ "$(uname -m)" == x86_64 ]] && [ -d "$ANDROID_NDK/prebuilt/linux-x86_64" ]; then
		export ANDROID_NDK_HOST=linux-x86_64
	elif [ -d "$ANDROID_NDK/prebuilt/linux-x86" ]; then
		export ANDROID_NDK_HOST=linux-x86
	else
		export ANDROID_NDK_HOST=linux
	fi

	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`nproc`
	fi
elif [[ "$(uname -a)" =~ Darwin ]]; then
	if [[ "$(uname -m)" == x86_64 ]] && [ -d "$ANDROID_NDK/prebuilt/darwin-x86_64" ]; then
		export ANDROID_NDK_HOST=darwin-x86_64
	elif [ -d "$ANDROID_NDK/prebuilt/darwin-x86" ]; then
		export ANDROID_NDK_HOST=darwin-x86
	else
		export ANDROID_NDK_HOST=darwin
	fi

	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`sysctl hw.ncpu | awk '{print $2}'`
	fi
else
	echo "'$(uname -a)' host is not supported"
	exit 1
fi
if [[ -z "$ANDROID_SDK" ]]; then
	echo "ANDROID_NDK '${ANDROID_NDK}' contains no valid host prebuilt tools"
	exit 1
fi
echo "Using ANDROID_NDK_HOST '${ANDROID_NDK_HOST}'"

export ANDROID_TARGET_ARCH=$targetArch
echo "Using ANDROID_TARGET_ARCH '${ANDROID_TARGET_ARCH}'"

if [[ "$compiler" == "gcc" ]]; then
	export ANDROID_NDK_TOOLCHAIN_VERSION=4.9
fi
echo "Using ANDROID_NDK_TOOLCHAIN_VERSION '${ANDROID_NDK_TOOLCHAIN_VERSION}'"

TARGET="android"
TOOLCHAIN_PATH=""
CC_PREFIX=""
ANDROID_ARCH_HEADERS=""
if [[ "$targetArch" == "armeabi" ]] || [[ "$targetArch" == "armeabi-v7a" ]]; then
	TOOLCHAIN_PATH="${ANDROID_NDK}/toolchains/arm-linux-androideabi-${ANDROID_NDK_TOOLCHAIN_VERSION}"
	CC_PREFIX="arm-linux-androideabi-"
	ANDROID_ARCH_HEADERS="arm"
	TARGET="android-armeabi"
elif [[ "$targetArch" == "arm64-v8a" ]]; then
	TOOLCHAIN_PATH="${ANDROID_NDK}/toolchains/aarch64-linux-android-${ANDROID_NDK_TOOLCHAIN_VERSION}"
	CC_PREFIX="aarch64-linux-android-"
	ANDROID_ARCH_HEADERS="arm64"
	TARGET="android64-aarch64"
elif [[ "$targetArch" == "x86" ]]; then
	TOOLCHAIN_PATH="${ANDROID_NDK}/toolchains/x86-${ANDROID_NDK_TOOLCHAIN_VERSION}"
	CC_PREFIX="i686-linux-android-"
	ANDROID_ARCH_HEADERS="x86"
	TARGET="android"
elif [[ "$targetArch" == "x86_64" ]]; then
	TOOLCHAIN_PATH="${ANDROID_NDK}/toolchains/x86_64-${ANDROID_NDK_TOOLCHAIN_VERSION}"
	CC_PREFIX="x86_64-linux-android-"
	ANDROID_ARCH_HEADERS="x86_64"
	TARGET="android64"
elif [[ "$targetArch" == "mips" ]]; then
	TOOLCHAIN_PATH="${ANDROID_NDK}/toolchains/mipsel-linux-android-${ANDROID_NDK_TOOLCHAIN_VERSION}"
	CC_PREFIX="mipsel-linux-android-"
	ANDROID_ARCH_HEADERS="mips"
	TARGET="android-mips"
elif [[ "$targetArch" == "mips64" ]]; then
	TOOLCHAIN_PATH="${ANDROID_NDK}/toolchains/mips64el-linux-android-${ANDROID_NDK_TOOLCHAIN_VERSION}"
	CC_PREFIX="mips64el-linux-android-"
	ANDROID_ARCH_HEADERS="mips64"
	TARGET="android-mips"
fi
if [[ ! -d "$TOOLCHAIN_PATH" ]]; then
	echo "Toolchain at '$TOOLCHAIN_PATH' not found"
	exit 1
fi
echo "Using toolchain '${TOOLCHAIN_PATH}'"
echo "Using compiler prefix '${CC_PREFIX}'"

# Configure & Build static
STATIC_BUILD_PATH="$SRCLOC/upstream.patched.${targetOS}.${compiler}-${targetArch}.static"
if [ ! -d "$STATIC_BUILD_PATH" ]; then
	cp -rpf "$SRCLOC/upstream.patched" "$STATIC_BUILD_PATH"

	(cd "$STATIC_BUILD_PATH" && \
		PATH="$TOOLCHAIN_PATH/prebuilt/$ANDROID_NDK_HOST/bin:$PATH" \
		CROSS_SYSROOT="$ANDROID_NDK/platforms/android-21/arch-$ANDROID_ARCH_HEADERS" \
		CROSS_COMPILE=$CC_PREFIX \
		./Configure $TARGET --cross-compile-prefix=$CC_PREFIX -static no-shared)
	retcode=$?
	if [ $retcode -ne 0 ]; then
		echo "Failed to configure 'OpenSSL' for '${targetOS}.${compiler}-${targetArch}', aborting..."
		rm -rf "$path"
		exit $retcode
	fi
fi
(cd "$STATIC_BUILD_PATH" && \
	PATH="$TOOLCHAIN_PATH/prebuilt/$ANDROID_NDK_HOST/bin:$PATH" \
	CROSS_SYSROOT="$ANDROID_NDK/platforms/android-21/arch-$ANDROID_ARCH_HEADERS" \
	CROSS_COMPILE=$CC_PREFIX \
	make -j$CPU_CORES_NUM build_libs)
retcode=$?
if [ $retcode -ne 0 ]; then
	echo "Failed to build 'OpenSSL' for '${targetOS}.${compiler}-${targetArch}', aborting..."
	rm -rf "$path"
	exit $retcode
fi

exit 0
